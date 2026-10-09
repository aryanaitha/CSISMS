package com.csims.dao.impl;

import com.csims.dao.NotificationDAO;
import com.csims.model.Notification;
import com.csims.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAOImpl implements NotificationDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notification_id"));

        int uId = rs.getInt("user_id");
        if (!rs.wasNull()) n.setUserId(uId);

        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setRead(rs.getBoolean("is_read"));

        Timestamp cTs = rs.getTimestamp("created_at");
        if (cTs != null) n.setCreatedAt(cTs.toLocalDateTime());

        return n;
    }

    @Override
    public int create(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, title, message, type, is_read) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (notification.getUserId() != null) {
                ps.setInt(1, notification.getUserId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, notification.getTitle());
            ps.setString(3, notification.getMessage());
            ps.setString(4, notification.getType());
            ps.setBoolean(5, notification.isRead());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    notification.setNotificationId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating notification: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public List<Notification> findByUserId(Integer userId, boolean unreadOnly) {
        StringBuilder sql = new StringBuilder("SELECT * FROM notifications WHERE (user_id = ? OR user_id IS NULL) ");
        if (unreadOnly) {
            sql.append("AND is_read = FALSE ");
        }
        sql.append("ORDER BY created_at DESC LIMIT 100");

        List<Notification> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (userId != null) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading notifications: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error marking notification read: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean markAllAsRead(Integer userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? OR user_id IS NULL";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error marking all read: " + e.getMessage(), e);
        }
    }

    @Override
    public int countUnread(Integer userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE (user_id = ? OR user_id IS NULL) AND is_read = FALSE";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting unread notifications: " + e.getMessage(), e);
        }
        return 0;
    }
}
