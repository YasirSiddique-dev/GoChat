package com.dev175.chat.model;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String profileImg;
    private String fullName;
    private String email;
    private String aboutMe;
    private String phone;
    private String token;



    public User() {
    }

    public User(String profileImg, String fullName, String email, String aboutMe, String phone, String token) {
        this.profileImg = profileImg;
        this.fullName = fullName;
        this.email = email;
        this.aboutMe = aboutMe;
        this.phone = phone;
        this.token = token;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
