package appBean;

import com.fasterxml.jackson.annotation.JsonProperty;

import entities.SellingTransaction;

/**
 * Created by Owner on 5/31/2016.
 */
public class TransactionResponse {

    @JsonProperty("SaleDetailsModel")
    private SellingTransaction sellingTransaction;
    @JsonProperty("statusCode")
    private int statusCode;
    @JsonProperty("message")
    private String message;

    public SellingTransaction getSellingTransaction() {
        return sellingTransaction;
    }

    public void setSellingTransaction(SellingTransaction sellingTransaction) {
        this.sellingTransaction = sellingTransaction;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
