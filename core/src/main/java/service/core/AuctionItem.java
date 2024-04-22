package service.core;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.TimeZone;

public class AuctionItem {
    private static final String ANSI_RED = "\u001B[1;31m";
    private static final String ANSI_GREEN = "\u001B[1;32m";
    private static final String ANSI_RESET = "\u001B[0m";

    private int itemID;
    private Timestamp startTime;
    private Timestamp endTime;
    private int offerPrice;
    private Timestamp bidTime;
    private String userID;
    public AuctionItem(){}
    public AuctionItem(int itemID, Timestamp startTime, Timestamp endTime, int offerPrice, Timestamp bidTime, String userID) {
        this.itemID = itemID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.offerPrice = offerPrice;
        this.bidTime = bidTime;
        this.userID = userID;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public int getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(int offerPrice) {
        this.offerPrice = offerPrice;
    }

    public Timestamp getBidTime() {
        return bidTime;
    }

    public void setBidTime(Timestamp bidTime) {
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

    public String toConsoleOutput() {;
        var now = OffsetDateTime.now();
        Instant startInstant = Instant.ofEpochMilli(startTime.getTime());
        Instant endInstant = Instant.ofEpochMilli(endTime.getTime());
        var local_start_time = startInstant.atOffset(ZoneId.systemDefault().getRules().getOffset(startInstant));
        var local_end_time = endInstant.atOffset(ZoneId.systemDefault().getRules().getOffset(endInstant));
        var isActive = local_start_time.isBefore(now) && local_end_time.isAfter(now);
        System.out.println(local_start_time.isBefore(now));
        System.out.println(local_end_time.isAfter(now));
        System.out.println(now);
        System.out.println(local_start_time);
        System.out.println(local_end_time);
        return (isActive? ANSI_GREEN + "[Active] " : ANSI_RED + "[Inactive] ") + ANSI_RESET + this.toString();
    }
}
