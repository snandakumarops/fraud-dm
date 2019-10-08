package rtp.demo.debtor.complete.payment.stream;

import com.google.gson.Gson;
import fraud.analysis.demo.transaction.Account;
import fraud.analysis.demo.transaction.Transaction;
import fraud.analysis.demo.transaction.fraud.analysis.Serde.RuleModel;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.dmn.api.core.*;
import rtp.demo.creditor.repository.account.AccountRepository;
import rtp.demo.creditor.repository.account.JdgAccountRepository;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.Logger;


public class UpdateValidationStatusGlue {


	private static final Logger LOG = Logger.getLogger(UpdateValidationStatusGlue.class.getName());

	AccountRepository accountRepository = new JdgAccountRepository();

	private String bootstrapServers = System.getenv("BOOTSTRAP_SERVERS");
	private String incomingTransaction = System.getenv("INCOMING_TRANSACTION");
	private String transactionEval = System.getenv("STATIC_EVAL_STATUS");
	private String applicationId = System.getenv("APPLICATION_ID");
	private String clientId = System.getenv("CLIENT_ID");


	private Properties streamsConfiguration = new Properties();
	private KafkaStreams streams;
	KieContainer kContainer = KieServices.Factory.get().newKieClasspathContainer();


	public UpdateValidationStatusGlue() {


		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
		streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, clientId);

		StreamsBuilder builder = new StreamsBuilder();

		KStream<String, String> incomingTransactions = builder.stream(incomingTransaction,
				Consumed.with(Serdes.String(), Serdes.String()));

		KStream<String, String> outData = incomingTransactions.map((x,y) -> new KeyValue<String,String>(x,callDMN(x,y))).filter((k,v) -> v != null);
		outData.to(transactionEval);

		KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);

		streams.start();

		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

	}

	private String callDMN(String key, String value)  {
		String responseMsg = null;
		try {

			Transaction valueMap = new Gson().fromJson(value,Transaction.class);

			KieSession kSession = kContainer.newKieSession("newrules");
			DMNRuntime dmnRuntime = kSession.getKieRuntime(DMNRuntime.class);
			String namespace = "https://kiegroup.org/dmn/_2E7CC4A2-92C4-44F8-B424-A29590D2AD0B";
			String modelName = "staticFraudRules";
			DMNModel dmnModel = dmnRuntime.getModel(namespace, modelName);
			DMNContext dmnContext = dmnRuntime.newContext();
			dmnContext.set("Transaction Type", valueMap.getTransactionType());
			dmnContext.set("Merchant Type", valueMap.getMerchantType());
			dmnContext.set("Transaction Amount", valueMap.getAmount());
			dmnContext.set("Transaction Country", valueMap.getTransactionCountry());
			//Can be replaced with JDG look up from a DB
			Account account = accountRepository.getAccount(valueMap.getAccountId()+"");
			if(null  == account) {
				account = new Account();
				account.setHomeCountry("US");
			}

			System.out.println("account home country"+account.getHomeCountry());
			dmnContext.set("Home Country", account.getHomeCountry());
			RuleModel ruleModel = null;
			DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);
			DMNDecisionResult blocked = dmnResult.getDecisionResultByName("Blocked Transactions");
			DMNDecisionResult alert = dmnResult.getDecisionResultByName("Alert Transactions");
			if(null != alert.getResult() || null != blocked.getResult()) {
				 ruleModel= new RuleModel();
				ruleModel.setFraudIdentified(new Date().getTime());
				if (null != alert.getResult()) {
					ruleModel.setFraudReason("Alert:" + alert.getResult().toString());
				}
				if (null != blocked.getResult()) {
					ruleModel.setFraudReason("Blocked:" + blocked.getResult().toString());
				}

				valueMap.setRuleModel(ruleModel);
				responseMsg = new Gson().toJson(valueMap);
			}

			return responseMsg;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	public void startStream() {
		LOG.info("Starting Stream");
		streams.start();
	}

	public void closeStream() {
		LOG.info("Stopping Stream");
		streams.close();
	}

}