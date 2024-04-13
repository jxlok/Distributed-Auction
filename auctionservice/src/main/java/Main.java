import service.auctionservice.*;

public class Main {
    public static void main(String[] args) {
        AuctionService auctionService = new AuctionService();
        auctionService.startListening();
    }
}
