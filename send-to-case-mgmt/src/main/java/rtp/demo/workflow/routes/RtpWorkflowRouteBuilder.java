package rtp.demo.workflow.routes;

import fraud.analysis.demo.transaction.fraud.analysis.Serde.TransactionDeserializer;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import rtp.demo.workflow.transformer.ParseCaseData;

@Component
public class RtpWorkflowRouteBuilder extends RouteBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(RtpWorkflowRouteBuilder.class);

	private String kafkaBootstrap = System.getenv("BOOTSTRAP_SERVERS");
	private String kafkaCreditorCompletedPaymentsTopic = System.getenv("FRAUD_TOPIC");
	private String consumerMaxPollRecords = System.getenv("CONSUMER_MAX_POLL_RECORDS");
	private String consumerCount = System.getenv("CONSUMER_COUNT");
	private String consumerSeekTo = System.getenv("CONSUMER_SEEK_TO");
	private String consumerGroup = System.getenv("CONSUMER_GROUP");
	private String bcHost = System.getenv("BC_HOST");

	@Override
	public void configure() throws Exception {
		LOG.info("Configuring Creditor Core Banking Routes");
		String startCase = "rest:post:/services/rest/server/containers/FraudAnalysisWorkflow_1.0.0-SNAPSHOT/cases/FraudAnalysisWorkflow.FraudInvestigation/instances?" +
				bcHost +
				"&produces=application/json";

		KafkaComponent kafka = new KafkaComponent();
		kafka.setBrokers(kafkaBootstrap);
		this.getContext().addComponent("kafka", kafka);

		from("kafka:" + kafkaCreditorCompletedPaymentsTopic + "?brokers=" + kafkaBootstrap + "&maxPollRecords="
				+ consumerMaxPollRecords + "&consumersCount=" + consumerCount + "&seekTo=" + consumerSeekTo
				+ "&groupId=" + consumerGroup)
						.log("\n/// Checking my glue")
				        .bean(ParseCaseData.class,"process")
				        .log("parsed message for case ${body}")
				        .to(startCase)
						.log("To Direct BC");



	}
}
