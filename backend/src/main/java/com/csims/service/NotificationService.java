package com.csims.service;

import com.csims.model.Notification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final SystemService systemService;

    public NotificationService(SystemService systemService) {
        this.systemService = systemService;
    }

    public void notify(Long userId, String title, String message, String type, String link) {
        systemService.notify(userId, title, message, type, link);
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return systemService.getNotificationsForUser(userId);
    }

    public int getUnreadCount(Long userId) {
        return systemService.getUnreadCount(userId);
    }

    public void markAsRead(Long id, Long userId) {
        systemService.markNotificationAsRead(id, userId);
    }

    public void markAllAsRead(Long userId) {
        systemService.markAllNotificationsAsRead(userId);
    }
}
