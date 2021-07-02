package com.dev175.chat.model;

import java.io.Serializable;

public class SpamInbox implements Serializable {

    private String receiverId;
    private String receiverProfile;
    private String receiverName;
    private long lastMessageTime;

    public SpamInbox() {
    }

    public SpamInbox(String receiverId, String receiverProfile, String receiverName, long lastMessageTime) {
        this.receiverId = receiverId;
        this.receiverProfile = receiverProfile;
        this.receiverName = receiverName;
        this.lastMessageTime = lastMessageTime;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverProfile() {
        return receiverProfile;
    }

    public void setReceiverProfile(String receiverProfile) {
        this.receiverProfile = receiverProfile;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

}
