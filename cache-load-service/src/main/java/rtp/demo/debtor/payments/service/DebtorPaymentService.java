package rtp.demo.debtor.payments.service;

import fraud.analysis.demo.transaction.Account;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import rtp.demo.creditor.repository.account.AccountRepository;
import rtp.demo.creditor.repository.account.JdgAccountRepository;



import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

public class DebtorPaymentService extends AbstractVerticle {

	private static final Logger LOGGER = Logger.getLogger(DebtorPaymentService.class.getName());

	private AccountRepository accountRepository = new JdgAccountRepository();


	@Override
	public void start() {
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());



		router.get("/*").handler(StaticHandler.create());

		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
		LOGGER.info("THE HTTP APPLICATION HAS STARTED");

		// Populate test payees in the cache, for purposes of the reference example
		Account testAccount1 = new Account();
		testAccount1.setAccountId("34234");
		testAccount1.setCustomerId("CUST8089");
        testAccount1.setHomeCountry("US");
		accountRepository.addAccount(testAccount1);

		Account testAccount2 = new Account();
		testAccount2.setAccountId("34235");
		testAccount2.setCustomerId("CUST8089");
        testAccount1.setHomeCountry("US");
		accountRepository.addAccount(testAccount2);

		Account testAccount3 = new Account();
		testAccount3.setAccountId("34236");
		testAccount3.setCustomerId("CUST8089");
        testAccount1.setHomeCountry("SD");
		accountRepository.addAccount(testAccount3);

		Account testAccount4 = new Account();
		testAccount4.setAccountId("34239");
		testAccount4.setCustomerId("CUST8089");
        testAccount1.setHomeCountry("US");
		accountRepository.addAccount(testAccount4);

		Account testAccount5 = new Account();
		testAccount5.setAccountId("34297");
		testAccount5.setCustomerId("CUST8089");
        testAccount1.setHomeCountry("IR");
		accountRepository.addAccount(testAccount5);

		LOGGER.info("JDG CACHE: " + accountRepository.toString());

		Account retrievedAccount = accountRepository.getAccount("walterlaw@company.com");

		LOGGER.info("RETRIEVED ACCT: " + retrievedAccount);

	}


}
