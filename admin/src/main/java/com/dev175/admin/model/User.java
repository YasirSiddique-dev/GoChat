package com.dev175.admin.model;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String profileImg;
    private String fullName;
    private String email;
    private String password;
    private String phone;


    public User() {
    }

    public User(String profileImg, String fullName, String email, String password, String phone) {
        this.profileImg = profileImg;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }
}
