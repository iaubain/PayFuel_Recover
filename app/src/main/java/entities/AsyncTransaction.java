package entities;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by user on 4/21/2016.
 */
public class AsyncTransaction {
    @JsonIgnore
    private int id;
    @JsonProperty("userId")
    private int userId;
    @JsonProperty("deviceId")
    private String deviceId;
    @JsonProperty("branchId")
    private int branchId;
    @JsonProperty("deviceTransactionId")
    private long deviceTransactionId;
    @JsonIgnore
    private int sum;


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public long getDeviceTransactionId() {
        return deviceTransactionId;
    }

    public void setDeviceTransactionId(long deviceTransactionId) {
        this.deviceTransactionId = deviceTransactionId;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
