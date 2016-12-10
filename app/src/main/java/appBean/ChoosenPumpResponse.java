package appBean;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import models.ChoosenPumpAndNozzle;

/**
 * Created by Owner on 5/13/2016.
 */
public class ChoosenPumpResponse {
    @JsonProperty("AssignedPumpModel")
    private List<ChoosenPumpAndNozzle> assignPumpModel;
    @JsonProperty("message")
    private String message;
    @JsonProperty("statusCode")
    private int statusCode;

    public List<ChoosenPumpAndNozzle> getAssignPumpModel() {
        return assignPumpModel;
    }

    public void setAssignPumpModel(List<ChoosenPumpAndNozzle> assignPumpModel) {
        this.assignPumpModel = assignPumpModel;
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
