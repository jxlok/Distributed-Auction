package service.auctionservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import service.core.AuctionItem;
import service.core.BidUpdate;
import service.core.BidUpdateDeserializer;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class AuctionService {
    private static final String ZK_CONNECTION_STRING = "zookeeper1:2181"; // ZooKeeper connection string
    private static final String LEADER_PATH = "/leader"; // Path for leader election

    public static String QUERY_BY_ITEM_ID = "SELECT * FROM current_bids WHERE itemID = ? FOR UPDATE";
    public static String QUERY_ALL_ITEMS = "SELECT * FROM current_bids";
    public static String INSERT_AUCTION_ITEM =
            "INSERT INTO current_bids (startTime, endTime, offerPrice, bidTime, userID) VALUES (?,?,?,?,?)";
    public static String UPDATE_BID_PRICE_USER_AND_TIMESTAMP =
            "UPDATE current_bids SET offerPrice = ?, userID = ?, bidTime = ? WHERE itemID = ?";

    private final Producer<String, String> producer;
    private final KafkaConsumer<String, BidUpdate> consumer;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final List<Timestamp> endTimes;

    private CuratorFramework curatorFramework;
    private String leaderPath;

    public AuctionService() {

        Properties consumerProps = new Properties();
        //Assign localhost id
        consumerProps.put("bootstrap.servers", "broker:19092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "auction-services");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BidUpdateDeserializer.class.getName());

        Properties producerProps = new Properties();
        //Assign localhost id
        producerProps.put("bootstrap.servers", "broker:19092");
        //Set acknowledgements for producer requests.
        producerProps.put("acks", "all");
        //If the request fails, the producer can automatically retry,
        producerProps.put("retries", 0);
        //Specify buffer size in config
        producerProps.put("batch.size", 16384);
        //Reduce the no of requests less than 0
        producerProps.put("linger.ms", 1);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        this.consumer = new KafkaConsumer<>(consumerProps);
        this.producer = new KafkaProducer<>(producerProps);

        // Subscribe to the Kafka topic
        this.consumer.subscribe(Collections.singletonList("pawn.auction.bids"));

        // MySQL database configuration
        this.jdbcUrl = String.format("jdbc:mysql://%s:%s/%s", System.getenv("MYSQL_HOST"),
                System.getenv("MYSQL_PORT"), System.getenv("MYSQL_DATABASE"));
        this.username = System.getenv("MYSQL_USER");
        this.password = System.getenv("MYSQL_PASSWORD");

        this.endTimes = new ArrayList<>();
        for(AuctionItem item : getAllAuctionItems()){
            endTimes.add(item.getEndTime());
        }
        //fork a new thread run kafka consumer
        this.startListening();
        produceBidUpdateToClient();

        // Initialize CuratorFramework
        this.curatorFramework = CuratorFrameworkFactory.newClient("zookeeper:2181", new ExponentialBackoffRetry(1000, 3));
        this.curatorFramework.start();

        // Specify leader election path
        this.leaderPath = "/leader";
        // Start leader election
        electLeader();
    }

    private void electLeader() {
        System.out.println("Electing leader");
        try {
            // Attempt to become the leader by creating an ephemeral znode
            this.curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(leaderPath);
            // If znode creation succeeds, this instance becomes the leader
            System.out.println("I am the leader!");
            if (this.curatorFramework.checkExists().forPath(leaderPath) != null) {
                this.checkEndTimes();
            }
        } catch (KeeperException.NodeExistsException e) {
            // If znode creation fails due to node already existing, another instance is already the leader
            System.out.println("Someone else is the leader!");
            watchLeaderNode();
        } catch (Exception e) {
            // Handle other exceptions
            e.printStackTrace();
        }
    }


    // Watch for changes in leadership status
    private void watchLeaderNode() {
        try {
            // Register a watcher on the leader znode to detect changes in leadership
            this.curatorFramework.getData().usingWatcher(new LeaderWatcher()).forPath(leaderPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Watcher class to handle changes in leadership status
    private class LeaderWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            // Handle changes in leadership status
            if (event.getType() == Event.EventType.NodeDeleted) {
                // Leader znode was deleted, indicating a change in leadership
                electLeader(); // Reattempt leader election
            }
        }
    }

    public void checkEndTimes() {
        //new thread to do while loop, otherwise it blocks main thread. springboot can't finish initialisation.
        new Thread(() -> {
            try {
                while (true) {
                    boolean updated = false;
                    List<Timestamp> finished = new ArrayList<>();
                    for (Timestamp timestamp : endTimes) {
                        var now = LocalDateTime.now();
                        ZoneId zoneId = ZoneId.systemDefault();
                        var local_end_time = timestamp.toLocalDateTime().atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId).toLocalDateTime();
                        if(now.isAfter(local_end_time)){
                            finished.add(timestamp);
                            updated=true;
                        }
                    }
                    if(updated){
                        System.out.println("Finished auction found.");
                        endTimes.removeAll(finished);
                        produceBidUpdateToClient();
                    }
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        //back to main thread to continue springboot web application
    }

    public void startListening() {
        // Poll for new messages
        //new thread to do while loop, otherwise it blocks main thread. springboot can't finish initialisation.
        new Thread(() -> {
            try {
                while (true) {
                    ConsumerRecords<String, BidUpdate> records = consumer.poll(Duration.ofMillis(1000)); // Timeout in milliseconds
                    for (ConsumerRecord<String, BidUpdate> record : records) {
                        BidUpdate bid = record.value();
                        processBid(bid);
                        System.out.println("Received message: " + bid.toString());
                    }
                }
            } finally {
                consumer.close();
            }
        }).start();
        //back to main thread to continue springboot web application
    }

    public void processBid(BidUpdate newBidOffer) {
        try {
            if (writeToDatabase(newBidOffer)) {
                produceBidUpdateToClient();
            }
        }catch (SQLException e){
            System.out.println("SQL connection issue arised.");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // Method to write data to MySQL database
    public boolean writeToDatabase(BidUpdate newBidOffer) throws SQLException {
        // Create database connection
        Connection connection = DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
        try {
            connection.setAutoCommit(false);

            // Prepare SQL statement
            PreparedStatement statement = connection.prepareStatement(QUERY_BY_ITEM_ID);
            PreparedStatement updateStatement = connection.prepareStatement(UPDATE_BID_PRICE_USER_AND_TIMESTAMP);

            // Set parameter for SQL statement
            statement.setString(1, Long.toString(newBidOffer.getAuctionId()));

            // Get current bid in db
            ResultSet result = statement.executeQuery();
            //get the first and only item
            result.next();
            var auctionItem = readAsAuctionItem(result);

            //Update bid if higher offerPrice
            if (newBidOffer.getUpdatedTimestamp().before(auctionItem.getEndTime()) && newBidOffer.getNewBidPrice() > auctionItem.getOfferPrice()) {
                updateStatement.setLong(1, newBidOffer.getNewBidPrice());
                updateStatement.setString(2, newBidOffer.getUserId());
                updateStatement.setTimestamp(3, Timestamp.from(newBidOffer.getUpdatedTimestamp().toInstant()));
                updateStatement.setString(4, Long.toString(newBidOffer.getAuctionId()));

                int returnCode = updateStatement.executeUpdate();
                connection.commit();
                connection.close();
                return returnCode > 0;
            }
        }catch (SQLException e){
            connection.rollback();
        }

        connection.commit();
        connection.close();
        return false;
    }

    public void produceBidUpdateToClient() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    "pawn.auction.updates",
                    mapper.writeValueAsString(getAllAuctionItems())
            );

            producer.send(record).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<AuctionItem> getAllAuctionItems() {
        try {
            Connection connection = DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
            PreparedStatement statement = connection.prepareStatement(QUERY_ALL_ITEMS);

            ResultSet rs = statement.executeQuery();

            List<AuctionItem> auctionItems = new ArrayList<>();
            while (rs.next()) {
                auctionItems.add(readAsAuctionItem(rs));
            }

            return auctionItems;

        } catch(SQLException e){
            System.out.println("getAllAuctionItems: Connection to db failed.");
        }

        return new ArrayList<>();
    }

    public boolean addItem(AuctionItem auctionItem) {
        try (
            Connection connection = DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
            PreparedStatement statement = connection.prepareStatement(INSERT_AUCTION_ITEM)
            ){
                statement.setTimestamp(1, auctionItem.getStartTime());
                statement.setTimestamp(2, auctionItem.getEndTime());
                statement.setInt(3, auctionItem.getOfferPrice());
                statement.setTimestamp(4, auctionItem.getBidTime());
                statement.setString(5, auctionItem.getUserID());

                statement.execute();

                produceBidUpdateToClient();

                endTimes.add(auctionItem.getEndTime());
                return true;
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            return false;
        }
    }

    private AuctionItem readAsAuctionItem(ResultSet rs) throws SQLException {
        var item = new AuctionItem();
        item.setItemID(rs.getInt("itemID"));
        item.setStartTime(rs.getTimestamp("startTime"));
        item.setEndTime(rs.getTimestamp("endTime"));
        item.setOfferPrice(rs.getInt("offerPrice"));
        item.setBidTime(rs.getTimestamp("bidTime"));
        item.setUserID(rs.getString("userID"));
        return item;
    }
}
