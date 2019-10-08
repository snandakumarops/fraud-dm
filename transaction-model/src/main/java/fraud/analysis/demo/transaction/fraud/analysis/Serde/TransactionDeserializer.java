package fraud.analysis.demo.transaction.fraud.analysis.Serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import fraud.analysis.demo.transaction.Transaction;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class TransactionDeserializer implements Deserializer {
    public void configure(Map map, boolean b) {

    }

    public Transaction deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        Transaction user = null;
        try {
            user = mapper.readValue(s, Transaction.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public void close() {

    }
}
