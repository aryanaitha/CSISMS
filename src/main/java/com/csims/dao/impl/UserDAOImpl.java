package com.csims.dao.impl;

import com.csims.dao.UserDAO;
import com.csims.model.User;
import com.csims.util.DBConnection;
import com.csims.util.DateUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));
        user.setFullName(rs.getString("full_name"));
        user.setRoleId(rs.getInt("role_id"));
        user.setActive(rs.getBoolean("is_active"));
        user.setFailedAttempts(rs.getInt("failed_attempts"));

        Timestamp lockedTs = rs.getTimestamp("locked_until");
        if (lockedTs != null) {
            user.setLockedUntil(lockedTs.toLocalDateTime());
        }

        Timestamp loginTs = rs.getTimestamp("last_login");
        if (loginTs != null) {
            user.setLastLogin(loginTs.toLocalDateTime());
        }

        user.setMustChangePassword(rs.getBoolean("must_change_password"));

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            user.setCreatedAt(createdTs.toLocalDateTime());
        }

        try {
            user.setRoleName(rs.getString("role_name"));
        } catch (SQLException ignored) {
            // role_name might not be in all projections
        }
        return user;
    }

    @Override
    public Optional<User> findById(int userId) {
        String sql = "SELECT u.*, r.role_name FROM users u " +
                     "JOIN roles r ON u.role_id = r.role_id WHERE u.user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        String sql = "SELECT u.*, r.role_name FROM users u " +
                     "JOIN roles r ON u.role_id = r.role_id " +
                     "WHERE LOWER(u.username) = LOWER(?) OR LOWER(u.email) = LOWER(?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usernameOrEmail.trim());
            ps.setString(2, usernameOrEmail.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username or email: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return search(null, null, null);
    }

    @Override
    public List<User> search(String keyword, Integer roleId, Boolean activeOnly) {
        StringBuilder sql = new StringBuilder("SELECT u.*, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(u.username) LIKE ? OR LOWER(u.email) LIKE ? OR LOWER(u.full_name) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (roleId != null && roleId > 0) {
            sql.append("AND u.role_id = ? ");
            params.add(roleId);
        }

        if (activeOnly != null) {
            sql.append("AND u.is_active = ? ");
            params.add(activeOnly);
        }

        sql.append("ORDER BY u.user_id ASC");

        List<User> list = new ArrayList<>();
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
            throw new RuntimeException("Error searching users: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int create(User user) {
        String sql = "INSERT INTO users (username, email, password_hash, salt, full_name, role_id, is_active, " +
                     "failed_attempts, locked_until, must_change_password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getSalt());
            ps.setString(5, user.getFullName());
            ps.setInt(6, user.getRoleId());
            ps.setBoolean(7, user.isActive());
            ps.setInt(8, user.getFailedAttempts());
            ps.setTimestamp(9, DateUtil.toSqlTimestamp(user.getLockedUntil()));
            ps.setBoolean(10, user.isMustChangePassword());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    user.setUserId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating user: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, role_id = ?, is_active = ? WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setInt(3, user.getRoleId());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updatePassword(int userId, String passwordHash, String salt, boolean mustChangePassword) {
        String sql = "UPDATE users SET password_hash = ?, salt = ?, must_change_password = ? WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setString(2, salt);
            ps.setBoolean(3, mustChangePassword);
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating password: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateFailedAttempts(int userId, int failedAttempts) {
        String sql = "UPDATE users SET failed_attempts = ? WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, failedAttempts);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating failed attempts: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean lockAccount(int userId, LocalDateTime lockedUntil, int failedAttempts) {
        String sql = "UPDATE users SET locked_until = ?, failed_attempts = ? WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, DateUtil.toSqlTimestamp(lockedUntil));
            ps.setInt(2, failedAttempts);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error locking account: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean resetLockout(int userId) {
        String sql = "UPDATE users SET failed_attempts = 0, locked_until = NULL WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error resetting lockout: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP, failed_attempts = 0, locked_until = NULL WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating last login: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(int userId, boolean isActive) {
        String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isActive);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }
}
