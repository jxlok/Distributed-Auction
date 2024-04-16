package service.core;

import java.io.Serializable;

public final class BidOffer implements Serializable {
    public Long getAuctionId() {
        return auctionId;
    }

    public String getUserId() {
        return userId;
    }

    public int getOfferPrice() {
        return offerPrice;
    }

    private Long auctionId;
    private String userId;
    private int offerPrice;

    public BidOffer() {}

    public BidOffer(Long auctionId, String userId, int offerPrice) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.offerPrice = offerPrice;
    }

    @Override
    public String toString() {
        return "BidOffer [auctionId=" + auctionId + ", userId=" + userId + ", offerPrice=" + offerPrice + "]";
    }
}
