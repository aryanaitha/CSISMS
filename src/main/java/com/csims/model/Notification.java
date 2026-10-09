package com.csims.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * System notification entity.
 */
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    private int notificationId;
    private Integer userId;
    private String title;
    private String message;
    private String type; // LOW_STOCK, MAINTENANCE_DUE, SAFETY_VIOLATION, TASK_UPDATE, etc.
    private boolean read = false;
    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(Integer userId, String title, String message, String type) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = false;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "[" + type + "] " + title;
    }
}
