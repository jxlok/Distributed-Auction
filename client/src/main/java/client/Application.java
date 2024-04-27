package client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Scanner;

@SpringBootApplication
public class Application implements CommandLineRunner {
    public static final String ANSI_YELLOW = "\u001B[1;33m";
    public static final String ANSI_RESET = "\u001B[0m";
    @Autowired
    BidsClient client;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String command;
        System.out.println(ANSI_YELLOW + getCurrentTime() + " Please enter your command: [bid/create]" + ANSI_RESET);

        while (true) {
            command = scanner.nextLine().trim();

            switch (command) {
                case "bid":

                    long auctionId = 0;
                    while(auctionId <= 0){
                        try {
                            System.out.println(ANSI_YELLOW + "[bidding] Please enter the auction id: " + ANSI_RESET);
                            auctionId = Long.parseLong(scanner.nextLine());
                        }catch (NumberFormatException e){
                            System.out.println(ANSI_YELLOW+"Invalid format!"+ANSI_RESET);
                        }
                    }

                    int bidOffer = 0;
                    while(bidOffer <=0) {
                        try{
                            System.out.println(ANSI_YELLOW + "[bidding] Please enter your bid € (integer): " + ANSI_RESET);
                            bidOffer = Integer.parseInt(scanner.nextLine());
                        }catch (NumberFormatException e){
                            System.out.println(ANSI_YELLOW+"Invalid format!"+ANSI_RESET);
                        }
                    }
                    System.out.println(ANSI_YELLOW + getCurrentTime() + " Please enter your command: [bid/create]" + ANSI_RESET);
                    client.bid(auctionId, bidOffer);
                    break;
                case "create":
                    int time = 0;
                    while(time<=0) {
                        try{
                            System.out.println(ANSI_YELLOW + "[creating] Please enter how long you would like the auction to last in hours (1+): " + ANSI_RESET);
                            time = Integer.parseInt(scanner.nextLine());
                        }catch (NumberFormatException e) {
                            System.out.println(ANSI_YELLOW+"Invalid format!"+ANSI_RESET);
                        }
                    }
                    Timestamp startTime = Timestamp.from(Instant.now());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(startTime);
                    calendar.add(Calendar.HOUR_OF_DAY, time);
                    Timestamp endTime = new Timestamp(calendar.getTimeInMillis());

                    int offerPrice = 0;
                    while(offerPrice<=0){
                        try {
                            System.out.println(ANSI_YELLOW + "[creating] Please enter the price: " + ANSI_RESET);
                            offerPrice = Integer.parseInt(scanner.nextLine());
                        }catch (NumberFormatException e){
                            System.out.println(ANSI_YELLOW+"Invalid format!"+ANSI_RESET);
                        }
                    }

                    Timestamp bidTime = Timestamp.from(Instant.now());
                    client.createItem(startTime, endTime, offerPrice, bidTime, client.getUserId());
                    break;
                default:
                    System.out.println(ANSI_YELLOW + getCurrentTime() + " Invalid command, please try again [bid/create]" + ANSI_RESET);
            }
        }
    }

    private String getCurrentTime() {
        return "[" + OffsetDateTime.now().toString() + "]";
    }
}
