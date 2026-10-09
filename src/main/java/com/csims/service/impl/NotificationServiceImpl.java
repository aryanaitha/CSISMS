package com.csims.service.impl;

import com.csims.dao.NotificationDAO;
import com.csims.dao.impl.NotificationDAOImpl;
import com.csims.model.Notification;
import com.csims.model.User;
import com.csims.security.SessionContext;
import com.csims.service.NotificationService;

import java.util.List;

public class NotificationServiceImpl implements NotificationService {

    private final NotificationDAO notificationDAO = new NotificationDAOImpl();

    @Override
    public void createNotification(Integer userId, String title, String message, String type) {
        Notification n = new Notification(userId, title, message, type);
        notificationDAO.create(n);
    }

    @Override
    public void notifyTaskAssignment(int workerId, String taskTitle, String projectName) {
        createNotification(null, "Task Assigned: " + taskTitle,
            "A task has been assigned in project '" + projectName + "'.", "TASK_ASSIGNED");
    }

    @Override
    public void notifyLowStock(String materialName, String materialCode, double currentStock, double minStock) {
        createNotification(null, "Low Stock Warning: " + materialName,
            "Material '" + materialCode + "' stock (" + currentStock + ") is at or below minimum threshold (" + minStock + ").", "LOW_STOCK");
    }

    @Override
    public void notifyMaintenanceDue(String equipmentCode, String equipmentName, String dateStr) {
        createNotification(null, "Maintenance Due: " + equipmentName,
            "Equipment [" + equipmentCode + "] service is due on " + dateStr + ".", "MAINTENANCE_DUE");
    }

    @Override
    public void notifySafetyViolation(int projectId, String severity, String description) {
        createNotification(null, "Safety Violation (" + severity + ")",
            "A " + severity + " severity violation was recorded on project #" + projectId + ": " + description, "SAFETY_VIOLATION");
    }

    @Override
    public void notifyCriticalIncident(int projectId, String title, String severity) {
        createNotification(null, "Incident Reported: " + title,
            "A " + severity + " severity incident was logged on project #" + projectId + ".", "INCIDENT_ALERT");
    }

    @Override
    public void notifyOverdueTask(String taskTitle, String projectName, String dueDateStr) {
        createNotification(null, "Overdue Task: " + taskTitle,
            "Task in project '" + projectName + "' was due on " + dueDateStr + " and remains incomplete.", "TASK_OVERDUE");
    }

    @Override
    public List<Notification> getMyNotifications(boolean unreadOnly) {
        User user = SessionContext.getInstance().getCurrentUser();
        Integer userId = user != null ? user.getUserId() : null;
        return notificationDAO.findByUserId(userId, unreadOnly);
    }

    @Override
    public boolean markAsRead(int notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }

    @Override
    public boolean markAllAsRead() {
        User user = SessionContext.getInstance().getCurrentUser();
        Integer userId = user != null ? user.getUserId() : null;
        return notificationDAO.markAllAsRead(userId);
    }

    @Override
    public int getUnreadCount() {
        User user = SessionContext.getInstance().getCurrentUser();
        Integer userId = user != null ? user.getUserId() : null;
        return notificationDAO.countUnread(userId);
    }
}
