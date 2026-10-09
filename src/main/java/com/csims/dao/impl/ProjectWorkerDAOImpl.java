package com.csims.dao.impl;

import com.csims.dao.ProjectWorkerDAO;
import com.csims.model.ProjectWorker;
import com.csims.util.DBConnection;
import com.csims.util.DateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProjectWorkerDAOImpl implements ProjectWorkerDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private ProjectWorker mapRow(ResultSet rs) throws SQLException {
        ProjectWorker pw = new ProjectWorker();
        pw.setProjectId(rs.getInt("project_id"));
        pw.setWorkerId(rs.getInt("worker_id"));
        pw.setRoleInProject(rs.getString("role_in_project"));

        Date aDate = rs.getDate("assigned_date");
        if (aDate != null) {
            pw.setAssignedDate(aDate.toLocalDate());
        }

        try {
            pw.setWorkerName(rs.getString("full_name"));
            pw.setTrade(rs.getString("trade"));
        } catch (SQLException ignored) {}

        try {
            pw.setProjectName(rs.getString("project_name"));
        } catch (SQLException ignored) {}

        return pw;
    }

    @Override
    public boolean assign(int projectId, int workerId, String roleInProject, LocalDate assignedDate) {
        String sql = "INSERT INTO project_workers (project_id, worker_id, role_in_project, assigned_date) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE role_in_project = VALUES(role_in_project), assigned_date = VALUES(assigned_date)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ps.setInt(2, workerId);
            ps.setString(3, roleInProject != null ? roleInProject : "Site Worker");
            ps.setDate(4, DateUtil.toSqlDate(assignedDate != null ? assignedDate : LocalDate.now()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error assigning worker to project: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean unassign(int projectId, int workerId) {
        String sql = "DELETE FROM project_workers WHERE project_id = ? AND worker_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ps.setInt(2, workerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error removing worker from project: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProjectWorker> findByProjectId(int projectId) {
        String sql = "SELECT pw.*, w.full_name, w.trade, p.project_name FROM project_workers pw " +
                     "JOIN workers w ON pw.worker_id = w.worker_id " +
                     "JOIN projects p ON pw.project_id = p.project_id " +
                     "WHERE pw.project_id = ? ORDER BY w.full_name ASC";
        List<ProjectWorker> list = new ArrayList<>();
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
    public List<ProjectWorker> findByWorkerId(int workerId) {
        String sql = "SELECT pw.*, w.full_name, w.trade, p.project_name FROM project_workers pw " +
                     "JOIN workers w ON pw.worker_id = w.worker_id " +
                     "JOIN projects p ON pw.project_id = p.project_id " +
                     "WHERE pw.worker_id = ? ORDER BY p.project_name ASC";
        List<ProjectWorker> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding projects for worker: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean isAssigned(int projectId, int workerId) {
        String sql = "SELECT 1 FROM project_workers WHERE project_id = ? AND worker_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ps.setInt(2, workerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking assignment: " + e.getMessage(), e);
        }
    }
}
