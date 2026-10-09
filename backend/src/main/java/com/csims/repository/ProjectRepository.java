package com.csims.repository;

import com.csims.model.Project;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProjectRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Project> rowMapper = (rs, rowNum) -> {
        Project p = new Project();
        p.setId(rs.getInt("project_id"));
        p.setProjectName(rs.getString("project_name"));
        p.setDescription(rs.getString("description"));
        p.setLocation(rs.getString("location"));
        
        Date startDate = rs.getDate("start_date");
        if (startDate != null) p.setStartDate(startDate.toLocalDate());
        
        Date endDate = rs.getDate("end_date");
        if (endDate != null) p.setEndDate(endDate.toLocalDate());
        
        p.setBudget(rs.getBigDecimal("budget"));
        p.setStatus(rs.getString("status"));
        p.setProgressPercent(rs.getInt("progress_percent"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) p.setCreatedAt(createdAt.toLocalDateTime());
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) p.setUpdatedAt(updatedAt.toLocalDateTime());
        
        return p;
    };

    public List<Project> findAll(String status) {
        if (status != null && !status.isBlank()) {
            return jdbcTemplate.query("SELECT * FROM projects WHERE status = ? ORDER BY project_id DESC", rowMapper, status);
        }
        return jdbcTemplate.query("SELECT * FROM projects ORDER BY project_id DESC", rowMapper);
    }

    public Optional<Project> findById(Integer id) {
        try {
            Project p = jdbcTemplate.queryForObject("SELECT * FROM projects WHERE project_id = ?", rowMapper, id);
            return Optional.ofNullable(p);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer create(Project p) {
        String sql = """
            INSERT INTO projects (project_name, description, location, start_date, end_date, budget, status, progress_percent)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getProjectName());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getLocation());
            ps.setDate(4, p.getStartDate() != null ? Date.valueOf(p.getStartDate()) : null);
            ps.setDate(5, p.getEndDate() != null ? Date.valueOf(p.getEndDate()) : null);
            ps.setBigDecimal(6, p.getBudget());
            ps.setString(7, p.getStatus() != null ? p.getStatus() : "Planning");
            ps.setInt(8, p.getProgressPercent() != null ? p.getProgressPercent() : 0);
            return ps;
        }, keyHolder);

        return keyHolder.getKey() != null ? keyHolder.getKey().intValue() : null;
    }

    public void update(Project p) {
        String sql = """
            UPDATE projects
            SET project_name = ?, description = ?, location = ?, start_date = ?, end_date = ?, budget = ?, status = ?, progress_percent = ?
            WHERE project_id = ?
        """;
        jdbcTemplate.update(sql,
            p.getProjectName(),
            p.getDescription(),
            p.getLocation(),
            p.getStartDate() != null ? Date.valueOf(p.getStartDate()) : null,
            p.getEndDate() != null ? Date.valueOf(p.getEndDate()) : null,
            p.getBudget(),
            p.getStatus(),
            p.getProgressPercent(),
            p.getId()
        );
    }

    public void updateStatus(Integer id, String status) {
        jdbcTemplate.update("UPDATE projects SET status = ? WHERE project_id = ?", status, id);
    }

    public void updateProgress(Integer id, Integer progress) {
        jdbcTemplate.update("UPDATE projects SET progress_percent = ? WHERE project_id = ?", progress, id);
    }
}
