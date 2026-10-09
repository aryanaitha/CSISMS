package com.csims.dao;

import com.csims.model.Notification;
import java.util.List;

public interface NotificationDAO {
    int create(Notification notification);
    List<Notification> findByUserId(Integer userId, boolean unreadOnly);
    boolean markAsRead(int notificationId);
    boolean markAllAsRead(Integer userId);
    int countUnread(Integer userId);
}
