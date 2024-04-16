package client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.OffsetDateTime;
import java.time.ZoneId;
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
        while (true) {
            System.out.println(ANSI_YELLOW + getCurrentTime() + " Please enter your command: [bid/refresh/create]" + ANSI_RESET);
            command = scanner.nextLine().trim();
            switch (command) {
                case "refresh":
                    client.refresh();
                    break;
                case "bid":
                    System.out.println(ANSI_YELLOW + "[bidding] Please enter the auction id: " + ANSI_RESET);
                    long auctionId = Long.parseLong(scanner.nextLine());
                    System.out.println(ANSI_YELLOW + "[bidding] Please enter your bid € (integer): " + ANSI_RESET);
                    int bidOffer = Integer.parseInt(scanner.nextLine());
                    client.bid(auctionId, bidOffer);
                    break;
                case "create":
                    OffsetDateTime bidTime = OffsetDateTime.now(ZoneId.of("UTC"));
                    System.out.println(ANSI_YELLOW + "[creating] Please enter the usern id: " + ANSI_RESET);
                    String userID = scanner.nextLine();
                    System.out.println(ANSI_YELLOW + "[creating] Please enter how long you would like the auction to be(1hour or 2hour): " + ANSI_RESET);
                    int time = Integer.parseInt(scanner.nextLine());
                    OffsetDateTime startTime = OffsetDateTime.now(ZoneId.of("UTC"));
                    OffsetDateTime endTime = startTime.plusHours(time);
                    System.out.println(ANSI_YELLOW + "[creating] Please enter the price: " + ANSI_RESET);
                    int offerPrice = Integer.parseInt(scanner.nextLine());
                    client.createItem(startTime, endTime, offerPrice, bidTime, userID);
                    break;
                default:
                    System.out.println(ANSI_YELLOW + getCurrentTime() + " Invalid command, please try again [bid/refresh/create]" + ANSI_RESET);
            }
        }
    }

    private String getCurrentTime() {
        return "[" + OffsetDateTime.now(ZoneId.of("UTC")).toString() + "]";
    }
}
