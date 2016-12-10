package appBean;

import com.fasterxml.jackson.annotation.JsonProperty;

import entities.AsyncTransaction;

/**
 * Created by Owner on 6/21/2016.
 */
public class AsyncResponce {
    @JsonProperty("message")
    private
    String message;
    @JsonProperty("statusCode")
    private
    int stastusCode;
    @JsonProperty("AsyncTransaction")
    private
    AsyncTransaction asyncTransaction;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStastusCode() {
        return stastusCode;
    }

    public void setStastusCode(int stastusCode) {
        this.stastusCode = stastusCode;
    }

    public AsyncTransaction getAsyncTransaction() {
        return asyncTransaction;
    }

    public void setAsyncTransaction(AsyncTransaction asyncTransaction) {
        this.asyncTransaction = asyncTransaction;
    }
}
