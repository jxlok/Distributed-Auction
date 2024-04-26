package service.core;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.OffsetDateTime;

public class BidUpdate implements Serializable {
    public long getAuctionId() {
        return auctionId;
    }

    public String getUserId() {
        return userId;
    }

    public int getNewBidPrice() {
        return newBidPrice;
    }

    public Timestamp getUpdatedTimestamp() {return updatedTimestamp;}

    private long auctionId;
    private String userId;
    private int newBidPrice;
    private Timestamp updatedTimestamp;

    public BidUpdate() {}

    public BidUpdate(Long auctionId, String userId, int newBidPrice, Timestamp updatedTimestamp) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.newBidPrice = newBidPrice;
        this.updatedTimestamp = updatedTimestamp;
    }

    @Override
    public String toString() {
        return "BidUpdate [auctionId=" + auctionId
                + ", userId=" + userId
                + ", new_bid_price=" + newBidPrice
                + ", updated_timestamp=" + updatedTimestamp
                + "]";
    }


}
