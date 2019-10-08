package rtp.demo.debtor.complete.payment.stream;

import com.google.gson.Gson;
import com.myspace.ceptest.Reference;
import fraud.analysis.demo.transaction.CEPFraud;
import fraud.analysis.demo.transaction.Transaction;
import fraud.analysis.demo.transaction.fraud.analysis.Serde.RuleModel;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Reducer;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.time.SessionClock;
import org.kie.api.time.SessionPseudoClock;

import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class UpdateValidationStatusGlue {

	private static final Logger LOG = Logger.getLogger(UpdateValidationStatusGlue.class.getName());

	private String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
	private String incomingTransaction = System.getenv("INCOMING_TRANSACTION");
	private String transactionEval = System.getenv("DYNAMIC_EVAL_STATUS");
	private String applicationId = System.getenv("APPLICATION_ID");
	private String clientId = System.getenv("CLIENT_ID");


	private Properties streamsConfiguration = new Properties();
	private KafkaStreams streams;
	private KieSession kieSession;


	public UpdateValidationStatusGlue() {

		//Read main Kafka Stream as Fire hose of transactions

		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
		streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, applicationId);


		StreamsBuilder builder = new StreamsBuilder();

		KStream<String, String> incomingTransactions = builder.stream(incomingTransaction, Consumed.with(Serdes.String(), Serdes.String()));

		//Get all transactions for this customer id from secondary topic
		incomingTransactions
				.map((x, y) -> new KeyValue<String, String>(x, callOne(x, y)));



		KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);

		streams.start();


		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}

	public String callOne(String key, String value) {

		//Create a CEP session
		KieServices kieServices = KieServices.Factory.get();
		KieBaseConfiguration config = kieServices.newKieBaseConfiguration();
		config.setOption(EventProcessingOption.STREAM);
		KieContainer kieContainer = kieServices.getKieClasspathContainer();
		try {
			System.out.println("disposing session");
			kieSession.dispose();

		}catch (Exception e) {
			System.out.println("call to dispose session");
		}
		kieSession = kieContainer.newKieSession("dynamicrules");

		//config for the secondary topic, we read this topic from the beginning always (set the commit offset to a large number as thiis is a reference topic)
		Transaction txn = new Gson().fromJson(value,Transaction.class);
		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, UUID.randomUUID().toString());
		streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG,"60000000000000");
		streamsConfiguration.put(StreamsConfig.EXACTLY_ONCE,true);


		streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());

		StreamsBuilder builder = new StreamsBuilder();

		KStream<String, String> custTopic = builder.stream("TxnByCust", Consumed.with(Serdes.String(), Serdes.String()));


		Reference ref = new Reference();
		ref.setTransactionId(txn.getTransactionId());
		ref.setMerchantType(txn.getMerchantType());
		ref.setTransactionType(txn.getTransactionType());

		kieSession.insert(ref);



//		EntryPoint ep = kieSession.getEntryPoint("main");
//		ep.insert(txn);

		//Filter out transactions for the specific customer, and put the fraud response back on the fraud topic
		custTopic
				.filter((k,v)-> k.equals(txn.getCustomerId()))
				.map((x, y) -> new KeyValue<String, String>(new Gson().fromJson(y,Transaction.class).getTransactionId(), callTwo(new Gson().fromJson(y,Transaction.class).getTransactionId(), y,txn.getTransactionId())))
				.to(transactionEval);




		KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);

		streams.start();


		return null;

	}


	public String callTwo(String key, String value,String txnId) {

		//Push transactions to CEP session and fire all rules
		Transaction txn = new Gson().fromJson(value,Transaction.class);
		EntryPoint ep = kieSession.getEntryPoint("Reference");
		ep.insert(txn);
		kieSession.fireAllRules();
		Collection<?> fraudResponse = kieSession.getObjects(new ClassObjectFilter(CEPFraud.class));
		for(Object object: fraudResponse) {
			RuleModel ruleModel = new RuleModel();
			ruleModel.setFraudIdentified(new Date().getTime());
			CEPFraud fraud = (CEPFraud) object;
			System.out.println("key"+key+"fraud"+fraud.getTransaction().getTransactionId());
		if(fraud.getTransaction().getTransactionId().equals(key)) {
					ruleModel.setFraudReason(fraud.getFraudReason());
					txn.setRuleModel(ruleModel);
					return new Gson().toJson(txn);
				}


		}

		return null;

	}





}