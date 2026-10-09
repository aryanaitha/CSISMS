package com.csims.service;

import com.csims.model.Notification;
import java.util.List;

public interface NotificationService {
    void createNotification(Integer userId, String title, String message, String type);
    void notifyTaskAssignment(int workerId, String taskTitle, String projectName);
    void notifyLowStock(String materialName, String materialCode, double currentStock, double minStock);
    void notifyMaintenanceDue(String equipmentCode, String equipmentName, String dateStr);
    void notifySafetyViolation(int projectId, String severity, String description);
    void notifyCriticalIncident(int projectId, String title, String severity);
    void notifyOverdueTask(String taskTitle, String projectName, String dueDateStr);
    List<Notification> getMyNotifications(boolean unreadOnly);
    boolean markAsRead(int notificationId);
    boolean markAllAsRead();
    int getUnreadCount();
}
