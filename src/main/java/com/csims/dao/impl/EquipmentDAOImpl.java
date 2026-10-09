package com.csims.dao.impl;

import com.csims.dao.EquipmentDAO;
import com.csims.model.Equipment;
import com.csims.util.DBConnection;
import com.csims.util.DateUtil;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class EquipmentDAOImpl implements EquipmentDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private Equipment mapRow(ResultSet rs) throws SQLException {
        Equipment e = new Equipment();
        e.setEquipmentId(rs.getInt("equipment_id"));
        e.setEquipmentCode(rs.getString("equipment_code"));
        e.setName(rs.getString("name"));
        e.setCategory(rs.getString("category"));
        e.setStatus(rs.getString("status"));

        int pId = rs.getInt("project_id");
        if (!rs.wasNull()) e.setProjectId(pId);

        Date pDate = rs.getDate("purchase_date");
        if (pDate != null) e.setPurchaseDate(pDate.toLocalDate());

        Date lDate = rs.getDate("last_service_date");
        if (lDate != null) e.setLastServiceDate(lDate.toLocalDate());

        Date nDate = rs.getDate("next_service_due");
        if (nDate != null) e.setNextServiceDue(nDate.toLocalDate());

        try {
            e.setProjectName(rs.getString("project_name"));
        } catch (SQLException ignored) {}

        return e;
    }

    @Override
    public Optional<Equipment> findById(int equipmentId) {
        String sql = "SELECT eq.*, p.project_name FROM equipment eq " +
                     "LEFT JOIN projects p ON eq.project_id = p.project_id WHERE eq.equipment_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, equipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding equipment: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Equipment> findByCode(String equipmentCode) {
        String sql = "SELECT eq.*, p.project_name FROM equipment eq " +
                     "LEFT JOIN projects p ON eq.project_id = p.project_id WHERE LOWER(eq.equipment_code) = LOWER(?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, equipmentCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding equipment by code: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Equipment> findAll() {
        return search(null, null, null, null);
    }

    @Override
    public List<Equipment> search(String keyword, String category, String status, Integer projectId) {
        StringBuilder sql = new StringBuilder(
            "SELECT eq.*, p.project_name FROM equipment eq " +
            "LEFT JOIN projects p ON eq.project_id = p.project_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(eq.equipment_code) LIKE ? OR LOWER(eq.name) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            sql.append("AND eq.category = ? ");
            params.add(category);
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND eq.status = ? ");
            params.add(status);
        }

        if (projectId != null && projectId > 0) {
            sql.append("AND eq.project_id = ? ");
            params.add(projectId);
        }

        sql.append("ORDER BY eq.name ASC");

        List<Equipment> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching equipment: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int create(Equipment equipment) {
        String sql = "INSERT INTO equipment (equipment_code, name, category, status, project_id, purchase_date, last_service_date, next_service_due) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, equipment.getEquipmentCode());
            ps.setString(2, equipment.getName());
            ps.setString(3, equipment.getCategory());
            ps.setString(4, equipment.getStatus() != null ? equipment.getStatus() : "Available");
            if (equipment.getProjectId() != null) {
                ps.setInt(5, equipment.getProjectId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setDate(6, DateUtil.toSqlDate(equipment.getPurchaseDate()));
            ps.setDate(7, DateUtil.toSqlDate(equipment.getLastServiceDate()));
            ps.setDate(8, DateUtil.toSqlDate(equipment.getNextServiceDue()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    equipment.setEquipmentId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating equipment: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(Equipment equipment) {
        String sql = "UPDATE equipment SET equipment_code = ?, name = ?, category = ?, status = ?, project_id = ?, " +
                     "purchase_date = ?, last_service_date = ?, next_service_due = ? WHERE equipment_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, equipment.getEquipmentCode());
            ps.setString(2, equipment.getName());
            ps.setString(3, equipment.getCategory());
            ps.setString(4, equipment.getStatus());
            if (equipment.getProjectId() != null) {
                ps.setInt(5, equipment.getProjectId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setDate(6, DateUtil.toSqlDate(equipment.getPurchaseDate()));
            ps.setDate(7, DateUtil.toSqlDate(equipment.getLastServiceDate()));
            ps.setDate(8, DateUtil.toSqlDate(equipment.getNextServiceDue()));
            ps.setInt(9, equipment.getEquipmentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating equipment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(int equipmentId, String status) {
        String sql = "UPDATE equipment SET status = ? WHERE equipment_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, equipmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating equipment status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean assignProject(int equipmentId, Integer projectId, String status) {
        String sql = "UPDATE equipment SET project_id = ?, status = ? WHERE equipment_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (projectId != null) {
                ps.setInt(1, projectId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, status != null ? status : "Assigned");
            ps.setInt(3, equipmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error assigning equipment to project: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateServiceDates(int equipmentId, LocalDate lastServiceDate, LocalDate nextServiceDue) {
        String sql = "UPDATE equipment SET last_service_date = ?, next_service_due = ? WHERE equipment_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, DateUtil.toSqlDate(lastServiceDate));
            ps.setDate(2, DateUtil.toSqlDate(nextServiceDue));
            ps.setInt(3, equipmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating service dates: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int equipmentId) {
        String sql = "DELETE FROM equipment WHERE equipment_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, equipmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting equipment: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Integer> countByStatus() {
        String sql = "SELECT status, COUNT(*) as cnt FROM equipment GROUP BY status";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting equipment: " + e.getMessage(), e);
        }
        return map;
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM equipment";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting equipment: " + e.getMessage(), e);
        }
        return 0;
    }
}
