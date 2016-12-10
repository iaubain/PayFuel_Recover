package appBean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import models.UrlPumps;

/**
 * Created by Owner on 5/3/2016.
 */
public class LoadPumpsResponse {
    @JsonProperty("PumpDetailsModel")
    private
    List<UrlPumps> pumps;
    @JsonProperty("message")
    private
    String message;
    @JsonProperty("statusCode")
    private
    int statusCode;

    public LoadPumpsResponse(List<UrlPumps> pumps, String message, int statusCode) {
        this.setPumps(pumps);
        this.setMessage(message);
        this.setStatusCode(statusCode);
    }

    public LoadPumpsResponse() {

    }

    public List<UrlPumps> getPumps() {
        return pumps;
    }

    public void setPumps(List<UrlPumps> pumps) {
        this.pumps = pumps;
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
