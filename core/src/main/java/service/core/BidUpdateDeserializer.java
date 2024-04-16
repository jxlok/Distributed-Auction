package service.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;

public class BidUpdateDeserializer implements Deserializer<BidUpdate> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No additional configuration required
    }

    @Override
    public BidUpdate deserialize(String topic, byte[] data) {
        ObjectMapper mapper = new ObjectMapper();
        BidUpdate bidUpdate = null;
        try {
            bidUpdate = mapper.readValue(data, BidUpdate.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bidUpdate;
    }

    @Override
    public void close() {
        // No resources to close
    }
}