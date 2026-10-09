package com.csims.dao.impl;

import com.csims.dao.MaintenanceDAO;
import com.csims.model.Maintenance;
import com.csims.util.DBConnection;
import com.csims.util.DateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaintenanceDAOImpl implements MaintenanceDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private Maintenance mapRow(ResultSet rs) throws SQLException {
        Maintenance m = new Maintenance();
        m.setMaintenanceId(rs.getInt("maintenance_id"));
        m.setEquipmentId(rs.getInt("equipment_id"));
        m.setMaintenanceType(rs.getString("maintenance_type"));
        m.setStatus(rs.getString("status"));

        Date sDate = rs.getDate("scheduled_date");
        if (sDate != null) m.setScheduledDate(sDate.toLocalDate());

        Date cDate = rs.getDate("completed_date");
        if (cDate != null) m.setCompletedDate(cDate.toLocalDate());

        m.setCost(rs.getBigDecimal("cost"));
        m.setTechnician(rs.getString("technician"));
        m.setNotes(rs.getString("notes"));

        Timestamp cTs = rs.getTimestamp("created_at");
        if (cTs != null) m.setCreatedAt(cTs.toLocalDateTime());

        try {
            m.setEquipmentCode(rs.getString("equipment_code"));
            m.setEquipmentName(rs.getString("equipment_name"));
        } catch (SQLException ignored) {}

        return m;
    }

    @Override
    public Optional<Maintenance> findById(int maintenanceId) {
        String sql = "SELECT m.*, eq.equipment_code, eq.name as equipment_name FROM maintenance m " +
                     "JOIN equipment eq ON m.equipment_id = eq.equipment_id WHERE m.maintenance_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maintenanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding maintenance record: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Maintenance> findAll() {
        return search(null, null, null);
    }

    @Override
    public List<Maintenance> search(Integer equipmentId, String status, String maintenanceType) {
        StringBuilder sql = new StringBuilder(
            "SELECT m.*, eq.equipment_code, eq.name as equipment_name FROM maintenance m " +
            "JOIN equipment eq ON m.equipment_id = eq.equipment_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (equipmentId != null && equipmentId > 0) {
            sql.append("AND m.equipment_id = ? ");
            params.add(equipmentId);
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND m.status = ? ");
            params.add(status);
        }

        if (maintenanceType != null && !maintenanceType.trim().isEmpty() && !maintenanceType.equalsIgnoreCase("ALL")) {
            sql.append("AND m.maintenance_type = ? ");
            params.add(maintenanceType);
        }

        sql.append("ORDER BY m.scheduled_date DESC");

        List<Maintenance> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching maintenance: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Maintenance> findDueMaintenance() {
        String sql = "SELECT m.*, eq.equipment_code, eq.name as equipment_name FROM maintenance m " +
                     "JOIN equipment eq ON m.equipment_id = eq.equipment_id " +
                     "WHERE m.status IN ('Due', 'Scheduled') ORDER BY m.scheduled_date ASC";
        List<Maintenance> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error finding due maintenance: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int create(Maintenance maintenance) {
        String sql = "INSERT INTO maintenance (equipment_id, maintenance_type, status, scheduled_date, completed_date, cost, technician, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, maintenance.getEquipmentId());
            ps.setString(2, maintenance.getMaintenanceType());
            ps.setString(3, maintenance.getStatus() != null ? maintenance.getStatus() : "Due");
            ps.setDate(4, DateUtil.toSqlDate(maintenance.getScheduledDate()));
            ps.setDate(5, DateUtil.toSqlDate(maintenance.getCompletedDate()));
            ps.setBigDecimal(6, maintenance.getCost());
            ps.setString(7, maintenance.getTechnician());
            ps.setString(8, maintenance.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    maintenance.setMaintenanceId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating maintenance order: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(Maintenance maintenance) {
        String sql = "UPDATE maintenance SET equipment_id = ?, maintenance_type = ?, status = ?, scheduled_date = ?, " +
                     "completed_date = ?, cost = ?, technician = ?, notes = ? WHERE maintenance_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maintenance.getEquipmentId());
            ps.setString(2, maintenance.getMaintenanceType());
            ps.setString(3, maintenance.getStatus());
            ps.setDate(4, DateUtil.toSqlDate(maintenance.getScheduledDate()));
            ps.setDate(5, DateUtil.toSqlDate(maintenance.getCompletedDate()));
            ps.setBigDecimal(6, maintenance.getCost());
            ps.setString(7, maintenance.getTechnician());
            ps.setString(8, maintenance.getNotes());
            ps.setInt(9, maintenance.getMaintenanceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating maintenance: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(int maintenanceId, String status, LocalDate completedDate) {
        String sql = "UPDATE maintenance SET status = ?, completed_date = ? WHERE maintenance_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setDate(2, DateUtil.toSqlDate(completedDate));
            ps.setInt(3, maintenanceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating maintenance status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int maintenanceId) {
        String sql = "DELETE FROM maintenance WHERE maintenance_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maintenanceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting maintenance: " + e.getMessage(), e);
        }
    }

    @Override
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM maintenance WHERE status IN ('Due', 'Scheduled', 'Serviced')";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting pending maintenance: " + e.getMessage(), e);
        }
        return 0;
    }
}
