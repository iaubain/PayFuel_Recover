package models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Owner on 5/23/2016.
 */
public class LogoutData {

    @JsonProperty("deviceId")
    private String devId;
    @JsonProperty("userId")
    private int userId;

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
