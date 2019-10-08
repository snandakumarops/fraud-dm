package rtp.demo.debtor.complete.payment.stream;

import com.google.gson.Gson;
import com.myspace.ceptest.Reference;
import fraud.analysis.demo.transaction.CEPFraud;
import fraud.analysis.demo.transaction.Transaction;
import fraud.analysis.demo.transaction.fraud.analysis.Serde.RuleModel;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.*;
import java.util.logging.Logger;


public class UpdateValidationStatusGlue {

	private static final Logger LOG = Logger.getLogger(UpdateValidationStatusGlue.class.getName());

	private String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
	private String incomingTransaction = System.getenv("INCOMING_TRANSACTION");
	private String transactionEval = System.getenv("DYNAMIC_EVAL_STATUS");
	private String applicationId = System.getenv("APPLICATION_ID");
	private String clientId = System.getenv("CLIENT_ID");


	private Properties streamsConfiguration = new Properties();

	private KieSession kieSession;


	public UpdateValidationStatusGlue() {

		instantitateSession();


		//Load Reference Data


		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString());
		streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "60000000000000");


		streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());

		StreamsBuilder builder = new StreamsBuilder();

		KStream<String, String> custTopic = builder.stream("txn-by-cust", Consumed.with(Serdes.String(), Serdes.String()));

		System.out.println("Preloading reference data");
		custTopic.foreach(this::loadTrasactionHistory);
		System.out.println("Preloading complete");



		KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);

		streams.start();

		try {
			Thread.sleep(2000);
			System.out.println("sleeping");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("load after this");

		//Read main Kafka Stream as Fire hose of transactions
		Properties streamsConfiguration = new Properties();

		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
		streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, applicationId);


		builder = new StreamsBuilder();

		KStream<String, String> incomingTransactions = builder.stream(incomingTransaction, Consumed.with(Serdes.String(), Serdes.String()));

		KTable<String,String> txnByCust = incomingTransactions
				.selectKey((k,txn) -> new Gson().fromJson(txn,Transaction.class).getCustomerId()).groupByKey().aggregate(
						() -> "", /* initializer */
						(aggKey, newValue, aggValue) -> aggValue +"|"+ newValue, /* adder */
						Materialized.as("new store") /* state store name */
								);


		txnByCust.foreach(
				(k,v) -> System.out.println("K"+k+"V"+v)
		);

		//Get all transactions for this customer id from secondary topic
		incomingTransactions
				.map((x, y) -> new KeyValue<String, String>(new Gson().fromJson(y,Transaction.class).getTransactionId(), callOne(x, y))).filter((k,v) -> v!= null).to(transactionEval);


		KafkaStreams mainStream = new KafkaStreams(builder.build(), streamsConfiguration);

		mainStream.start();

		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

		Runtime.getRuntime().addShutdownHook(new Thread(mainStream::close));

	}

	public String callOne(String key, String value) {
		System.out.println("txn details"+key);
		Transaction txn = new Gson().fromJson(value, Transaction.class);

		Reference ref = new Reference();
		ref.setTransactionId(txn.getTransactionId());
		ref.setMerchantType(txn.getMerchantType());
		ref.setTransactionType(txn.getTransactionType());

		kieSession.insert(ref);
		kieSession.fireAllRules();

		Collection<?> fraudResponse = kieSession.getObjects(new ClassObjectFilter(CEPFraud.class));
		for (Object object : fraudResponse) {
			System.out.println(object.toString());
			RuleModel ruleModel = new RuleModel();
			ruleModel.setFraudIdentified(new Date().getTime());
			CEPFraud fraud = (CEPFraud) object;
			System.out.println("key" + key + "fraud" + fraud.getTransaction().getTransactionId());
			if (fraud.getTransaction().getTransactionId().equals(txn.getTransactionId())) {
				ruleModel.setFraudReason(fraud.getFraudReason());
				txn.setRuleModel(ruleModel);
				return new Gson().toJson(txn);
			}


		}
		return null;
	}


	public void loadTrasactionHistory(String key, String value) {
		System.out.println("loaded values");
		//Push transactions to CEP session and fire all rules
		Transaction txn = new Gson().fromJson(value, Transaction.class);
		EntryPoint ep = kieSession.getEntryPoint("Reference");
		ep.insert(txn);
	}


	public void instantitateSession() {
		//Create a CEP session
		KieServices kieServices = KieServices.Factory.get();
		KieBaseConfiguration config = kieServices.newKieBaseConfiguration();
		config.setOption(EventProcessingOption.STREAM);
		KieContainer kieContainer = kieServices.getKieClasspathContainer();
		kieSession = kieContainer.newKieSession("dynamicrules");

	}


}