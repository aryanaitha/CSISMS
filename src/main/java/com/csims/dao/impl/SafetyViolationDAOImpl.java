package com.csims.dao.impl;

import com.csims.dao.SafetyViolationDAO;
import com.csims.model.SafetyViolation;
import com.csims.util.DBConnection;
import com.csims.util.DateUtil;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class SafetyViolationDAOImpl implements SafetyViolationDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private SafetyViolation mapRow(ResultSet rs) throws SQLException {
        SafetyViolation sv = new SafetyViolation();
        sv.setViolationId(rs.getInt("violation_id"));

        int insId = rs.getInt("inspection_id");
        if (!rs.wasNull()) sv.setInspectionId(insId);

        sv.setProjectId(rs.getInt("project_id"));

        int wId = rs.getInt("worker_id");
        if (!rs.wasNull()) sv.setWorkerId(wId);

        sv.setSeverity(rs.getString("severity"));
        sv.setDescription(rs.getString("description"));
        sv.setCorrectiveAction(rs.getString("corrective_action"));
        sv.setStatus(rs.getString("status"));

        Date rDate = rs.getDate("resolution_date");
        if (rDate != null) sv.setResolutionDate(rDate.toLocalDate());

        int repBy = rs.getInt("reported_by");
        if (!rs.wasNull()) sv.setReportedBy(repBy);

        Timestamp cTs = rs.getTimestamp("created_at");
        if (cTs != null) sv.setCreatedAt(cTs.toLocalDateTime());

        try {
            sv.setProjectName(rs.getString("project_name"));
        } catch (SQLException ignored) {}

        try {
            sv.setWorkerName(rs.getString("worker_name"));
        } catch (SQLException ignored) {}

        try {
            sv.setReporterName(rs.getString("reporter_name"));
        } catch (SQLException ignored) {}

        return sv;
    }

    @Override
    public Optional<SafetyViolation> findById(int violationId) {
        String sql = "SELECT sv.*, p.project_name, w.full_name as worker_name, u.username as reporter_name " +
                     "FROM safety_violations sv " +
                     "JOIN projects p ON sv.project_id = p.project_id " +
                     "LEFT JOIN workers w ON sv.worker_id = w.worker_id " +
                     "LEFT JOIN users u ON sv.reported_by = u.user_id " +
                     "WHERE sv.violation_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, violationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding violation: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<SafetyViolation> findAll() {
        return search(null, null, null, null);
    }

    @Override
    public List<SafetyViolation> search(Integer projectId, Integer workerId, String severity, String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT sv.*, p.project_name, w.full_name as worker_name, u.username as reporter_name " +
            "FROM safety_violations sv " +
            "JOIN projects p ON sv.project_id = p.project_id " +
            "LEFT JOIN workers w ON sv.worker_id = w.worker_id " +
            "LEFT JOIN users u ON sv.reported_by = u.user_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (projectId != null && projectId > 0) {
            sql.append("AND sv.project_id = ? ");
            params.add(projectId);
        }

        if (workerId != null && workerId > 0) {
            sql.append("AND sv.worker_id = ? ");
            params.add(workerId);
        }

        if (severity != null && !severity.trim().isEmpty() && !severity.equalsIgnoreCase("ALL")) {
            sql.append("AND sv.severity = ? ");
            params.add(severity);
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND sv.status = ? ");
            params.add(status);
        }

        sql.append("ORDER BY sv.violation_id DESC");

        List<SafetyViolation> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching violations: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int create(SafetyViolation violation) {
        String sql = "INSERT INTO safety_violations (inspection_id, project_id, worker_id, severity, description, " +
                     "corrective_action, status, resolution_date, reported_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (violation.getInspectionId() != null) {
                ps.setInt(1, violation.getInspectionId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, violation.getProjectId());
            if (violation.getWorkerId() != null) {
                ps.setInt(3, violation.getWorkerId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, violation.getSeverity() != null ? violation.getSeverity() : "Medium");
            ps.setString(5, violation.getDescription());
            ps.setString(6, violation.getCorrectiveAction());
            ps.setString(7, violation.getStatus() != null ? violation.getStatus() : "OPEN");
            ps.setDate(8, DateUtil.toSqlDate(violation.getResolutionDate()));
            if (violation.getReportedBy() != null) {
                ps.setInt(9, violation.getReportedBy());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    violation.setViolationId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating safety violation: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(SafetyViolation violation) {
        String sql = "UPDATE safety_violations SET inspection_id = ?, project_id = ?, worker_id = ?, severity = ?, " +
                     "description = ?, corrective_action = ?, status = ?, resolution_date = ? WHERE violation_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (violation.getInspectionId() != null) {
                ps.setInt(1, violation.getInspectionId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, violation.getProjectId());
            if (violation.getWorkerId() != null) {
                ps.setInt(3, violation.getWorkerId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, violation.getSeverity());
            ps.setString(5, violation.getDescription());
            ps.setString(6, violation.getCorrectiveAction());
            ps.setString(7, violation.getStatus());
            ps.setDate(8, DateUtil.toSqlDate(violation.getResolutionDate()));
            ps.setInt(9, violation.getViolationId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating violation: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateResolution(int violationId, String status, String correctiveAction, LocalDate resolutionDate) {
        String sql = "UPDATE safety_violations SET status = ?, corrective_action = ?, resolution_date = ? WHERE violation_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, correctiveAction);
            ps.setDate(3, DateUtil.toSqlDate(resolutionDate));
            ps.setInt(4, violationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating violation resolution: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int violationId) {
        String sql = "DELETE FROM safety_violations WHERE violation_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, violationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting violation: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Integer> countBySeverity() {
        String sql = "SELECT severity, COUNT(*) as cnt FROM safety_violations GROUP BY severity";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("severity"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting violations by severity: " + e.getMessage(), e);
        }
        return map;
    }

    @Override
    public int countOpen() {
        String sql = "SELECT COUNT(*) FROM safety_violations WHERE status IN ('OPEN', 'IN_PROGRESS')";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting open violations: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM safety_violations";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting total violations: " + e.getMessage(), e);
        }
        return 0;
    }
}
