package client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
            System.out.println(ANSI_YELLOW + "Please enter your command: [bid/refresh]" + ANSI_RESET);
            command = scanner.nextLine();
            switch (command) {
                case "refresh":
                    client.refresh();
                    break;
                case "bid":
                    System.out.println(ANSI_YELLOW + "Please enter the auction id: " + ANSI_RESET);
                    long auctionId = Long.parseLong(scanner.nextLine());
                    System.out.println(ANSI_YELLOW + "Please enter your bid € (integer): " + ANSI_RESET);
                    int bidOffer = Integer.parseInt(scanner.nextLine());
                    client.bid(auctionId, bidOffer);
                    break;
                default:
                    System.out.println(ANSI_YELLOW + "Invalid command, please try again [bid/refresh]" + ANSI_RESET);
            }
        }
    }
}
