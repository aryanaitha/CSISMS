package com.csims.repository;

import com.csims.model.User;
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
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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
}
