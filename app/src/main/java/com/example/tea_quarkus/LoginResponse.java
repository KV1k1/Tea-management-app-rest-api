package com.example.tea_quarkus;

public class LoginResponse {
    private boolean retval;
    private String token;

    public boolean isRetval() { return retval; }
    public void setRetval(boolean retval) { this.retval = retval; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}