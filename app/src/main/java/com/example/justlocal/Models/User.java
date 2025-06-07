package com.example.justlocal.Models;

import java.io.Serializable;

public class User implements Serializable {

    private String id;
    private String fullName;
    private String email;
    private String password;
    private String status;
    private String phone;
    private String role;
    private long joinDate;
    private long lastActive;
    private String avatarUrl;


    //required empty constructor for Firebase
    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    //Full constructor
    public User(String id, String fullName, String email, String password, String status, String phone, String role, long joinDate, long lastActive, String avatarUrl) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.status = status;
        this.phone = phone;
        this.role = role;
        this.joinDate = joinDate;
        this.lastActive = lastActive;
        this.avatarUrl = avatarUrl;
    }


    //Getters and setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(long joinDate) {
        this.joinDate = joinDate;
    }

    public long getLastActive() {
        return lastActive;
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
