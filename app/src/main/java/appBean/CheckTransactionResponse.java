package appBean;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

import entities.SellingTransaction;
import models.TransactionToCheck;

/**
 * Created by Owner on 6/24/2016.
 */
public class CheckTransactionResponse {
    @JsonProperty("Transaction")
    private List<TransactionToCheck> transactions;
    @JsonProperty("message")
    private String message;
    @JsonProperty("statusCode")
    private int statusCode;

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

    public List<TransactionToCheck> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionToCheck> transactions) {
        this.transactions = transactions;
    }
}
