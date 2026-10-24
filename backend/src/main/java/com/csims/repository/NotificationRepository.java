package com.csims.repository;

import com.csims.model.Notification;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NotificationRepository {
    private final SystemRepository systemRepository;

    public NotificationRepository(SystemRepository systemRepository) {
        this.systemRepository = systemRepository;
    }

    public void save(Notification n) {
        systemRepository.saveNotification(n);
    }

    public List<Notification> findByUserId(Long userId) {
        return systemRepository.findNotificationsByUserId(userId);
    }

    public int countUnreadByUserId(Long userId) {
        return systemRepository.countUnreadNotificationsByUserId(userId);
    }

    public void markAsRead(Long id, Long userId) {
        systemRepository.markNotificationAsRead(id, userId);
    }

    public void markAllAsRead(Long userId) {
        systemRepository.markAllNotificationsAsRead(userId);
    }
}
