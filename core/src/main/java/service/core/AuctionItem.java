package service.core;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public class AuctionItem {
    private static final String ANSI_RED = "\u001B[1;31m";
    private static final String ANSI_GREEN = "\u001B[1;32m";
    private static final String ANSI_RESET = "\u001B[0m";

    private int itemID;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private int offerPrice;
    private OffsetDateTime bidTime;
    private String userID;
    public AuctionItem(){}
    public AuctionItem(int itemID, OffsetDateTime startTime, OffsetDateTime endTime, int offerPrice, OffsetDateTime bidTime, String userID) {
        this.itemID = itemID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.offerPrice = offerPrice;
        this.bidTime = bidTime;
        this.userID = userID;
    }

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

    public String toConsoleOutput() {
        var now = OffsetDateTime.now(ZoneId.of("UTC"));
        var isActive = startTime.isBefore(now) && endTime.isAfter(now);

        return (isActive? ANSI_GREEN + "[Active] " : ANSI_RED + "[Inactive] ") + ANSI_RESET + this.toString();
    }
}
