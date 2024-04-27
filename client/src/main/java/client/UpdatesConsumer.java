package client;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import service.core.BidUpdate;
import service.core.BidUpdateDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Component
public class UpdatesConsumer {

    private final KafkaConsumer<String, BidUpdate> updatesConsumer;
    private final String userId;
    public static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RESET = "\u001B[0m";


    public UpdatesConsumer(@Value("${userId}") String userId) {
        this.userId = userId;
        Properties consumerProps = new Properties();
        //Assign localhost id
        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "client-" + userId );
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BidUpdateDeserializer.class.getName());

        updatesConsumer = new KafkaConsumer<>(consumerProps);
        updatesConsumer.subscribe(Collections.singletonList("pawn.auction.bids"));
        this.pollUpdates();
    }

    public void pollUpdates() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        var records = updatesConsumer.poll(Duration.ofMillis(1000));
                        for (var record : records) {
                            System.out.println(ANSI_CYAN+formatBidUpdate(record)+ANSI_RESET);
                        }
                    }
                }  catch (Exception e) {
                    System.out.println("Update consumer failed, closing client.");
                    e.printStackTrace();
                    Thread.interrupted();
                }   finally {
                    updatesConsumer.close();
                }
            }
        }).start();
    }

    private String formatBidUpdate(ConsumerRecord<String, BidUpdate> record) {

        return "Recieved a bid update:\n" +
                "[AuctionId: " + record.value().getAuctionId()+ "] " +
                "Latest Price: " + record.value().getNewBidPrice() + ", " +
                "Bidder: " + record.value().getUserId() + ", " +
                "Updated at: " + record.value().getUpdatedTimestamp();
    }
}
