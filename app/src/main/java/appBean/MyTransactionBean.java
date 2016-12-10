package appBean;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Owner on 6/24/2016.
 */
public class MyTransactionBean {
    @JsonProperty("userId")
    private
    long userId;
    @JsonProperty("deviceTransactionDate")
    private
    String deviceTransactionDate;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getDeviceTransactionDate() {
        return deviceTransactionDate;
    }

    public void setDeviceTransactionDate(String deviceTransactionDate) {
        this.deviceTransactionDate = deviceTransactionDate;
    }
}
