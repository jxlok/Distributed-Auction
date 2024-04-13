package service.auctionservice;

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

    private final Producer<String, BidUpdate> producer;
    private final KafkaConsumer<String, BidOffer> consumer;
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public AuctionService() {
        Properties consumerProps = new Properties();
        //Assign localhost id
        consumerProps.put("bootstrap.servers", "broker:19092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "bids");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BidOfferDeserializer.class.getName());

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
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BidUpdateSerializer.class.getName());

        this.consumer = new KafkaConsumer<>(consumerProps);
        this.producer = new KafkaProducer<>(producerProps);

        // Subscribe to the Kafka topic
        this.consumer.subscribe(Collections.singletonList("pawn.auction.bids"));

        // MySQL database configuration
        this.jdbcUrl = String.format("jdbc:mysql://%s:%s/%s", System.getenv("MYSQL_HOST"),
                System.getenv("MYSQL_PORT"), System.getenv("MYSQL_DATABASE"));
        this.username = System.getenv("MYSQL_USER");
        this.password = System.getenv("MYSQL_PASSWORD");

        this.startListening();
    }

    public void startListening() {
        // Poll for new messages
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        ConsumerRecords<String, BidOffer> records = consumer.poll(Duration.ofMillis(1000)); // Timeout in milliseconds
                        for (ConsumerRecord<String, BidOffer> record : records) {
                            BidOffer bid = record.value();
                            processBid(bid);
                            System.out.println("Received message: " + bid.toString());
                        }
                    }
                } finally {
                    consumer.close();
                }
            }
        }).start();

    }

    public void processBid(BidOffer newBidOffer) {
        OffsetDateTime currentTime = OffsetDateTime.now(ZoneId.of("UTC"));

        if (writeToDatabase(newBidOffer, currentTime)) {
            BidUpdate bidUpdate = new BidUpdate(newBidOffer.getAuctionId(), newBidOffer.getUserId(), newBidOffer.getOfferPrice(), Timestamp.from(currentTime.toInstant()));
            produceBidUpdateToClient(bidUpdate);
            //send to pawn.auction.updates for client to read
            System.out.println("Processed updated bid and sent to updateTopic");
        }
    }

    // Method to write data to MySQL database
    public boolean writeToDatabase(BidOffer newBidOffer, OffsetDateTime currentTime) {
        try (
                // Create database connection
                Connection connection = DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
                // Prepare SQL statement
                PreparedStatement statement = connection.prepareStatement(QUERY_BY_ITEM_ID);
                PreparedStatement updateStatement = connection.prepareStatement(UPDATE_BID_PRICE_USER_AND_TIMESTAMP);
        ) {
            // Set parameter for SQL statement
            statement.setString(1, newBidOffer.getAuctionId().toString());

            // Get current bid in db
            ResultSet result = statement.executeQuery();
            //get the first and only item
            result.next();
            var auctionItem = readAsAuctionItem(result);

            //Update bid if higher offerPrice
            if (currentTime.isBefore(auctionItem.getEndTime()) && newBidOffer.getOfferPrice() > auctionItem.getOfferPrice()) {
                updateStatement.setString(1, newBidOffer.getOfferPrice().toString());
                updateStatement.setString(2, newBidOffer.getUserId());
                updateStatement.setTimestamp(3, Timestamp.from(currentTime.toInstant()));
                updateStatement.setString(4, newBidOffer.getAuctionId().toString());

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

    public void produceBidUpdateToClient(BidUpdate bidUpdate) {
        try {
            ProducerRecord<String, BidUpdate> record = new ProducerRecord<>(
                    "pawn.auction.updates",
                    bidUpdate.getAuctionId().toString(),
                    bidUpdate
            );

            producer.send(record).get();
            System.out.println("Sent bid update to updateTopic");
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
        item.setStartTime(OffsetDateTime.of(rs.getTimestamp("startTime").toLocalDateTime(), ZoneOffset.UTC));
        item.setEndTime(OffsetDateTime.of(rs.getTimestamp("endTime").toLocalDateTime(), ZoneOffset.UTC));
        item.setOfferPrice(rs.getInt("offerPrice"));
        item.setBidTime(OffsetDateTime.of(rs.getTimestamp("bidTime").toLocalDateTime(), ZoneOffset.UTC));
        item.setUserID(rs.getString("userID"));
        return item;
    }
}
