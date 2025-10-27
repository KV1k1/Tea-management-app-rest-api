package com.example.tea_quarkus;

public class RegisterRequest {
    private String loginName;
    private String displayName;
    private String password;
    private boolean isPublic;

    public RegisterRequest(String loginName, String displayName, String password, boolean isPublic) {
        this.loginName = loginName;
        this.displayName = displayName;
        this.password = password;
        this.isPublic = isPublic;
    }

    // Getters and setters
    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
}