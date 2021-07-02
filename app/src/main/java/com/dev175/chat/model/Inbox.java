package com.dev175.chat.model;

import java.io.Serializable;

public class Inbox implements Serializable {

    private String receiverId;
    private String receiverProfile;
    private String receiverName;
    private String lastMessage;
    private long lastMessageTime;

    public Inbox() {
    }

    public Inbox(String receiverId, String receiverProfile, String receiverName, String lastMessage, long lastMessageTime) {
        this.receiverId = receiverId;
        this.receiverProfile = receiverProfile;
        this.receiverName = receiverName;
        this.lastMessage = lastMessage;
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

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}
