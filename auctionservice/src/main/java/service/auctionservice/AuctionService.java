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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Properties;

public class AuctionService {

    private final Producer<String, BidUpdate> producer;
    private final KafkaConsumer<String, BidOffer> consumer;
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public AuctionService(){
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

    }

    public void startListening(){
        // Poll for new messages
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

    public void processBid(BidOffer newBidOffer) {
        Timestamp currentTime = Timestamp.valueOf(LocalDateTime.now());

        if(writeToDatabase(newBidOffer, currentTime)){
            BidUpdate bidUpdate = new BidUpdate(newBidOffer.getAuctionId(), newBidOffer.getUserId(), newBidOffer.getOfferPrice(), currentTime);
            produceBidUpdateToClient(bidUpdate);
            //send to pawn.auction.updates for client to read
            System.out.println("Processed updated bid and sent to updateTopic");
        }
    }

    // Method to write data to MySQL database
    public boolean writeToDatabase(BidOffer newBidOffer, Timestamp currentTime) {
        try (
                // Create database connection
                Connection connection = DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
                // Prepare SQL statement
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM current_bids WHERE itemID = ?");
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE current_bids SET offerPrice = ?, userID = ?, bidTime = ? WHERE itemID = ?");
        ) {
            // Set parameter for SQL statement
            statement.setString(1, newBidOffer.getAuctionId().toString());

            // Get current bid in db
            ResultSet result = statement.executeQuery();
            //get the first and only item
            result.next();
            BidOffer currentBidOffer = new BidOffer(result.getLong("itemID"), result.getString("userID"), result.getDouble("offerPrice"));

            Timestamp bidEndTime = result.getTimestamp("endTime");
            //Update bid if higher offerPrice
            if(currentTime.before(bidEndTime) && newBidOffer.getOfferPrice() > currentBidOffer.getOfferPrice()){
                updateStatement.setString(1, newBidOffer.getOfferPrice().toString());
                updateStatement.setString(2, newBidOffer.getUserId());
                updateStatement.setString(3, currentTime.toString());
                updateStatement.setString(4, newBidOffer.getAuctionId().toString());

                updateStatement.executeUpdate();
                return true;
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

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
