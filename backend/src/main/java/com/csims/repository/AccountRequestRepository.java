package com.csims.repository;

import com.csims.model.AccountRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class AccountRequestRepository {
    private final JdbcTemplate jdbcTemplate;

    public AccountRequestRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AccountRequest> rowMapper = (rs, rowNum) -> {
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

    public Long create(AccountRequest req) {
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

    public Optional<AccountRequest> findById(Long id) {
        String sql = """
            SELECT ar.*, r.role_name, u.full_name AS reviewer_name
            FROM account_requests ar
            LEFT JOIN roles r ON ar.requested_role_id = r.role_id
            LEFT JOIN users u ON ar.reviewed_by = u.user_id
            WHERE ar.request_id = ?
        """;
        try {
            AccountRequest req = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(req);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<AccountRequest> findAll(String status) {
        String sql = """
            SELECT ar.*, r.role_name, u.full_name AS reviewer_name
            FROM account_requests ar
            LEFT JOIN roles r ON ar.requested_role_id = r.role_id
            LEFT JOIN users u ON ar.reviewed_by = u.user_id
        """;
        if (status != null && !status.isBlank()) {
            sql += " WHERE ar.status = ? ORDER BY ar.created_at DESC";
            return jdbcTemplate.query(sql, rowMapper, status);
        } else {
            sql += " ORDER BY ar.created_at DESC";
            return jdbcTemplate.query(sql, rowMapper);
        }
    }

    public void updateReview(Long id, String status, Long reviewerId, String notes) {
        String sql = """
            UPDATE account_requests 
            SET status = ?, reviewed_by = ?, reviewed_at = NOW(), review_note = ? 
            WHERE request_id = ?
        """;
        jdbcTemplate.update(sql, status, reviewerId, notes, id);
    }
}
