package com.csims.dao.impl;

import com.csims.dao.SafetyInspectionDAO;
import com.csims.model.SafetyInspection;
import com.csims.util.DBConnection;
import com.csims.util.DateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SafetyInspectionDAOImpl implements SafetyInspectionDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private SafetyInspection mapRow(ResultSet rs) throws SQLException {
        SafetyInspection si = new SafetyInspection();
        si.setInspectionId(rs.getInt("inspection_id"));
        si.setProjectId(rs.getInt("project_id"));
        si.setInspectorId(rs.getInt("inspector_id"));

        Date iDate = rs.getDate("inspection_date");
        if (iDate != null) si.setInspectionDate(iDate.toLocalDate());

        si.setPpeScore(rs.getInt("ppe_score"));
        si.setWorkerSafetyScore(rs.getInt("worker_safety_score"));
        si.setEquipmentScore(rs.getInt("equipment_score"));
        si.setWorkAreaScore(rs.getInt("work_area_score"));
        si.setEmergencyScore(rs.getInt("emergency_score"));
        si.setOverallScore(rs.getBigDecimal("overall_score"));
        si.setRemarks(rs.getString("remarks"));

        Timestamp cTs = rs.getTimestamp("created_at");
        if (cTs != null) si.setCreatedAt(cTs.toLocalDateTime());

        try {
            si.setProjectName(rs.getString("project_name"));
        } catch (SQLException ignored) {}

        try {
            si.setInspectorName(rs.getString("inspector_name"));
        } catch (SQLException ignored) {}

        return si;
    }

    @Override
    public Optional<SafetyInspection> findById(int inspectionId) {
        String sql = "SELECT si.*, p.project_name, u.full_name as inspector_name FROM safety_inspections si " +
                     "JOIN projects p ON si.project_id = p.project_id " +
                     "JOIN users u ON si.inspector_id = u.user_id WHERE si.inspection_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inspectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding inspection: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<SafetyInspection> findAll() {
        return search(null, null, null, null);
    }

    @Override
    public List<SafetyInspection> search(Integer projectId, Integer inspectorId, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder(
            "SELECT si.*, p.project_name, u.full_name as inspector_name FROM safety_inspections si " +
            "JOIN projects p ON si.project_id = p.project_id " +
            "JOIN users u ON si.inspector_id = u.user_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (projectId != null && projectId > 0) {
            sql.append("AND si.project_id = ? ");
            params.add(projectId);
        }

        if (inspectorId != null && inspectorId > 0) {
            sql.append("AND si.inspector_id = ? ");
            params.add(inspectorId);
        }

        if (startDate != null) {
            sql.append("AND si.inspection_date >= ? ");
            params.add(DateUtil.toSqlDate(startDate));
        }

        if (endDate != null) {
            sql.append("AND si.inspection_date <= ? ");
            params.add(DateUtil.toSqlDate(endDate));
        }

        sql.append("ORDER BY si.inspection_date DESC, si.inspection_id DESC");

        List<SafetyInspection> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching inspections: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int create(SafetyInspection inspection) {
        String sql = "INSERT INTO safety_inspections (project_id, inspector_id, inspection_date, ppe_score, " +
                     "worker_safety_score, equipment_score, work_area_score, emergency_score, overall_score, remarks) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inspection.getProjectId());
            ps.setInt(2, inspection.getInspectorId());
            ps.setDate(3, DateUtil.toSqlDate(inspection.getInspectionDate()));
            ps.setInt(4, inspection.getPpeScore());
            ps.setInt(5, inspection.getWorkerSafetyScore());
            ps.setInt(6, inspection.getEquipmentScore());
            ps.setInt(7, inspection.getWorkAreaScore());
            ps.setInt(8, inspection.getEmergencyScore());
            ps.setBigDecimal(9, inspection.getOverallScore());
            ps.setString(10, inspection.getRemarks());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    inspection.setInspectionId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating inspection: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean delete(int inspectionId) {
        String sql = "DELETE FROM safety_inspections WHERE inspection_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inspectionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting inspection: " + e.getMessage(), e);
        }
    }

    @Override
    public double getAverageSafetyScore() {
        String sql = "SELECT AVG(overall_score) FROM safety_inspections";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error getting avg safety score: " + e.getMessage(), e);
        }
        return 100.0;
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM safety_inspections";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting inspections: " + e.getMessage(), e);
        }
        return 0;
    }
}
