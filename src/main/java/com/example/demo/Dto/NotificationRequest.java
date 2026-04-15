package com.example.demo.Dto;

import com.example.demo.models.NotificationType;

public class NotificationRequest {

    private String receiverEmail;
    private String message;
    private NotificationType type;

    public NotificationRequest() {
    }

    public NotificationRequest(String receiverEmail, String message, NotificationType type) {
        this.receiverEmail = receiverEmail;
        this.message = message;
        this.type = type;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }
}