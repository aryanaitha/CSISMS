package com.csims.dao.impl;

import com.csims.dao.AccountRequestDAO;
import com.csims.model.AccountRequest;
import com.csims.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountRequestDAOImpl implements AccountRequestDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private AccountRequest mapRow(ResultSet rs) throws SQLException {
        AccountRequest ar = new AccountRequest();
        ar.setRequestId(rs.getInt("request_id"));
        ar.setRequestType(rs.getString("request_type"));
        ar.setFullName(rs.getString("full_name"));
        ar.setEmail(rs.getString("email"));
        ar.setUsername(rs.getString("username"));

        int roleId = rs.getInt("requested_role_id");
        if (!rs.wasNull()) {
            ar.setRequestedRoleId(roleId);
        }

        ar.setReason(rs.getString("reason"));
        ar.setStatus(rs.getString("status"));

        int reviewer = rs.getInt("reviewed_by");
        if (!rs.wasNull()) {
            ar.setReviewedBy(reviewer);
        }

        ar.setReviewNote(rs.getString("review_note"));

        Timestamp cTs = rs.getTimestamp("created_at");
        if (cTs != null) {
            ar.setCreatedAt(cTs.toLocalDateTime());
        }

        Timestamp rTs = rs.getTimestamp("reviewed_at");
        if (rTs != null) {
            ar.setReviewedAt(rTs.toLocalDateTime());
        }

        try {
            ar.setRequestedRoleName(rs.getString("role_name"));
        } catch (SQLException ignored) {}

        try {
            ar.setReviewerUsername(rs.getString("reviewer_name"));
        } catch (SQLException ignored) {}

        return ar;
    }

    @Override
    public int create(AccountRequest request) {
        String sql = "INSERT INTO account_requests (request_type, full_name, email, username, requested_role_id, reason, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, request.getRequestType());
            ps.setString(2, request.getFullName());
            ps.setString(3, request.getEmail());
            ps.setString(4, request.getUsername());
            if (request.getRequestedRoleId() != null) {
                ps.setInt(5, request.getRequestedRoleId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, request.getReason());
            ps.setString(7, request.getStatus() != null ? request.getStatus() : "PENDING");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    request.setRequestId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating account request: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public Optional<AccountRequest> findById(int requestId) {
        String sql = "SELECT ar.*, r.role_name, u.username as reviewer_name FROM account_requests ar " +
                     "LEFT JOIN roles r ON ar.requested_role_id = r.role_id " +
                     "LEFT JOIN users u ON ar.reviewed_by = u.user_id " +
                     "WHERE ar.request_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding account request by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<AccountRequest> findAll() {
        return search(null, null, null);
    }

    @Override
    public List<AccountRequest> findByStatus(String status) {
        return search(null, null, status);
    }

    @Override
    public List<AccountRequest> search(String keyword, String type, String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT ar.*, r.role_name, u.username as reviewer_name FROM account_requests ar " +
            "LEFT JOIN roles r ON ar.requested_role_id = r.role_id " +
            "LEFT JOIN users u ON ar.reviewed_by = u.user_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(ar.username) LIKE ? OR LOWER(ar.email) LIKE ? OR LOWER(ar.full_name) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (type != null && !type.trim().isEmpty() && !type.equalsIgnoreCase("ALL")) {
            sql.append("AND ar.request_type = ? ");
            params.add(type);
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND ar.status = ? ");
            params.add(status);
        }

        sql.append("ORDER BY ar.created_at DESC");

        List<AccountRequest> list = new ArrayList<>();
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
            throw new RuntimeException("Error searching account requests: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean updateReview(int requestId, String status, int reviewerId, String reviewNote) {
        String sql = "UPDATE account_requests SET status = ?, reviewed_by = ?, review_note = ?, reviewed_at = CURRENT_TIMESTAMP WHERE request_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reviewerId);
            ps.setString(3, reviewNote);
            ps.setInt(4, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error reviewing account request: " + e.getMessage(), e);
        }
    }
}
