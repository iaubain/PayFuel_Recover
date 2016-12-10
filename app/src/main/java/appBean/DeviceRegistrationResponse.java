package appBean;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Owner on 4/28/2016.
 */
public class DeviceRegistrationResponse {
    @JsonProperty("DeviceRegistrationResponse")
    private DeviceResponse devdata;
    @JsonProperty("message")
    private String message;
    @JsonProperty("statusCode")
    private int statusCode;

    public DeviceResponse getDevdata() {
        return devdata;
    }

    public void setDevdata(DeviceResponse devdata) {
        this.devdata = devdata;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
