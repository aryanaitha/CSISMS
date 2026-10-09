package com.csims.dao.impl;

import com.csims.dao.RoleDAO;
import com.csims.model.Role;
import com.csims.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoleDAOImpl implements RoleDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private Role mapRow(ResultSet rs) throws SQLException {
        return new Role(
            rs.getInt("role_id"),
            rs.getString("role_name"),
            rs.getString("description")
        );
    }

    @Override
    public Optional<Role> findById(int roleId) {
        String sql = "SELECT * FROM roles WHERE role_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding role by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Role> findByName(String roleName) {
        String sql = "SELECT * FROM roles WHERE LOWER(role_name) = LOWER(?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding role by name: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Role> findAll() {
        String sql = "SELECT * FROM roles ORDER BY role_id ASC";
        List<Role> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing all roles: " + e.getMessage(), e);
        }
        return list;
    }
}
