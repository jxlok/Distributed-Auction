package client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import service.core.AuctionItem;
import service.core.BidOffer;

import java.io.IOException;
import java.util.List;

@Component
public class BidsClient {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String userId;

    public BidsClient(@Value("${userId}") String userId) {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        this.userId = userId;
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
                    System.out.println(response.isSuccessful() ? "Bid Success" : "Bid Failed");
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

    public void refresh() {
        var request = new Request
                        .Builder()
                        .get()
                        .url("http://localhost:8000/auctions")
                        .addHeader("Accept", "application/json")
                        .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                TypeFactory typeFactory = objectMapper.getTypeFactory();
                List<AuctionItem> list = objectMapper.readValue(
                        response.body().string(),
                        typeFactory.constructCollectionType(List.class, AuctionItem.class)
                );
                list.forEach(item -> System.out.println(item.toString()));
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Refresh command field, please try again. Error: " + e.getCause());
            }
        });
    }
}
