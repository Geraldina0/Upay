package com.example.demo.Dto;

import com.example.demo.models.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationResponse {

    private UUID id;
    private String message;
    private NotificationType type;
    private LocalDateTime createdAt;
    private boolean read;
    private String userName;
    private long unreadCount;

    public NotificationResponse() {
    }

    public NotificationResponse(UUID id, String message, NotificationType type,
                                LocalDateTime createdAt, boolean read) {
        this.id = id;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
        this.read = read;
    }

    public NotificationResponse(UUID id, String message, NotificationType type,
                                LocalDateTime createdAt, boolean read,
                                String userName, long unreadCount) {
        this.id = id;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
        this.read = read;
        this.userName = userName;
        this.unreadCount = unreadCount;
    }

    public UUID getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public String getUserName() {
        return userName;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }
}