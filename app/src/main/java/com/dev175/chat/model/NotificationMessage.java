package com.dev175.chat.model;

public class NotificationMessage {

    private String to;
    private Data data;

    public NotificationMessage(String to, Data data) {
        this.to = to;
        this.data = data;
    }
}
