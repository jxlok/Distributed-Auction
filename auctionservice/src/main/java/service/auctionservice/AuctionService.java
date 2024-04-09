package service.auctionservice;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import service.core.BidOffer;
import service.core.BidOfferDeserializer;

import java.sql.*;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class AuctionService {

    private final KafkaConsumer<String, BidOffer> consumer;
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public AuctionService(){
        Properties props = new Properties();
        //Assign localhost id
        props.put("bootstrap.servers", "broker:19092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "bids");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BidOfferDeserializer.class.getName());

        this.consumer = new KafkaConsumer<>(props);

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
                    writeToDatabase(bid);
                    System.out.println("Received message: " + bid.toString());
                }
            }
        } finally {
            consumer.close();
        }
    }

    // Method to write data to MySQL database
    public void writeToDatabase(BidOffer newBidOffer) {
        try (
                // Create database connection
                Connection connection = DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
                // Prepare SQL statement
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM current_bids WHERE itemID = ?");
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE current_bids SET offerPrice = ?, userID = ? WHERE itemID = ?");
        ) {
            // Set parameter for SQL statement
            statement.setString(1, newBidOffer.getAuctionId().toString());

            // Get current bid in db
            ResultSet result = statement.executeQuery();
            //get the first and only item
            result.next();
            BidOffer currentBidOffer = new BidOffer(result.getLong("itemID"), result.getString("userID"), result.getDouble("offerPrice"));
            //Update bid if higher offerPrice
            if(newBidOffer.getOfferPrice() > currentBidOffer.getOfferPrice()){
                updateStatement.setString(1, newBidOffer.getOfferPrice().toString());
                updateStatement.setString(2, newBidOffer.getUserId());
                updateStatement.setString(3, newBidOffer.getAuctionId().toString());

                updateStatement.executeUpdate();

                //send to pawn.auction.updates for client to read
                System.out.println("Processed updated bid and sent to updateTopic");

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
