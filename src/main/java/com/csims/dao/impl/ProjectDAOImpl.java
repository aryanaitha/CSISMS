package com.csims.dao.impl;

import com.csims.dao.ProjectDAO;
import com.csims.model.Project;
import com.csims.util.DBConnection;
import com.csims.util.DateUtil;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class ProjectDAOImpl implements ProjectDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private Project mapRow(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setProjectId(rs.getInt("project_id"));
        p.setProjectName(rs.getString("project_name"));
        p.setDescription(rs.getString("description"));
        p.setLocation(rs.getString("location"));

        Date sDate = rs.getDate("start_date");
        if (sDate != null) p.setStartDate(sDate.toLocalDate());

        Date eDate = rs.getDate("end_date");
        if (eDate != null) p.setEndDate(eDate.toLocalDate());

        p.setBudget(rs.getBigDecimal("budget"));
        p.setStatus(rs.getString("status"));
        p.setProgressPercent(rs.getInt("progress_percent"));

        Timestamp cTs = rs.getTimestamp("created_at");
        if (cTs != null) p.setCreatedAt(cTs.toLocalDateTime());

        Timestamp uTs = rs.getTimestamp("updated_at");
        if (uTs != null) p.setUpdatedAt(uTs.toLocalDateTime());

        return p;
    }

    @Override
    public Optional<Project> findById(int projectId) {
        String sql = "SELECT * FROM projects WHERE project_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding project by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Project> findAll() {
        return search(null, null);
    }

    @Override
    public List<Project> search(String keyword, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM projects WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(project_name) LIKE ? OR LOWER(location) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND status = ? ");
            params.add(status);
        }

        sql.append("ORDER BY project_id DESC");

        List<Project> list = new ArrayList<>();
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
            throw new RuntimeException("Error listing projects: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int create(Project project) {
        String sql = "INSERT INTO projects (project_name, description, location, start_date, end_date, budget, status, progress_percent) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, project.getProjectName());
            ps.setString(2, project.getDescription());
            ps.setString(3, project.getLocation());
            ps.setDate(4, DateUtil.toSqlDate(project.getStartDate()));
            ps.setDate(5, DateUtil.toSqlDate(project.getEndDate()));
            ps.setBigDecimal(6, project.getBudget());
            ps.setString(7, project.getStatus() != null ? project.getStatus() : "Planning");
            ps.setInt(8, project.getProgressPercent());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    project.setProjectId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating project: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(Project project) {
        String sql = "UPDATE projects SET project_name = ?, description = ?, location = ?, start_date = ?, end_date = ?, " +
                     "budget = ?, status = ?, progress_percent = ? WHERE project_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, project.getProjectName());
            ps.setString(2, project.getDescription());
            ps.setString(3, project.getLocation());
            ps.setDate(4, DateUtil.toSqlDate(project.getStartDate()));
            ps.setDate(5, DateUtil.toSqlDate(project.getEndDate()));
            ps.setBigDecimal(6, project.getBudget());
            ps.setString(7, project.getStatus());
            ps.setInt(8, project.getProgressPercent());
            ps.setInt(9, project.getProjectId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating project: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateProgress(int projectId, int progressPercent) {
        String sql = "UPDATE projects SET progress_percent = ? WHERE project_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, progressPercent);
            ps.setInt(2, projectId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating progress: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(int projectId, String status) {
        String sql = "UPDATE projects SET status = ? WHERE project_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, projectId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int projectId) {
        String sql = "DELETE FROM projects WHERE project_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting project: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Integer> countByStatus() {
        String sql = "SELECT status, COUNT(*) as cnt FROM projects GROUP BY status";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting projects by status: " + e.getMessage(), e);
        }
        return map;
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM projects";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting projects: " + e.getMessage(), e);
        }
        return 0;
    }
}
