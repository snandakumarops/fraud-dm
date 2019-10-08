package rtp.demo.creditor.repository.account;


import fraud.analysis.demo.transaction.Account;

public interface AccountRepository {

	public void addAccount(Account account);

	public Account getAccount(String accountNumber);

	public void deleteAccount(String accountNumber);

}
