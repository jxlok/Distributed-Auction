package service.auctionservice;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import service.core.*;

import java.sql.*;
import java.time.*;
import java.util.*;

public class AuctionService {

    public static String QUERY_BY_ITEM_ID = "SELECT * FROM current_bids WHERE itemID = ?";
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

    public AuctionService() {
        Properties consumerProps = new Properties();
        //Assign localhost id
        consumerProps.put("bootstrap.servers", "broker:19092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "bids");
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
        //fork a new thread run kafka consumer
        this.startListening();
        produceBidUpdateToClient();
        System.out.println("after produce");
    }

    public void startListening() {
        // Poll for new messages
        //new thread to do while loop, otherwise it blocks main thread. springboot can't finish initialisation.
        new Thread(new Runnable() {
            @Override
            public void run() {
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
            }
        }).start();
        //back to main thread to continue springboot web application
    }

    public void processBid(BidUpdate newBidOffer) {

        if (writeToDatabase(newBidOffer)) {
            produceBidUpdateToClient();
            //send to pawn.auction.updates for client to read
            System.out.println("Processed updated bid and sent to updateTopic");
        }
    }

    // Method to write data to MySQL database
    public boolean writeToDatabase(BidUpdate newBidOffer) {
        try (
                // Create database connection
                Connection connection = DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
                // Prepare SQL statement
                PreparedStatement statement = connection.prepareStatement(QUERY_BY_ITEM_ID);
                PreparedStatement updateStatement = connection.prepareStatement(UPDATE_BID_PRICE_USER_AND_TIMESTAMP);
        ) {
            // Set parameter for SQL statement
            statement.setString(1,  Long.toString(newBidOffer.getAuctionId()));

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

                return updateStatement.executeUpdate() > 0;
            }
            else {
                System.out.println("Failed to update bid price for item:" + newBidOffer.toString() + ", bid is closed or offer price is too low.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void produceBidUpdateToClient() {
        System.out.println("producing to update topic");
        ObjectMapper mapper = new ObjectMapper();
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    "pawn.auction.updates",
                    mapper.writeValueAsString(getAllAuctionItems())
            );

            producer.send(record).get();
            System.out.println("Triggered refresh");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Optional<AuctionItem> getAuctionItemById(int auctionId) {
        try (
                Connection connection = DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
                PreparedStatement statement = connection.prepareStatement(QUERY_BY_ITEM_ID);
        ) {
            statement.setString(1, Integer.toString(auctionId));
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return Optional.of(readAsAuctionItem(rs));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<AuctionItem> getAllAuctionItems() {
        try (
                Connection connection = DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
                PreparedStatement statement = connection.prepareStatement(QUERY_ALL_ITEMS);
        ) {
            ResultSet rs = statement.executeQuery();
            List<AuctionItem> auctionItems = new ArrayList<>();
            while (rs.next()) {
                auctionItems.add(readAsAuctionItem(rs));
            }

            return auctionItems;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean addItem(AuctionItem auctionItem) {
        try (
                Connection connection = DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
                PreparedStatement statement = connection.prepareStatement(INSERT_AUCTION_ITEM);
                ) {
            statement.setTimestamp(1, Timestamp.from(auctionItem.getStartTime().toInstant()));
            statement.setTimestamp(2, Timestamp.from(auctionItem.getEndTime().toInstant()));
            statement.setInt(3, auctionItem.getOfferPrice());
            statement.setTimestamp(4, Timestamp.from(auctionItem.getBidTime().toInstant()));
            statement.setString(5, auctionItem.getUserID());
            return statement.execute();
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
