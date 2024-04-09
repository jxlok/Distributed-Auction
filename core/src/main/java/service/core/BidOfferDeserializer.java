package service.core;

import org.apache.kafka.common.serialization.Deserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class BidOfferDeserializer implements Deserializer<BidOffer> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No additional configuration required
    }

    @Override
    public BidOffer deserialize(String topic, byte[] data) {
        ObjectMapper mapper = new ObjectMapper();
        BidOffer bidoffer = null;
        try {
            bidoffer = mapper.readValue(data, BidOffer.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bidoffer;
    }

    @Override
    public void close() {
        // No resources to close
    }
}