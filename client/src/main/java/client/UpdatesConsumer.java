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

    public UpdatesConsumer(@Value("${userId}") String userId) {
        this.userId = userId;
        Properties consumerProps = new Properties();
        //Assign localhost id
        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "client-" + userId );
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BidUpdateDeserializer.class.getName());

        updatesConsumer = new KafkaConsumer<String, BidUpdate>(consumerProps);
        updatesConsumer.subscribe(Collections.singletonList("pawn.auction.updates"));
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
                            System.out.println(formatBidUpdate(record));
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
                "[AuctionId: " + record.value().getAuctionId()+ "]\n" +
                "Latest Price: " + record.value().getNewBidPrice() + ",\n" +
                "Bidder: " + record.value().getUserId() + ",\n" +
                "Updated at: " + record.value().getUpdatedTimestamp();
    }
}
