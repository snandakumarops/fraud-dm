package fraud.analysis.demo.transaction;

import fraud.analysis.demo.transaction.fraud.analysis.Serde.RuleModel;

/**
 * This class was automatically generated by the data modeler tool.
 */


public class Transaction implements java.io.Serializable {



	static final long serialVersionUID = 1L;

	private java.lang.String transactionId;
	private java.lang.Integer accountId;
	private java.lang.String customerId;
	private java.lang.Double amount;
	private java.lang.String transactionType;
	private java.lang.String merchantType;
	private String transactionCountry;
	private RuleModel ruleModel;

	private String event;

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public RuleModel getRuleModel() {
		return ruleModel;
	}

	public void setRuleModel(RuleModel ruleModel) {
		this.ruleModel = ruleModel;
	}

	public String getTransactionCountry() {
		return transactionCountry;
	}

	public void setTransactionCountry(String transactionCountry) {
		this.transactionCountry = transactionCountry;
	}


	private java.lang.Long timestamp;

	public Transaction() {
	}

	public java.lang.String getTransactionId() {
		return this.transactionId;
	}

	public void setTransactionId(java.lang.String transactionId) {
		this.transactionId = transactionId;
	}

	public java.lang.Integer getAccountId() {
		return this.accountId;
	}

	public void setAccountId(java.lang.Integer accountId) {
		this.accountId = accountId;
	}

	public java.lang.String getCustomerId() {
		return this.customerId;
	}

	public void setCustomerId(java.lang.String customerId) {
		this.customerId = customerId;
	}

	public java.lang.Double getAmount() {
		return this.amount;
	}

	public void setAmount(java.lang.Double amount) {
		this.amount = amount;
	}

	public java.lang.String getTransactionType() {
		return this.transactionType;
	}

	public void setTransactionType(java.lang.String transactionType) {
		this.transactionType = transactionType;
	}

	public java.lang.String getMerchantType() {
		return this.merchantType;
	}

	public void setMerchantType(java.lang.String merchantType) {
		this.merchantType = merchantType;
	}

	public java.lang.Long getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(java.lang.Long timestamp) {
		this.timestamp = timestamp;
	}

	public Transaction(java.lang.String transactionId,
					   java.lang.Integer accountId, java.lang.String customerId,
					   java.lang.Double amount, java.lang.String transactionType,
					   java.lang.String merchantType, java.lang.Long timestamp) {
		this.transactionId = transactionId;
		this.accountId = accountId;
		this.customerId = customerId;
		this.amount = amount;
		this.transactionType = transactionType;
		this.merchantType = merchantType;
		this.timestamp = timestamp;
	}

}