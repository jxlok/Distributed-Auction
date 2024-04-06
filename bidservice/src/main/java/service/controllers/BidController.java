package service.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


import service.core.BidOffer;
import service.services.BidService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
public class BidController {
    private int port;
    private BidService bidService;

    public BidController(@Value("${server.port}") int port) {
        this.port = port;
        this.bidService = new BidService();
    }

    @PostMapping(value = "/bids", consumes = "application/json")
    public ResponseEntity<Void> submitBid(
            @RequestBody BidOffer bidOffer) {
        try {
            bidService.submit(bidOffer);
        } catch (IOException e) {
            // return 400 upon json encoding error
            System.out.println("Failed to encode bid message.");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ExecutionException | InterruptedException e) {
            // return 500 upon publish error
            System.out.println("Failed to publish message.");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        // return 201 upon success
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .build();
    }


}
