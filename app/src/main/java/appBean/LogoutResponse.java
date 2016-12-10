package appBean;

import com.fasterxml.jackson.annotation.JsonProperty;

import entities.Logged_in_user;

/**
 * Created by Owner on 5/23/2016.
 */
public class LogoutResponse {
    @JsonProperty("LogoutOpModel")
    private
    Logged_in_user user;
    @JsonProperty("statusCode")
    private
    int statusCode;
    @JsonProperty("message")
    private
    String message;

    public Logged_in_user getUser() {
        return user;
    }

    public void setUser(Logged_in_user user) {
        this.user = user;
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
