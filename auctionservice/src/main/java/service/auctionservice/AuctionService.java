package service.auctionservice;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class AuctionService {

    private final KafkaConsumer<String, String> consumer;

    public AuctionService(){
        Properties props = new Properties();
        //Assign localhost id
        props.put("bootstrap.servers", "broker:19092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "bids");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        this.consumer = new KafkaConsumer<>(props);

        // Subscribe to the Kafka topic
        this.consumer.subscribe(Collections.singletonList("pawn.auction.bids"));
    }

    public void startListening(){
        // Poll for new messages
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000)); // Timeout in milliseconds
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Received message: " + record.value());
                }
            }
        } finally {
            consumer.close();
        }
    }

}
