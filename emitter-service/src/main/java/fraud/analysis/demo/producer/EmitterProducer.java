package fraud.analysis.demo.producer;

import fraud.analysis.demo.transaction.*;
import io.vertx.core.json.Json;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

public class EmitterProducer {
	private static final Logger LOGGER = Logger.getLogger(EmitterProducer.class.getName());

	private EmitterConfig config;
	private KafkaProducer<String, String> producer;
	private Properties props;

	public EmitterProducer() {
		this.config = EmitterConfig.fromEnv();
		this.props = EmitterConfig.createProperties(config);
		this.producer = new KafkaProducer<>(props);
	}

	public void setConfig(EmitterConfig emitterConfig) {
		this.config = emitterConfig;
	}

	public void sendMessage(String key, Transaction transaction) throws InterruptedException {
		LOGGER.info("Sending message: {}" + transaction.getMerchantType());
		transaction.setTimestamp(new Date().getTime());

		producer.send(new ProducerRecord<>(config.getTopic(), key, Json.encode(transaction)));
		System.out.println("message sent at" + new Date().getTime());
	}

	public void closeProducer() {
		producer.close();
	}

}
