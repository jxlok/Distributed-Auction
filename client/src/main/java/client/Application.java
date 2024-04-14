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
            System.out.println(ANSI_YELLOW + getCurrentTime() + " Please enter your command: [bid/refresh]" + ANSI_RESET);
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
                default:
                    System.out.println(ANSI_YELLOW + getCurrentTime() + " Invalid command, please try again [bid/refresh]" + ANSI_RESET);
            }
        }
    }

    private String getCurrentTime() {
        return "[" + OffsetDateTime.now(ZoneId.of("UTC")).toString() + "]";
    }
}
