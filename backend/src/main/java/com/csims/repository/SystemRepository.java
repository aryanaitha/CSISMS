package com.csims.repository;

import com.csims.model.AuditLog;
import com.csims.model.Notification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Consolidated System Repository.
 * Covers Audit Logs, Notifications, and System/Dashboard Queries.
 */
@Repository
public class SystemRepository {
    private final JdbcTemplate jdbcTemplate;

    public SystemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    // =========================================================================
    // SECTION 1: AUDIT LOGS
    // =========================================================================

    private final RowMapper<AuditLog> auditLogRowMapper = (rs, rowNum) -> {
        AuditLog a = new AuditLog();
        a.setId(rs.getLong("audit_id"));
        
        long userId = rs.getLong("user_id");
        if (!rs.wasNull()) {
            a.setUserId(userId);
        }
        
        a.setUsername(rs.getString("username"));
        a.setRole(rs.getString("role"));
        a.setAction(rs.getString("action"));
        a.setModule(rs.getString("module"));
        a.setEntityType(rs.getString("entity_type"));
        a.setEntityId(rs.getString("entity_id"));
        a.setOldValue(rs.getString("old_value"));
        a.setNewValue(rs.getString("new_value"));
        a.setStatus(rs.getString("status"));
        a.setDescription(rs.getString("description"));
        
        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) {
            a.setTimestamp(ts.toLocalDateTime());
        }
        return a;
    };

    public void saveAuditLog(AuditLog log) {
        String sql = """
            INSERT INTO audit_logs (user_id, username, role, action, module, entity_type, entity_id, old_value, new_value, status, description)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
            log.getUserId(),
            log.getUsername(),
            log.getRole(),
            log.getAction(),
            log.getModule() != null ? log.getModule() : "SYSTEM",
            log.getEntityType(),
            log.getEntityId(),
            log.getOldValue(),
            log.getNewValue(),
            log.getStatus() != null ? log.getStatus() : "SUCCESS",
            log.getDescription()
        );
    }

    public List<AuditLog> findAllAuditLogs(String action, String module, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_logs WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (action != null && !action.isBlank()) {
            sql.append("AND action = ? ");
            params.add(action);
        }
        if (module != null && !module.isBlank()) {
            sql.append("AND module = ? ");
            params.add(module);
        }

        sql.append("ORDER BY timestamp DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), auditLogRowMapper, params.toArray());
    }

    public int countAuditLogs(String action, String module) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM audit_logs WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (action != null && !action.isBlank()) {
            sql.append("AND action = ? ");
            params.add(action);
        }
        if (module != null && !module.isBlank()) {
            sql.append("AND module = ? ");
            params.add(module);
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    // =========================================================================
    // SECTION 2: NOTIFICATIONS
    // =========================================================================

    private final RowMapper<Notification> notificationRowMapper = (rs, rowNum) -> {
        Notification n = new Notification();
        n.setId(rs.getLong("notification_id"));
        n.setUserId(rs.getLong("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setReadStatus(rs.getBoolean("is_read"));
        
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            n.setCreatedAt(ts.toLocalDateTime());
        }
        return n;
    };

    public void saveNotification(Notification n) {
        String sql = """
            INSERT INTO notifications (user_id, title, message, type, is_read)
            VALUES (?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql, n.getUserId(), n.getTitle(), n.getMessage(), n.getType(), n.isReadStatus());
    }

    public List<Notification> findNotificationsByUserId(Long userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        return jdbcTemplate.query(sql, notificationRowMapper, userId);
    }

    public int countUnreadNotificationsByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    public void markNotificationAsRead(Long id, Long userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, id, userId);
    }

    public void markAllNotificationsAsRead(Long userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
}
