package service.core;

import java.sql.Timestamp;
import java.time.ZoneId;
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
        ZoneId zoneId = ZoneId.systemDefault();
        return "AuctionItem [itemID=" + itemID
                + ", startTime=" + startTime
                + ", endTime=" + endTime
                + ", offerPrice=" + offerPrice
                + ", bidTime=" + bidTime
                + ", userID=" + userID + "]";
    }

    public String toConsoleOutput() {;
        var now = getCurrentLocalTimestamp();
        var isActive = startTime.before(now) && endTime.after(now);
        return (isActive? ANSI_GREEN + "[Active] " : ANSI_RED + "[Inactive] ") + ANSI_RESET + this;
    }

    public static Timestamp getCurrentLocalTimestamp() {
        // Get current time in UTC timezone
        long currentMillis = System.currentTimeMillis();

        // Create a Calendar instance and set it to the current time in UTC
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.setTimeInMillis(currentMillis);

        // Convert to local timezone
        Calendar localCalendar = Calendar.getInstance();
        localCalendar.setTimeInMillis(utcCalendar.getTimeInMillis());

        // Get the local timestamp
        return new Timestamp(localCalendar.getTimeInMillis());
    }
}
