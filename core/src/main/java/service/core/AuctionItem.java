package service.core;

import java.sql.Timestamp;
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
//        return "[itemID=" + itemID
//                + ", startTime=" + startTime
//                + ", endTime=" + endTime
//                + ", offerPrice=" + offerPrice
//                + ", bidTime=" + bidTime
//                + ", userID=" + userID + "]";
        return String.format("%-6d | %-21s | %-21s | %-10d | %-21s | %-10s |\n",
                itemID, startTime.toString(), endTime.toString(), offerPrice, bidTime.toString(), userID);
    }

    public String toConsoleOutput() {;
        var now = getCurrentLocalTimestamp();
        var isActive = startTime.before(now) && endTime.after(now);
        String status = (isActive ? ANSI_GREEN + "[Active] " : ANSI_RED + "[Inactive] ") + ANSI_RESET;
        String test = ANSI_RED + "[Inactive] " + ANSI_RESET;
//        System.out.println(String.format("%-11s|", status));
        return String.format("%-22s | ", status) + this;
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
