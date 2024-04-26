package service.auctionservice.controllers;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import service.auctionservice.*;
import service.core.AuctionItem;

import java.util.List;

@RestController//main thread run this component
public class AuctionController {
    private final AuctionService auctionService;

    public AuctionController() {
        this.auctionService = new AuctionService();
    }

    @GetMapping(value = "/auctions/{id}")
    public ResponseEntity<AuctionItem> createAuctionItem(@PathVariable("id") int auctionId) {
        var auctionItem = auctionService.getAuctionItemById(auctionId);

        return auctionItem
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/auctions")
    public ResponseEntity<List<AuctionItem>> addAuctionItem() {
        return ResponseEntity.ok(auctionService.getAllAuctionItems());
    }

    @PostMapping(value = "/auctions", consumes = "application/json")
    public ResponseEntity<Void> addAuctionItem(@RequestBody AuctionItem auctionItem) {
        return auctionService.addItem(auctionItem) ?
                ResponseEntity.ok().build() : ResponseEntity.status(500).build();

    }
}
