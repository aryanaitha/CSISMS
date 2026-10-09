package com.csims.dao.impl;

import com.csims.dao.AuditLogDAO;
import com.csims.model.AuditLog;
import com.csims.util.DBConnection;
import com.csims.util.DateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of AuditLogDAO. Strictly provides insert and read queries.
 */
public class AuditLogDAOImpl implements AuditLogDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setAuditId(rs.getInt("audit_id"));

        int uId = rs.getInt("user_id");
        if (!rs.wasNull()) log.setUserId(uId);

        log.setUsername(rs.getString("username"));
        log.setRole(rs.getString("role"));
        log.setAction(rs.getString("action"));
        log.setModule(rs.getString("module"));
        log.setEntityType(rs.getString("entity_type"));
        log.setEntityId(rs.getString("entity_id"));
        log.setOldValue(rs.getString("old_value"));
        log.setNewValue(rs.getString("new_value"));

        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) log.setTimestamp(ts.toLocalDateTime());

        log.setStatus(rs.getString("status"));
        log.setDescription(rs.getString("description"));

        return log;
    }

    @Override
    public int insert(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_id, username, role, action, module, entity_type, " +
                     "entity_id, old_value, new_value, status, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getUserId() != null) {
                ps.setInt(1, log.getUserId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, log.getUsername());
            ps.setString(3, log.getRole());
            ps.setString(4, log.getAction());
            ps.setString(5, log.getModule());
            ps.setString(6, log.getEntityType());
            ps.setString(7, log.getEntityId());
            ps.setString(8, log.getOldValue());
            ps.setString(9, log.getNewValue());
            ps.setString(10, log.getStatus() != null ? log.getStatus() : "SUCCESS");
            ps.setString(11, log.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    log.setAuditId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            // Note: Audit logging should never crash transaction if DB is unstable, but log the error
            System.err.println("CRITICAL: Failed to write audit log: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public List<AuditLog> findAll() {
        return search(null, null, null, null, null, null);
    }

    @Override
    public List<AuditLog> search(String username, String action, String module, LocalDate startDate, LocalDate endDate, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_logs WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (username != null && !username.trim().isEmpty()) {
            sql.append("AND LOWER(username) LIKE ? ");
            params.add("%" + username.trim().toLowerCase() + "%");
        }

        if (action != null && !action.trim().isEmpty() && !action.equalsIgnoreCase("ALL")) {
            sql.append("AND LOWER(action) LIKE ? ");
            params.add("%" + action.trim().toLowerCase() + "%");
        }

        if (module != null && !module.trim().isEmpty() && !module.equalsIgnoreCase("ALL")) {
            sql.append("AND module = ? ");
            params.add(module);
        }

        if (startDate != null) {
            sql.append("AND timestamp >= ? ");
            params.add(DateUtil.toSqlTimestamp(startDate.atStartOfDay()));
        }

        if (endDate != null) {
            sql.append("AND timestamp <= ? ");
            params.add(DateUtil.toSqlTimestamp(endDate.atTime(23, 59, 59)));
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND status = ? ");
            params.add(status);
        }

        sql.append("ORDER BY audit_id DESC LIMIT 500");

        List<AuditLog> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching audit logs: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM audit_logs";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting audit logs: " + e.getMessage(), e);
        }
        return 0;
    }
}
