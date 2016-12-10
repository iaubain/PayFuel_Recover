package models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Owner on 5/13/2016.
 */
public class ChoosenPumpAndNozzle {

    @JsonProperty("userId")
    private int userId;
    @JsonProperty("pumpId")
    private int pumpId;
    @JsonProperty("nozzleId")
    private int nozzleId;
    @JsonProperty("status")
    private int status;
    @JsonProperty("message")
    private String message;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPumpId() {
        return pumpId;
    }

    public void setPumpId(int pumpId) {
        this.pumpId = pumpId;
    }

    public int getNozzleId() {
        return nozzleId;
    }

    public void setNozzleId(int nozzleId) {
        this.nozzleId = nozzleId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
