package com.csims.repository;

import com.csims.model.AuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AuditLogRepository {
    private final JdbcTemplate jdbcTemplate;

    public AuditLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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

    public void save(AuditLog log) {
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

    public List<AuditLog> findAll(String action, String module, int limit, int offset) {
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

    public int count(String action, String module) {
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
}
