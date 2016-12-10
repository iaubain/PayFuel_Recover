package appBean;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Owner on 4/28/2016.
 */
public class DeviceResponse {
    @JsonProperty("deviceId")
    private int deviceId;
    @JsonProperty("deviceName")
    private String deviceName;
    @JsonProperty("status")
    private int status;
    @JsonProperty("branchId")
    private int branchId;
    @JsonProperty("branchName")
    private String branchName;

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
