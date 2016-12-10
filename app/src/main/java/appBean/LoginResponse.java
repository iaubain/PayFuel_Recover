package appBean;

import com.fasterxml.jackson.annotation.JsonProperty;

import entities.Logged_in_user;

/**
 * Created by Owner on 4/28/2016.
 */
public class LoginResponse {
        @JsonProperty("LoginOpModel")
        private Logged_in_user logged_in_user;
        @JsonProperty("message")
        private String message;
        @JsonProperty("statusCode")
        private int statusCode;

    public Logged_in_user getLogged_in_user() {
        return logged_in_user;
    }

    public void setLogged_in_user(Logged_in_user logged_in_user) {
        this.logged_in_user = logged_in_user;
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
