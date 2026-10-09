package com.frauddetection.model;

public class User {

    private int userId;
    private String username;
    private String email;
    private String usualLocation;
    private String password; // plain text for this project only — a real app must hash it

    public User(String username, String email, String usualLocation, String password) {
        this.username = username;
        this.email = email;
        this.usualLocation = usualLocation;
        this.password = password;
    }

    public User(int userId, String username, String email, String usualLocation, String password) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.usualLocation = usualLocation;
        this.password = password;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsualLocation() { return usualLocation; }
    public void setUsualLocation(String usualLocation) { this.usualLocation = usualLocation; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return username + " (" + email + ")";
    }
}
