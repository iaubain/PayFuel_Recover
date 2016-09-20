package com.aub.oltranz.payfuel.spadmin.beans;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by Owner on 7/5/2016.
 */
public class AdminLoginBean {
    @JsonProperty("email")
    private String email;
    @JsonProperty("password")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
