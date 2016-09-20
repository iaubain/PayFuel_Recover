package appBean;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

import entities.PaymentMode;

/**
 * Created by Owner on 5/3/2016.
 */
public class PaymentModeResponse {
    @JsonProperty("PaymentMode")
    private List<PaymentMode> paymentModeList;
    @JsonProperty("message")
    private String message;
    @JsonProperty("statusCode")
    private int statusCode;

    public List<PaymentMode> getPaymentModeList() {
        return paymentModeList;
    }

    public void setPaymentModeList(List<PaymentMode> paymentModeList) {
        this.paymentModeList = paymentModeList;
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
