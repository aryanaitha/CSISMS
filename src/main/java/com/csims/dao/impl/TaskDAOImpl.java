package com.csims.dao.impl;

import com.csims.dao.TaskDAO;
import com.csims.model.TaskItem;
import com.csims.util.DBConnection;
import com.csims.util.DateUtil;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class TaskDAOImpl implements TaskDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private TaskItem mapRow(ResultSet rs) throws SQLException {
        TaskItem t = new TaskItem();
        t.setTaskId(rs.getInt("task_id"));
        t.setProjectId(rs.getInt("project_id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setPriority(rs.getString("priority"));
        t.setStatus(rs.getString("status"));
        t.setCompletionPercent(rs.getInt("completion_percent"));

        int wId = rs.getInt("assigned_worker_id");
        if (!rs.wasNull()) {
            t.setAssignedWorkerId(wId);
        }

        Date sDate = rs.getDate("start_date");
        if (sDate != null) t.setStartDate(sDate.toLocalDate());

        Date dDate = rs.getDate("due_date");
        if (dDate != null) t.setDueDate(dDate.toLocalDate());

        Timestamp cTs = rs.getTimestamp("created_at");
        if (cTs != null) t.setCreatedAt(cTs.toLocalDateTime());

        Timestamp uTs = rs.getTimestamp("updated_at");
        if (uTs != null) t.setUpdatedAt(uTs.toLocalDateTime());

        try {
            t.setProjectName(rs.getString("project_name"));
        } catch (SQLException ignored) {}

        try {
            t.setWorkerName(rs.getString("worker_name"));
        } catch (SQLException ignored) {}

        return t;
    }

    @Override
    public Optional<TaskItem> findById(int taskId) {
        String sql = "SELECT t.*, p.project_name, w.full_name as worker_name FROM tasks t " +
                     "JOIN projects p ON t.project_id = p.project_id " +
                     "LEFT JOIN workers w ON t.assigned_worker_id = w.worker_id " +
                     "WHERE t.task_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding task by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<TaskItem> findAll() {
        return search(null, null, null, null, null);
    }

    @Override
    public List<TaskItem> search(String keyword, Integer projectId, Integer workerId, String status, String priority) {
        StringBuilder sql = new StringBuilder(
            "SELECT t.*, p.project_name, w.full_name as worker_name FROM tasks t " +
            "JOIN projects p ON t.project_id = p.project_id " +
            "LEFT JOIN workers w ON t.assigned_worker_id = w.worker_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(t.title) LIKE ? OR LOWER(t.description) LIKE ?) ");
            String term = "%" + keyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }

        if (projectId != null && projectId > 0) {
            sql.append("AND t.project_id = ? ");
            params.add(projectId);
        }

        if (workerId != null && workerId > 0) {
            sql.append("AND t.assigned_worker_id = ? ");
            params.add(workerId);
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND t.status = ? ");
            params.add(status);
        }

        if (priority != null && !priority.trim().isEmpty() && !priority.equalsIgnoreCase("ALL")) {
            sql.append("AND t.priority = ? ");
            params.add(priority);
        }

        sql.append("ORDER BY t.task_id DESC");

        List<TaskItem> list = new ArrayList<>();
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
            throw new RuntimeException("Error searching tasks: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TaskItem> findOverdueTasks() {
        String sql = "SELECT t.*, p.project_name, w.full_name as worker_name FROM tasks t " +
                     "JOIN projects p ON t.project_id = p.project_id " +
                     "LEFT JOIN workers w ON t.assigned_worker_id = w.worker_id " +
                     "WHERE t.due_date < CURRENT_DATE AND t.status != 'Completed' ORDER BY t.due_date ASC";
        List<TaskItem> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading overdue tasks: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int create(TaskItem task) {
        String sql = "INSERT INTO tasks (project_id, title, description, priority, status, completion_percent, " +
                     "assigned_worker_id, start_date, due_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, task.getProjectId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setString(4, task.getPriority() != null ? task.getPriority() : "MEDIUM");
            ps.setString(5, task.getStatus() != null ? task.getStatus() : "Created");
            ps.setInt(6, task.getCompletionPercent());
            if (task.getAssignedWorkerId() != null) {
                ps.setInt(7, task.getAssignedWorkerId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setDate(8, DateUtil.toSqlDate(task.getStartDate()));
            ps.setDate(9, DateUtil.toSqlDate(task.getDueDate()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    task.setTaskId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating task: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public boolean update(TaskItem task) {
        String sql = "UPDATE tasks SET project_id = ?, title = ?, description = ?, priority = ?, status = ?, " +
                     "completion_percent = ?, assigned_worker_id = ?, start_date = ?, due_date = ? WHERE task_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, task.getProjectId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setString(4, task.getPriority());
            ps.setString(5, task.getStatus());
            ps.setInt(6, task.getCompletionPercent());
            if (task.getAssignedWorkerId() != null) {
                ps.setInt(7, task.getAssignedWorkerId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setDate(8, DateUtil.toSqlDate(task.getStartDate()));
            ps.setDate(9, DateUtil.toSqlDate(task.getDueDate()));
            ps.setInt(10, task.getTaskId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating task: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatusAndProgress(int taskId, String status, int completionPercent) {
        String sql = "UPDATE tasks SET status = ?, completion_percent = ? WHERE task_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, completionPercent);
            ps.setInt(3, taskId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating task progress: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean assignWorker(int taskId, Integer workerId) {
        String sql = "UPDATE tasks SET assigned_worker_id = ?, status = CASE WHEN status = 'Created' THEN 'Assigned' ELSE status END WHERE task_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (workerId != null) {
                ps.setInt(1, workerId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, taskId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error assigning worker to task: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int taskId) {
        String sql = "DELETE FROM tasks WHERE task_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting task: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Integer> countByStatus() {
        String sql = "SELECT status, COUNT(*) as cnt FROM tasks GROUP BY status";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting tasks by status: " + e.getMessage(), e);
        }
        return map;
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM tasks";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting total tasks: " + e.getMessage(), e);
        }
        return 0;
    }
}
