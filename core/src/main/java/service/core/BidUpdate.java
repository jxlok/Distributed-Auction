package service.core;

import java.io.Serializable;
import java.sql.Timestamp;

public class BidUpdate implements Serializable {
    public Long getAuctionId() {
        return auctionId;
    }

    public String getUserId() {
        return userId;
    }

    public Double getNewBidPrice() {
        return new_bid_price;
    }

    public Timestamp getUpdatedTimestamp() {return updated_timestamp;}

    private Long auctionId;
    private String userId;
    private Double new_bid_price;
    private Timestamp updated_timestamp;

    public BidUpdate() {}

    public BidUpdate(Long auctionId, String userId, Double new_bid_price, Timestamp updated_timestamp) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.new_bid_price = new_bid_price;
        this.updated_timestamp = updated_timestamp;
    }


}
