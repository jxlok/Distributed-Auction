package client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import service.core.AuctionItem;
import service.core.BidOffer;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
public class BidsClient {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public String getUserId() {
        return userId;
    }

    private final String userId;
    private final KafkaConsumer<String, String> consumer;

    public BidsClient(@Value("${userId}") String userId) throws JsonProcessingException, InterruptedException {
        Thread.sleep(5000);
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        this.userId = userId;
        Properties consumerProps = new Properties();

        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "client-" + UUID.randomUUID().toString() );
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        consumer = new KafkaConsumer<>(consumerProps);

        TopicPartition partition = new TopicPartition("pawn.auction.updates", 0);
        consumer.assign(Collections.singletonList(partition));
        consumer.seekToBeginning(Collections.singletonList(partition));
        long currentOffset = consumer.position(partition);

        // Fetch the last message
        consumer.seek(partition, Math.max(currentOffset - 1, 0)); // Ensure offset is non-negative
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

        // Check if there are records available
        if (!records.isEmpty()) {
            ConsumerRecord<String, String> record = records.iterator().next();
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            CollectionType listType = typeFactory.constructCollectionType(List.class, AuctionItem.class);
            List<AuctionItem> list = objectMapper.readValue(record.value(), listType);
            list.forEach(item -> System.out.println(item.toConsoleOutput()));
        } else {
            System.out.println("Error: No records initialised in SQL or pawn.auction.updates.");
        }

        this.pollUpdates();
    }

    public void bid(long auctionId, int price) {
        try {
            var request = new Request
                    .Builder()
                    .post(RequestBody.create(
                            objectMapper.writeValueAsBytes(new BidOffer(auctionId, userId, price))
                    ))
                    .url("http://localhost:8000/bids")
                    .addHeader("Content-Type", "application/json")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    System.out.println(response.isSuccessful() ? "Bid Successfully submitted" : "Bid submitted Failed");
                }
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    System.out.println("Bid Failed. Error: " + e.getMessage());
                }
            });

        } catch (JsonProcessingException e) {
            System.out.println("Error while creating bid request, please try again. Error: " + e.getMessage());
        }
    }
    public void pollUpdates() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        new Thread(() -> {
            try {
                while (true) {
                    var records = consumer.poll(Duration.ofMillis(200));
                    for (var record : records) {
                        TypeFactory typeFactory = objectMapper.getTypeFactory();
                        CollectionType listType = typeFactory.constructCollectionType(List.class, AuctionItem.class);
                        List<AuctionItem> list = objectMapper.readValue(record.value(), listType);
                        list.forEach(item -> System.out.println(item.toConsoleOutput()));
                    }
                }
            }  catch (Exception e) {
                System.out.println("Update consumer failed, closing client.");
                e.printStackTrace();
                Thread.interrupted();
            }   finally {
                consumer.close();
            }
        }).start();
    }

    public void createItem(Timestamp startTime, Timestamp endTime, int offerPrice, Timestamp bidTime, String userID) {
        try {
            var request = new Request
                    .Builder()
                    .post(RequestBody.create(
                            objectMapper.writeValueAsBytes(new AuctionItem(0, startTime, endTime, offerPrice, bidTime, userID))
                    ))
                    .url("http://localhost:8000/auctions")
                    .addHeader("Content-Type", "application/json")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
//                    System.out.println(response.message());
                    System.out.println(response.isSuccessful() ? "Create Success" : "Create Failed");
                }
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    System.out.println("Create New Item Failed. Error: " + e.getMessage());
                }
            });

        } catch (JsonProcessingException e) {
            System.out.println("Error while creating bid request, please try again. Error: " + e.getMessage());
        }
    }

}
