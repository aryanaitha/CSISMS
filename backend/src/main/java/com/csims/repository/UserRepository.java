package com.csims.repository;

import com.csims.model.Models.AccountRequest;
import com.csims.model.Models.Permission;
import com.csims.model.Models.Role;
import com.csims.model.Models.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Consolidated User & Identity Repository.
 * Handles Users, Roles, Permissions, and Account Registration Requests.
 */
@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================================================================
    // SECTION 1: USERS
    // =========================================================================

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User u = new User();
        u.setId(rs.getLong("user_id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setRoleId(rs.getInt("role_id"));
        u.setRoleName(rs.getString("role_name"));
        u.setActive(rs.getBoolean("is_active"));
        u.setFailedLoginAttempts(rs.getInt("failed_attempts"));
        
        Timestamp lockedUntilTs = rs.getTimestamp("locked_until");
        if (lockedUntilTs != null) {
            u.setLockedUntil(lockedUntilTs.toLocalDateTime());
        }
        
        u.setMustChangePassword(rs.getBoolean("must_change_password"));
        
        Timestamp lastLoginTs = rs.getTimestamp("last_login");
        if (lastLoginTs != null) {
            u.setLastLogin(lastLoginTs.toLocalDateTime());
        }
        
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            u.setCreatedAt(createdAtTs.toLocalDateTime());
        }
        return u;
    };

    public Optional<User> findByUsernameOrEmail(String identifier) {
        String sql = """
            SELECT u.*, r.role_name 
            FROM users u 
            JOIN roles r ON u.role_id = r.role_id 
            WHERE u.username = ? OR u.email = ?
        """;
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, identifier, identifier);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findById(Long id) {
        String sql = """
            SELECT u.*, r.role_name 
            FROM users u 
            JOIN roles r ON u.role_id = r.role_id 
            WHERE u.user_id = ?
        """;
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<User> findAll() {
        String sql = """
            SELECT u.*, r.role_name 
            FROM users u 
            JOIN roles r ON u.role_id = r.role_id 
            ORDER BY u.user_id ASC
        """;
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public void incrementFailedLogins(Long id) {
        String sql = "UPDATE users SET failed_attempts = failed_attempts + 1 WHERE user_id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void lockAccount(Long id, LocalDateTime lockedUntil) {
        String sql = "UPDATE users SET locked_until = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(lockedUntil), id);
    }

    public void resetFailedLoginsAndLock(Long id) {
        String sql = "UPDATE users SET failed_attempts = 0, locked_until = NULL WHERE user_id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void updateLastLogin(Long id) {
        String sql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void updatePassword(Long id, String passwordHash, boolean mustChangePassword) {
        String sql = "UPDATE users SET password_hash = ?, must_change_password = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, passwordHash, mustChangePassword, id);
    }

    public Long createUser(User user) {
        String sql = """
            INSERT INTO users (username, email, password_hash, full_name, role_id, is_active, must_change_password)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getFullName());
            ps.setInt(5, user.getRoleId());
            ps.setBoolean(6, user.isActive());
            ps.setBoolean(7, user.isMustChangePassword());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        return null;
    }

    public void updateUser(User user) {
        String sql = "UPDATE users SET email = ?, full_name = ?, role_id = ?, is_active = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getFullName(), user.getRoleId(), user.isActive(), user.getId());
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    // =========================================================================
    // SECTION 2: ROLES & PERMISSIONS
    // =========================================================================

    private final RowMapper<Role> roleRowMapper = (rs, rowNum) -> 
        new Role(rs.getInt("role_id"), rs.getString("role_name"), rs.getString("description"));

    private final RowMapper<Permission> permissionRowMapper = (rs, rowNum) ->
        new Permission(rs.getInt("permission_id"), rs.getString("permission_code"), rs.getString("module"), rs.getString("description"));

    public List<Role> findAllRoles() {
        return jdbcTemplate.query("SELECT * FROM roles ORDER BY role_id ASC", roleRowMapper);
    }

    public Optional<Role> findRoleById(Integer id) {
        try {
            Role role = jdbcTemplate.queryForObject("SELECT * FROM roles WHERE role_id = ?", roleRowMapper, id);
            return Optional.ofNullable(role);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Permission> findAllPermissions() {
        return jdbcTemplate.query("SELECT * FROM permissions ORDER BY module ASC, permission_code ASC", permissionRowMapper);
    }

    public Set<String> findPermissionNamesByRoleId(Integer roleId) {
        String sql = """
            SELECT p.permission_code 
            FROM role_permissions rp 
            JOIN permissions p ON rp.permission_id = p.permission_id 
            WHERE rp.role_id = ?
        """;
        List<String> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("permission_code"), roleId);
        return new HashSet<>(list);
    }

    public List<Permission> findPermissionsByRoleId(Integer roleId) {
        String sql = """
            SELECT p.* 
            FROM role_permissions rp 
            JOIN permissions p ON rp.permission_id = p.permission_id 
            WHERE rp.role_id = ?
            ORDER BY p.module ASC, p.permission_code ASC
        """;
        return jdbcTemplate.query(sql, permissionRowMapper, roleId);
    }

    public void updateRolePermissions(Integer roleId, List<Integer> permissionIds) {
        jdbcTemplate.update("DELETE FROM role_permissions WHERE role_id = ?", roleId);
        String sql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";
        for (Integer permId : permissionIds) {
            jdbcTemplate.update(sql, roleId, permId);
        }
    }

    // =========================================================================
    // SECTION 3: ACCOUNT REQUESTS
    // =========================================================================

    private final RowMapper<AccountRequest> accountRequestRowMapper = (rs, rowNum) -> {
        AccountRequest r = new AccountRequest();
        r.setId(rs.getLong("request_id"));
        r.setFullName(rs.getString("full_name"));
        r.setEmail(rs.getString("email"));
        r.setUsername(rs.getString("username"));
        
        int roleId = rs.getInt("requested_role_id");
        if (!rs.wasNull()) {
            r.setRequestedRoleId(roleId);
        }
        r.setRequestedRoleName(rs.getString("role_name"));
        r.setReason(rs.getString("reason"));
        r.setStatus(rs.getString("status"));
        r.setRequestType(rs.getString("request_type"));
        
        long reviewedBy = rs.getLong("reviewed_by");
        if (!rs.wasNull()) {
            r.setReviewedBy(reviewedBy);
        }
        r.setReviewerName(rs.getString("reviewer_name"));
        
        Timestamp reviewedAt = rs.getTimestamp("reviewed_at");
        if (reviewedAt != null) {
            r.setReviewedAt(reviewedAt.toLocalDateTime());
        }
        
        r.setReviewNotes(rs.getString("review_note"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            r.setCreatedAt(createdAt.toLocalDateTime());
        }
        return r;
    };

    public Long createAccountRequest(AccountRequest req) {
        String sql = """
            INSERT INTO account_requests (full_name, email, username, requested_role_id, reason, status, request_type)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, req.getFullName());
            ps.setString(2, req.getEmail());
            ps.setString(3, req.getUsername());
            if (req.getRequestedRoleId() != null) {
                ps.setInt(4, req.getRequestedRoleId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setString(5, req.getReason());
            ps.setString(6, req.getStatus());
            ps.setString(7, req.getRequestType());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        return null;
    }

    public Optional<AccountRequest> findAccountRequestById(Long id) {
        String sql = """
            SELECT ar.*, r.role_name, u.full_name AS reviewer_name
            FROM account_requests ar
            LEFT JOIN roles r ON ar.requested_role_id = r.role_id
            LEFT JOIN users u ON ar.reviewed_by = u.user_id
            WHERE ar.request_id = ?
        """;
        try {
            AccountRequest req = jdbcTemplate.queryForObject(sql, accountRequestRowMapper, id);
            return Optional.ofNullable(req);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<AccountRequest> findAllAccountRequests(String status) {
        String sql = """
            SELECT ar.*, r.role_name, u.full_name AS reviewer_name
            FROM account_requests ar
            LEFT JOIN roles r ON ar.requested_role_id = r.role_id
            LEFT JOIN users u ON ar.reviewed_by = u.user_id
        """;
        if (status != null && !status.isBlank()) {
            sql += " WHERE ar.status = ? ORDER BY ar.created_at DESC";
            return jdbcTemplate.query(sql, accountRequestRowMapper, status);
        } else {
            sql += " ORDER BY ar.created_at DESC";
            return jdbcTemplate.query(sql, accountRequestRowMapper);
        }
    }

    public void updateAccountRequestReview(Long id, String status, Long reviewerId, String notes) {
        String sql = """
            UPDATE account_requests 
            SET status = ?, reviewed_by = ?, reviewed_at = NOW(), review_note = ? 
            WHERE request_id = ?
        """;
        jdbcTemplate.update(sql, status, reviewerId, notes, id);
    }

    // Compatibility delegates
    public Long create(AccountRequest req) { return createAccountRequest(req); }
    public Optional<AccountRequest> findRequestById(Long id) { return findAccountRequestById(id); }
    public List<AccountRequest> findAllRequests(String status) { return findAllAccountRequests(status); }
    public void updateReview(Long id, String status, Long reviewerId, String notes) { updateAccountRequestReview(id, status, reviewerId, notes); }
}
