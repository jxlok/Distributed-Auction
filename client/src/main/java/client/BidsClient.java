package client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import service.core.AuctionItem;
import service.core.BidOffer;
import service.core.BidUpdate;
import service.core.BidUpdateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Component
public class BidsClient {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String userId;
    private final KafkaConsumer<String, String> consumer;

    public BidsClient(@Value("${userId}") String userId) {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        this.userId = userId;
        Properties consumerProps = new Properties();

        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "client-" + userId );
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList("pawn.auction.updates"));
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
                    var records = consumer.poll(Duration.ofMillis(1000));
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
            Instant startInstant = Instant.ofEpochMilli(startTime.getTime());
            Instant endInstant = Instant.ofEpochMilli(endTime.getTime());
            Timestamp local_start_time = Timestamp.from(startInstant.atZone(ZoneId.of("UTC")).toInstant());
            Timestamp local_end_time = Timestamp.from(endInstant.atZone(ZoneId.of("UTC")).toInstant());

            var request = new Request

                    .Builder()
                    .post(RequestBody.create(
                            objectMapper.writeValueAsBytes(new AuctionItem(0, local_start_time, local_end_time, offerPrice, bidTime, userID))
                    ))
                    .url("http://localhost:8000/auctions")
                    .addHeader("Content-Type", "application/json")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    System.out.println(response.message());
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
