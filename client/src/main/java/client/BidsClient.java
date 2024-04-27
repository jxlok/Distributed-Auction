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
import service.core.BidUpdate;
import service.core.BidUpdateDeserializer;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;

@Component
public class BidsClient {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final KafkaConsumer<String, String> updateConsumer;
    private final KafkaConsumer<String, BidUpdate> bidsConsumer;

    private final String userId;

    public String getUserId() {
        return userId;
    }

    public BidsClient(@Value("${userId}") String userId) {

        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        this.userId = userId;

        Properties updateConsumerProps = new Properties();
        updateConsumerProps.put("bootstrap.servers", "localhost:9092");
        updateConsumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "client-" + UUID.randomUUID());
        updateConsumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        updateConsumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        updateConsumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        updateConsumer = new KafkaConsumer<>(updateConsumerProps);

        TopicPartition partition = new TopicPartition("pawn.auction.updates", 0);
        updateConsumer.assign(Collections.singletonList(partition));
        updateConsumer.seekToBeginning(Collections.singletonList(partition));
        long currentOffset = updateConsumer.position(partition);
        // Fetch the last message
        updateConsumer.seek(partition, Math.max(currentOffset - 1, 0)); // Ensure offset is non-negative
        ConsumerRecords<String, String> records = updateConsumer.poll(Duration.ofMillis(1000));

        // Check if there are records available
        if (!records.isEmpty()) {
            try {
                ConsumerRecord<String, String> record = records.iterator().next();
                TypeFactory typeFactory = objectMapper.getTypeFactory();
                CollectionType listType = typeFactory.constructCollectionType(List.class, AuctionItem.class);
                List<AuctionItem> list = objectMapper.readValue(record.value(), listType);
                outputTable(list);
            }catch (Exception e){
                System.out.println("Intiialisation: Failed to parse pawn.auction.updates message.");
            }
        } else {
            System.out.println("Error: No records initialised in SQL or pawn.auction.updates.");
        }


        Properties bidConsumerProps = new Properties();
        //Assign localhost id
        bidConsumerProps.put("bootstrap.servers", "localhost:9092");
        bidConsumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "client-" + userId );
        bidConsumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        bidConsumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BidUpdateDeserializer.class.getName());

        bidsConsumer = new KafkaConsumer<>(bidConsumerProps);
        bidsConsumer.subscribe(Collections.singletonList("pawn.auction.bids"));
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
//                    System.out.println(response.isSuccessful() ? "Bid Successfully submitted" : "Bid submitted Failed");
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
                    var records = updateConsumer.poll(Duration.ofMillis(200));
                    for (var record : records) {
                        TypeFactory typeFactory = objectMapper.getTypeFactory();
                        CollectionType listType = typeFactory.constructCollectionType(List.class, AuctionItem.class);
                        List<AuctionItem> list = objectMapper.readValue(record.value(), listType);
                        outputTable(list);
                    }
                }
            }  catch (Exception e) {
                System.out.println("Update consumer failed, closing client.");
                e.printStackTrace();
                Thread.interrupted();
            }   finally {
                updateConsumer.close();
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

    private void outputTable(List<AuctionItem> list){
        System.out.println("-------------------------------------------------");
        list.forEach(item -> System.out.println(item.toConsoleOutput()));
        System.out.println("-------------------------------------------------");
    }



}
