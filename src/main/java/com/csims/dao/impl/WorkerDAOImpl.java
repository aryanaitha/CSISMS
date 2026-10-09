package com.csims.dao.impl;

import com.csims.dao.WorkerDAO;
import com.csims.model.Worker;
import com.csims.util.DBConnection;
import com.csims.util.DateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkerDAOImpl implements WorkerDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private Worker mapRow(ResultSet rs) throws SQLException {
        Worker w = new Worker();
        w.setWorkerId(rs.getInt("worker_id"));
        w.setFullName(rs.getString("full_name"));
        w.setEmail(rs.getString("email"));
        w.setPhone(rs.getString("phone"));
        w.setTrade(rs.getString("trade"));

        int cId = rs.getInt("contractor_id");
        if (!rs.wasNull()) {
            w.setContractorId(cId);
        }

        w.setStatus(rs.getString("status"));

        Date jDate = rs.getDate("joined_date");
        if (jDate != null) {
            w.setJoinedDate(jDate.toLocalDate());
        }

        try {
            w.setContractorName(rs.getString("company_name"));
        } catch (SQLException ignored) {}

        return w;
    }

    @Override
    public Optional<Worker> findById(int workerId) {
        String sql = "SELECT w.*, c.company_name FROM workers w " +
                     "LEFT JOIN contractors c ON w.contractor_id = c.contractor_id " +
                     "WHERE w.worker_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding worker: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Worker> findAll() {
        return search(null, null, null);
    }

    @Override
    public List<Worker> search(String keyword, Integer contractorId, String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT w.*, c.company_name FROM workers w " +
            "LEFT JOIN contractors c ON w.contractor_id = c.contractor_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(w.full_name) LIKE ? OR LOWER(w.trade) LIKE ? OR LOWER(w.phone) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (contractorId != null && contractorId > 0) {
            sql.append("AND w.contractor_id = ? ");
            params.add(contractorId);
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND w.status = ? ");
            params.add(status);
        }

        sql.append("ORDER BY w.full_name ASC");

        List<Worker> list = new ArrayList<>();
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
            throw new RuntimeException("Error searching workers: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Worker> findByProjectId(int projectId) {
        String sql = "SELECT w.*, c.company_name FROM workers w " +
                     "JOIN project_workers pw ON w.worker_id = pw.worker_id " +
                     "LEFT JOIN contractors c ON w.contractor_id = c.contractor_id " +
                     "WHERE pw.project_id = ? ORDER BY w.full_name ASC";
        List<Worker> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding workers for project: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int create(Worker worker) {
        String sql = "INSERT INTO workers (full_name, email, phone, trade, contractor_id, status, joined_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, worker.getFullName());
            ps.setString(2, worker.getEmail());
            ps.setString(3, worker.getPhone());
            ps.setString(4, worker.getTrade());
            if (worker.getContractorId() != null) {
                ps.setInt(5, worker.getContractorId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, worker.getStatus() != null ? worker.getStatus() : "ACTIVE");
            ps.setDate(7, DateUtil.toSqlDate(worker.getJoinedDate()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    worker.setWorkerId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating worker: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(Worker worker) {
        String sql = "UPDATE workers SET full_name = ?, email = ?, phone = ?, trade = ?, contractor_id = ?, " +
                     "status = ?, joined_date = ? WHERE worker_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, worker.getFullName());
            ps.setString(2, worker.getEmail());
            ps.setString(3, worker.getPhone());
            ps.setString(4, worker.getTrade());
            if (worker.getContractorId() != null) {
                ps.setInt(5, worker.getContractorId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, worker.getStatus());
            ps.setDate(7, DateUtil.toSqlDate(worker.getJoinedDate()));
            ps.setInt(8, worker.getWorkerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating worker: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int workerId) {
        String sql = "DELETE FROM workers WHERE worker_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting worker: " + e.getMessage(), e);
        }
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM workers";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting workers: " + e.getMessage(), e);
        }
        return 0;
    }
}
