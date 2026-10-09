package com.csims.repository;

import com.csims.model.Task;
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
public class TaskRepository {
    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Task> rowMapper = (rs, rowNum) -> {
        Task t = new Task();
        t.setId(rs.getInt("task_id"));
        t.setProjectId(rs.getInt("project_id"));
        t.setProjectName(rs.getString("project_name"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setPriority(rs.getString("priority"));
        t.setStatus(rs.getString("status"));
        t.setCompletionPercent(rs.getInt("completion_percent"));
        
        int wid = rs.getInt("assigned_worker_id");
        if (!rs.wasNull()) t.setAssignedWorkerId(wid);
        t.setAssignedWorkerName(rs.getString("worker_name"));
        
        Date start = rs.getDate("start_date");
        if (start != null) t.setStartDate(start.toLocalDate());
        
        Date due = rs.getDate("due_date");
        if (due != null) t.setDueDate(due.toLocalDate());
        
        Timestamp cAt = rs.getTimestamp("created_at");
        if (cAt != null) t.setCreatedAt(cAt.toLocalDateTime());
        
        Timestamp uAt = rs.getTimestamp("updated_at");
        if (uAt != null) t.setUpdatedAt(uAt.toLocalDateTime());
        
        return t;
    };

    public List<Task> findAll(Integer projectId, String status, Integer workerId) {
        StringBuilder sql = new StringBuilder("""
            SELECT t.*, p.project_name, w.full_name AS worker_name
            FROM tasks t
            JOIN projects p ON t.project_id = p.project_id
            LEFT JOIN workers w ON t.assigned_worker_id = w.worker_id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();
        if (projectId != null) {
            sql.append(" AND t.project_id = ?");
            params.add(projectId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND t.status = ?");
            params.add(status);
        }
        if (workerId != null) {
            sql.append(" AND t.assigned_worker_id = ?");
            params.add(workerId);
        }
        sql.append(" ORDER BY t.task_id DESC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public Optional<Task> findById(Integer id) {
        String sql = """
            SELECT t.*, p.project_name, w.full_name AS worker_name
            FROM tasks t
            JOIN projects p ON t.project_id = p.project_id
            LEFT JOIN workers w ON t.assigned_worker_id = w.worker_id
            WHERE t.task_id = ?
        """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer create(Task t) {
        String sql = """
            INSERT INTO tasks (project_id, title, description, priority, status, completion_percent, assigned_worker_id, start_date, due_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, t.getProjectId());
            ps.setString(2, t.getTitle());
            ps.setString(3, t.getDescription());
            ps.setString(4, t.getPriority() != null ? t.getPriority() : "MEDIUM");
            ps.setString(5, t.getStatus() != null ? t.getStatus() : "Created");
            ps.setInt(6, t.getCompletionPercent() != null ? t.getCompletionPercent() : 0);
            if (t.getAssignedWorkerId() != null) ps.setInt(7, t.getAssignedWorkerId());
            else ps.setNull(7, java.sql.Types.INTEGER);
            ps.setDate(8, t.getStartDate() != null ? Date.valueOf(t.getStartDate()) : null);
            ps.setDate(9, t.getDueDate() != null ? Date.valueOf(t.getDueDate()) : null);
            return ps;
        }, kh);
        return kh.getKey() != null ? kh.getKey().intValue() : null;
    }

    public void update(Task t) {
        String sql = """
            UPDATE tasks
            SET title = ?, description = ?, priority = ?, status = ?, completion_percent = ?, assigned_worker_id = ?, start_date = ?, due_date = ?
            WHERE task_id = ?
        """;
        jdbcTemplate.update(sql,
            t.getTitle(),
            t.getDescription(),
            t.getPriority(),
            t.getStatus(),
            t.getCompletionPercent(),
            t.getAssignedWorkerId(),
            t.getStartDate() != null ? Date.valueOf(t.getStartDate()) : null,
            t.getDueDate() != null ? Date.valueOf(t.getDueDate()) : null,
            t.getId()
        );
    }

    public void updateStatus(Integer id, String status, Integer completionPercent) {
        if (completionPercent != null) {
            jdbcTemplate.update("UPDATE tasks SET status = ?, completion_percent = ? WHERE task_id = ?", status, completionPercent, id);
        } else {
            jdbcTemplate.update("UPDATE tasks SET status = ? WHERE task_id = ?", status, id);
        }
    }

    public void assignWorker(Integer id, Integer workerId) {
        jdbcTemplate.update("UPDATE tasks SET assigned_worker_id = ? WHERE task_id = ?", workerId, id);
    }
}
