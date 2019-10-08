package fraud.analysis.demo.service;

import fraud.analysis.demo.producer.EmitterConfig;
import fraud.analysis.demo.producer.EmitterProducer;
import fraud.analysis.demo.transaction.Transaction;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class EventEmitterService extends AbstractVerticle {

	private static final Logger LOGGER = Logger.getLogger(EventEmitterService.class.getName());


	@Override
	public void start() {
		Router router = Router.router(vertx);
		System.out.println("inside Start");
		router.route().handler(BodyHandler.create());

		router.post("/emitter-service/transaction").handler(this::createEvents);

		router.get("/*").handler(StaticHandler.create());

		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
		LOGGER.info("THE HTTP APPLICATION HAS STARTED");



	}

    private void createEvents(RoutingContext routingContext) {

	    LOGGER.info("inside the create events method");
        Transaction transaction = routingContext.getBodyAsJson().mapTo(Transaction.class);
        LOGGER.info("Creating payments: " + transaction);

        EmitterProducer emitterProducer = new EmitterProducer();
        EmitterConfig emitterConfig = new EmitterConfig(System.getenv("BOOTSTRAP_SERVERS"),"txn-by-cust"
                ,System.getenv("SECURITY_PROTOCOL"),System.getenv("SERIALIZER_CLASS"),System.getenv("ACKS"));
        EmitterProducer paritionByCustomer = new EmitterProducer();
        paritionByCustomer.setConfig(emitterConfig);





        try {
                // Payment key generated based on timestamp for the reference example

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
                    LocalDateTime now = LocalDateTime.now();
                    transaction.setAccountId(34234);
                    if (null == transaction.getCustomerId()) {
                        transaction.setCustomerId("CUST8089");
                    }
                    transaction.setTransactionType(transaction.getTransactionType());
                    transaction.setTransactionId("TXN" + formatter.format(now));
                    transaction.setTimestamp(now.toEpochSecond(ZoneOffset.UTC));


                    emitterProducer.sendMessage(transaction.getCustomerId(), transaction);
                    paritionByCustomer.sendMessage(transaction.getCustomerId(), transaction);



            } catch (Exception e) {
                LOGGER.severe("Error publishing payment to topic");
                LOGGER.severe(e.getMessage());
                e.printStackTrace();
            }




        HttpServerResponse response = routingContext.response();
        response.putHeader(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=utf-8");
        response.putHeader("Access-Control-Allow-Origin", "*");

        response.end(Json.encode(transaction));
    }





}
