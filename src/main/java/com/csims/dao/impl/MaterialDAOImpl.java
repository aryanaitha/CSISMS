package com.csims.dao.impl;

import com.csims.dao.MaterialDAO;
import com.csims.model.Material;
import com.csims.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaterialDAOImpl implements MaterialDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private Material mapRow(ResultSet rs) throws SQLException {
        Material m = new Material();
        m.setMaterialId(rs.getInt("material_id"));
        m.setMaterialCode(rs.getString("material_code"));
        m.setName(rs.getString("name"));
        m.setCategory(rs.getString("category"));
        m.setUnit(rs.getString("unit"));
        m.setQuantityOnHand(rs.getBigDecimal("quantity_on_hand"));
        m.setMinStock(rs.getBigDecimal("min_stock"));
        m.setMaxStock(rs.getBigDecimal("max_stock"));
        m.setUnitPrice(rs.getBigDecimal("unit_price"));
        m.setLocation(rs.getString("location"));

        Timestamp cTs = rs.getTimestamp("created_at");
        if (cTs != null) m.setCreatedAt(cTs.toLocalDateTime());

        Timestamp uTs = rs.getTimestamp("updated_at");
        if (uTs != null) m.setUpdatedAt(uTs.toLocalDateTime());

        return m;
    }

    @Override
    public Optional<Material> findById(int materialId) {
        String sql = "SELECT * FROM materials WHERE material_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, materialId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding material: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Material> findByCode(String materialCode) {
        String sql = "SELECT * FROM materials WHERE LOWER(material_code) = LOWER(?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, materialCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding material by code: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Material> findAll() {
        return search(null, null, false);
    }

    @Override
    public List<Material> search(String keyword, String category, Boolean lowStockOnly) {
        StringBuilder sql = new StringBuilder("SELECT * FROM materials WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(material_code) LIKE ? OR LOWER(name) LIKE ? OR LOWER(location) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            sql.append("AND category = ? ");
            params.add(category);
        }

        if (Boolean.TRUE.equals(lowStockOnly)) {
            sql.append("AND quantity_on_hand <= min_stock ");
        }

        sql.append("ORDER BY name ASC");

        List<Material> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching materials: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Material> findLowStock() {
        return search(null, null, true);
    }

    @Override
    public int create(Material material) {
        String sql = "INSERT INTO materials (material_code, name, category, unit, quantity_on_hand, min_stock, max_stock, unit_price, location) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, material.getMaterialCode());
            ps.setString(2, material.getName());
            ps.setString(3, material.getCategory());
            ps.setString(4, material.getUnit());
            ps.setBigDecimal(5, material.getQuantityOnHand());
            ps.setBigDecimal(6, material.getMinStock());
            ps.setBigDecimal(7, material.getMaxStock());
            ps.setBigDecimal(8, material.getUnitPrice());
            ps.setString(9, material.getLocation());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    material.setMaterialId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating material: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(Material material) {
        String sql = "UPDATE materials SET material_code = ?, name = ?, category = ?, unit = ?, " +
                     "min_stock = ?, max_stock = ?, unit_price = ?, location = ? WHERE material_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, material.getMaterialCode());
            ps.setString(2, material.getName());
            ps.setString(3, material.getCategory());
            ps.setString(4, material.getUnit());
            ps.setBigDecimal(5, material.getMinStock());
            ps.setBigDecimal(6, material.getMaxStock());
            ps.setBigDecimal(7, material.getUnitPrice());
            ps.setString(8, material.getLocation());
            ps.setInt(9, material.getMaterialId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating material: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateQuantity(int materialId, BigDecimal newQuantity) {
        String sql = "UPDATE materials SET quantity_on_hand = ? WHERE material_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newQuantity);
            ps.setInt(2, materialId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating quantity: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateQuantityInTransaction(Connection conn, int materialId, BigDecimal newQuantity) throws SQLException {
        String sql = "UPDATE materials SET quantity_on_hand = ? WHERE material_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newQuantity);
            ps.setInt(2, materialId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int materialId) {
        String sql = "DELETE FROM materials WHERE material_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, materialId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting material: " + e.getMessage(), e);
        }
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM materials";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting materials: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public int countLowStock() {
        String sql = "SELECT COUNT(*) FROM materials WHERE quantity_on_hand <= min_stock";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting low stock materials: " + e.getMessage(), e);
        }
        return 0;
    }
}
