package appBean;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Owner on 4/27/2016.
 */
public class Login {
    @JsonProperty("deviceId")
    private String deviceId;
    @JsonProperty("userPin")
    private String userPin;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserPin() {
        return userPin;
    }

    public void setUserPin(String userPin) {
        this.userPin = userPin;
    }
}
