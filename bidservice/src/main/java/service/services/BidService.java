package service.services;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import service.core.BidOffer;
import service.core.BidOfferSerializer;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class BidService {

    private Producer<String, BidOffer> producer;
    private ObjectMapper objectMapper;
    public BidService() {
        this.objectMapper = new ObjectMapper();

        Properties props = new Properties();
        //Assign localhost id
        props.put("bootstrap.servers", "broker:19092");
        //Set acknowledgements for producer requests.
        props.put("acks", "all");
        //If the request fails, the producer can automatically retry,
        props.put("retries", 0);
        //Specify buffer size in config
        props.put("batch.size", 16384);
        //Reduce the no of requests less than 0
        props.put("linger.ms", 1);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", BidOfferSerializer.class.getName());

        this.producer = new KafkaProducer<String, BidOffer>(props);
    }

    public void submit(BidOffer bidOffer) throws IOException, ExecutionException, InterruptedException {

        ProducerRecord<String, BidOffer> record = new ProducerRecord<>(
                "pawn.auction.bids",
                bidOffer.getAuctionId().toString(),
                bidOffer
        );

        producer.send(record).get();
    }
}
