package rtp.demo.creditor.repository.account;

import fraud.analysis.demo.transaction.Account;
import org.infinispan.commons.api.BasicCache;



public class JdgAccountRepository implements AccountRepository {

	private static final String JDG_CACHE_NAME = "accountCache";

	private CacheContainerProvider containerProvider = new CacheContainerProvider();
	private BasicCache<String, Object> accountCache;

	public JdgAccountRepository() {
		accountCache = containerProvider.getBasicCacheContainer().getCache(JDG_CACHE_NAME);
	}

	@Override
	public void addAccount(Account account) {
		accountCache.put(AccountManager.encode(account.getAccountId()), account);
	}

	@Override
	public Account getAccount(String accountNumber) {
		Account retrievedAccount = (Account) accountCache.get(AccountManager.encode(accountNumber));
		return retrievedAccount;
	}

	@Override
	public void deleteAccount(String accountNumber) {
		accountCache.remove(accountNumber);
	}

}
