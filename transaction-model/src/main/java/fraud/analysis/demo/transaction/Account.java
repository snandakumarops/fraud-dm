package fraud.analysis.demo.transaction;

import java.io.Serializable;

public class Account implements Serializable {
    private static final long serialVersionUID = -9131494827098991910L;



    private String accountId;
    private String customerId;
    private String homeCountry;
    private String zip;
    private String excludedCountries;
    private String travelPreference;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getHomeCountry() {
        return homeCountry;
    }

    public void setHomeCountry(String homeCountry) {
        this.homeCountry = homeCountry;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getExcludedCountries() {
        return excludedCountries;
    }

    public void setExcludedCountries(String excludedCountries) {
        this.excludedCountries = excludedCountries;
    }

    public String getTravelPreference() {
        return travelPreference;
    }

    public void setTravelPreference(String travelPreference) {
        this.travelPreference = travelPreference;
    }
}
