package fraud.analysis.demo.transaction.fraud.analysis.Serde;
import java.util.Map;

import fraud.analysis.demo.transaction.Transaction;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;



public class TransactionSerializer implements Serializer<Transaction> {


    @Override
    public byte[] serialize(String s, Transaction transaction) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsString(s).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    @Override
        public void close() {

        }

        @Override
        public void configure(Map arg0, boolean arg1) {
            // TODO Auto-generated method stub

        }



}
