package service.auctionservice.controllers;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import service.auctionservice.*;
import service.core.AuctionItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController//main thread run this component
public class AuctionController {
    private final AuctionService auctionService;

    public AuctionController() {
        this.auctionService = new AuctionService();
    }

    @GetMapping(value = "/auctions")
    public ResponseEntity<List<AuctionItem>> getAllAuctionItem() {
        return ResponseEntity.ok(auctionService.getAllAuctionItems());
    }

    @PostMapping(value = "/auctions", consumes = "application/json")
    public ResponseEntity<Void> addAuctionItem(@RequestBody AuctionItem auctionItem) {
        return auctionService.addItem(auctionItem) ?
                ResponseEntity.ok().build() : ResponseEntity.status(500).build();

    }
}
