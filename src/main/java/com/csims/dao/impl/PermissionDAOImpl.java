package com.csims.dao.impl;

import com.csims.dao.PermissionDAO;
import com.csims.model.Permission;
import com.csims.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermissionDAOImpl implements PermissionDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private Permission mapRow(ResultSet rs) throws SQLException {
        return new Permission(
            rs.getInt("permission_id"),
            rs.getString("permission_code"),
            rs.getString("description"),
            rs.getString("module")
        );
    }

    @Override
    public List<Permission> findAll() {
        String sql = "SELECT * FROM permissions ORDER BY module, permission_code";
        List<Permission> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading permissions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Permission> findByRoleId(int roleId) {
        String sql = "SELECT p.* FROM permissions p " +
                     "JOIN role_permissions rp ON p.permission_id = rp.permission_id " +
                     "WHERE rp.role_id = ? ORDER BY p.module, p.permission_code";
        List<Permission> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading role permissions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Set<String> findPermissionCodesByRoleId(int roleId) {
        String sql = "SELECT p.permission_code FROM permissions p " +
                     "JOIN role_permissions rp ON p.permission_id = rp.permission_id " +
                     "WHERE rp.role_id = ?";
        Set<String> set = new HashSet<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getString("permission_code"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading permission codes: " + e.getMessage(), e);
        }
        return set;
    }

    @Override
    public boolean updateRolePermissions(int roleId, Set<Integer> permissionIds) {
        String deleteSql = "DELETE FROM role_permissions WHERE role_id = ?";
        String insertSql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement deletePs = conn.prepareStatement(deleteSql);
                 PreparedStatement insertPs = conn.prepareStatement(insertSql)) {

                deletePs.setInt(1, roleId);
                deletePs.executeUpdate();

                if (permissionIds != null) {
                    for (Integer pId : permissionIds) {
                        insertPs.setInt(1, roleId);
                        insertPs.setInt(2, pId);
                        insertPs.addBatch();
                    }
                    insertPs.executeBatch();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating role permissions: " + e.getMessage(), e);
        }
    }
}
