package service.core;

import java.time.OffsetDateTime;

public class AuctionItem {

    private int itemID;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private int offerPrice;
    private OffsetDateTime bidTime;
    private String userID;

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public int getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(int offerPrice) {
        this.offerPrice = offerPrice;
    }

    public OffsetDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(OffsetDateTime bidTime) {
        this.bidTime = bidTime;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public String toString() {
        return "AuctionItem [itemID=" + itemID
                + ", startTime=" + startTime
                + ", endTime=" + endTime
                + ", offerPrice=" + offerPrice
                + ", bidTime=" + bidTime
                + ", userID=" + userID + "]";
    }
}
