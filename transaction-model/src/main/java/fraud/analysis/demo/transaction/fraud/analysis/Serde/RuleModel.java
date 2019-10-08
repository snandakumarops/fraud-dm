package fraud.analysis.demo.transaction.fraud.analysis.Serde;

public class RuleModel {

    private String fraudReason;
    private long fraudIdentified;
    private long timeToStaticFraudDetection;


    public String getFraudReason() {
        return fraudReason;
    }

    public void setFraudReason(String fraudReason) {
        this.fraudReason = fraudReason;
    }

    public long getFraudIdentified() {
        return fraudIdentified;
    }

    public void setFraudIdentified(long fraudIdentified) {
        this.fraudIdentified = fraudIdentified;
    }

    public long getTimeToStaticFraudDetection() {
        return timeToStaticFraudDetection;
    }

    public void setTimeToStaticFraudDetection(long timeToStaticFraudDetection) {
        this.timeToStaticFraudDetection = timeToStaticFraudDetection;
    }
}
