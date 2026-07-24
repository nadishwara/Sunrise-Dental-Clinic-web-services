package com.sunrisedental.model;

public class User {
    private int userId;
    private String username;
    private String email;
    private String password;
    private String role;
    private int staffId;
    private String customId;

    public User() {}

//    user registration

    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(int userId, String username, String email, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public int getUserId() {return userId;}
    public void setUserId(int userId) {this.userId = userId;}

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}

    public int getStaffId() {return staffId;}
    public void setStaffId(int staffId) {this.staffId =staffId;}

    public String getCustomId() {return customId;}
    public void setCustomId(String customId) { this.customId=customId;}
}
