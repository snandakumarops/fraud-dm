package fraud.analysis.demo.transaction;

public class CEPFraud {

    private Transaction transaction;
    private String fraudReason;

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getFraudReason() {
        return fraudReason;
    }

    public void setFraudReason(String fraudReason) {
        this.fraudReason = fraudReason;
    }
}
