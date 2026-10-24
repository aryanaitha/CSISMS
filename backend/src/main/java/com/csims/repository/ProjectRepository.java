package com.csims.repository;

import com.csims.model.Models.Attendance;
import com.csims.model.Models.Contractor;
import com.csims.model.Models.Project;
import com.csims.model.Models.ProjectWorker;
import com.csims.model.Models.Task;
import com.csims.model.Models.Worker;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Consolidated Project Management Repository.
 * Covers Projects, Workforce (Contractors/Workers), Tasks, and Attendance.
 */
@Repository
public class ProjectRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProjectRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================================================================
    // SECTION 1: PROJECTS
    // =========================================================================

    private final RowMapper<Project> projectRowMapper = (rs, rowNum) -> {
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
            return jdbcTemplate.query("SELECT * FROM projects WHERE status = ? ORDER BY project_id DESC", projectRowMapper, status);
        }
        return jdbcTemplate.query("SELECT * FROM projects ORDER BY project_id DESC", projectRowMapper);
    }

    public Optional<Project> findById(Integer id) {
        try {
            Project p = jdbcTemplate.queryForObject("SELECT * FROM projects WHERE project_id = ?", projectRowMapper, id);
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

    // =========================================================================
    // SECTION 2: WORKFORCE & CONTRACTORS
    // =========================================================================

    private final RowMapper<Contractor> contractorRowMapper = (rs, rowNum) -> {
        Contractor c = new Contractor();
        c.setId(rs.getInt("contractor_id"));
        c.setCompanyName(rs.getString("company_name"));
        c.setContactPerson(rs.getString("contact_person"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) c.setCreatedAt(createdAt.toLocalDateTime());
        return c;
    };

    private final RowMapper<Worker> workerRowMapper = (rs, rowNum) -> {
        Worker w = new Worker();
        w.setId(rs.getInt("worker_id"));
        w.setFullName(rs.getString("full_name"));
        w.setEmail(rs.getString("email"));
        w.setPhone(rs.getString("phone"));
        w.setTrade(rs.getString("trade"));
        
        int cid = rs.getInt("contractor_id");
        if (!rs.wasNull()) w.setContractorId(cid);
        
        w.setContractorName(rs.getString("company_name"));
        w.setStatus(rs.getString("status"));
        
        Date joined = rs.getDate("joined_date");
        if (joined != null) w.setJoinedDate(joined.toLocalDate());
        return w;
    };

    private final RowMapper<ProjectWorker> projectWorkerRowMapper = (rs, rowNum) -> {
        ProjectWorker pw = new ProjectWorker();
        pw.setProjectId(rs.getInt("project_id"));
        pw.setWorkerId(rs.getInt("worker_id"));
        pw.setWorkerName(rs.getString("full_name"));
        pw.setTrade(rs.getString("trade"));
        pw.setRoleInProject(rs.getString("role_in_project"));
        
        Date assigned = rs.getDate("assigned_date");
        if (assigned != null) pw.setAssignedDate(assigned.toLocalDate());
        return pw;
    };

    public List<Contractor> findAllContractors() {
        return jdbcTemplate.query("SELECT * FROM contractors ORDER BY contractor_id ASC", contractorRowMapper);
    }

    public Optional<Contractor> findContractorById(Integer id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT * FROM contractors WHERE contractor_id = ?", contractorRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer createContractor(Contractor c) {
        String sql = "INSERT INTO contractors (company_name, contact_person, email, phone, address) VALUES (?, ?, ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getCompanyName());
            ps.setString(2, c.getContactPerson());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getAddress());
            return ps;
        }, kh);
        return kh.getKey() != null ? kh.getKey().intValue() : null;
    }

    public void updateContractor(Contractor c) {
        String sql = "UPDATE contractors SET company_name = ?, contact_person = ?, email = ?, phone = ?, address = ? WHERE contractor_id = ?";
        jdbcTemplate.update(sql, c.getCompanyName(), c.getContactPerson(), c.getEmail(), c.getPhone(), c.getAddress(), c.getId());
    }

    public List<Worker> findAllWorkers(String status, Integer contractorId) {
        StringBuilder sql = new StringBuilder("""
            SELECT w.*, c.company_name
            FROM workers w
            LEFT JOIN contractors c ON w.contractor_id = c.contractor_id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            sql.append(" AND w.status = ?");
            params.add(status);
        }
        if (contractorId != null) {
            sql.append(" AND w.contractor_id = ?");
            params.add(contractorId);
        }
        sql.append(" ORDER BY w.worker_id ASC");
        return jdbcTemplate.query(sql.toString(), workerRowMapper, params.toArray());
    }

    public Optional<Worker> findWorkerById(Integer id) {
        String sql = """
            SELECT w.*, c.company_name
            FROM workers w
            LEFT JOIN contractors c ON w.contractor_id = c.contractor_id
            WHERE w.worker_id = ?
        """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, workerRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer createWorker(Worker w) {
        String sql = """
            INSERT INTO workers (full_name, email, phone, trade, contractor_id, status, joined_date)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, w.getFullName());
            ps.setString(2, w.getEmail());
            ps.setString(3, w.getPhone());
            ps.setString(4, w.getTrade());
            if (w.getContractorId() != null) ps.setInt(5, w.getContractorId());
            else ps.setNull(5, java.sql.Types.INTEGER);
            ps.setString(6, w.getStatus() != null ? w.getStatus() : "ACTIVE");
            ps.setDate(7, w.getJoinedDate() != null ? Date.valueOf(w.getJoinedDate()) : Date.valueOf(LocalDate.now()));
            return ps;
        }, kh);
        return kh.getKey() != null ? kh.getKey().intValue() : null;
    }

    public void updateWorker(Worker w) {
        String sql = """
            UPDATE workers
            SET full_name = ?, email = ?, phone = ?, trade = ?, contractor_id = ?, status = ?
            WHERE worker_id = ?
        """;
        jdbcTemplate.update(sql,
            w.getFullName(),
            w.getEmail(),
            w.getPhone(),
            w.getTrade(),
            w.getContractorId(),
            w.getStatus(),
            w.getId()
        );
    }

    public List<ProjectWorker> findWorkersByProjectId(Integer projectId) {
        String sql = """
            SELECT pw.*, w.full_name, w.trade
            FROM project_workers pw
            JOIN workers w ON pw.worker_id = w.worker_id
            WHERE pw.project_id = ?
            ORDER BY pw.assigned_date DESC
        """;
        return jdbcTemplate.query(sql, projectWorkerRowMapper, projectId);
    }

    public void assignWorkerToProject(Integer projectId, Integer workerId, String roleInProject) {
        String sql = """
            INSERT INTO project_workers (project_id, worker_id, role_in_project, assigned_date)
            VALUES (?, ?, ?, CURDATE())
            ON DUPLICATE KEY UPDATE role_in_project = VALUES(role_in_project)
        """;
        jdbcTemplate.update(sql, projectId, workerId, roleInProject != null ? roleInProject : "Site Worker");
    }

    public void removeWorkerFromProject(Integer projectId, Integer workerId) {
        jdbcTemplate.update("DELETE FROM project_workers WHERE project_id = ? AND worker_id = ?", projectId, workerId);
    }

    // =========================================================================
    // SECTION 3: TASKS
    // =========================================================================

    private final RowMapper<Task> taskRowMapper = (rs, rowNum) -> {
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

    public List<Task> findAllTasks(Integer projectId, String status, Integer workerId) {
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
        return jdbcTemplate.query(sql.toString(), taskRowMapper, params.toArray());
    }

    public Optional<Task> findTaskById(Integer id) {
        String sql = """
            SELECT t.*, p.project_name, w.full_name AS worker_name
            FROM tasks t
            JOIN projects p ON t.project_id = p.project_id
            LEFT JOIN workers w ON t.assigned_worker_id = w.worker_id
            WHERE t.task_id = ?
        """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, taskRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer createTask(Task t) {
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

    public void updateTask(Task t) {
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

    public void updateTaskStatus(Integer id, String status, Integer completionPercent) {
        if (completionPercent != null) {
            jdbcTemplate.update("UPDATE tasks SET status = ?, completion_percent = ? WHERE task_id = ?", status, completionPercent, id);
        } else {
            jdbcTemplate.update("UPDATE tasks SET status = ? WHERE task_id = ?", status, id);
        }
    }

    public void assignTaskWorker(Integer id, Integer workerId) {
        jdbcTemplate.update("UPDATE tasks SET assigned_worker_id = ? WHERE task_id = ?", workerId, id);
    }

    // =========================================================================
    // SECTION 4: ATTENDANCE
    // =========================================================================

    private final RowMapper<Attendance> attendanceRowMapper = (rs, rowNum) -> {
        Attendance a = new Attendance();
        a.setId(rs.getInt("attendance_id"));
        a.setWorkerId(rs.getInt("worker_id"));
        a.setWorkerName(rs.getString("worker_name"));
        a.setProjectId(rs.getInt("project_id"));
        a.setProjectName(rs.getString("project_name"));
        
        Date d = rs.getDate("attendance_date");
        if (d != null) a.setAttendanceDate(d.toLocalDate());
        
        a.setStatus(rs.getString("status"));
        a.setRemarks(rs.getString("remarks"));
        
        long recBy = rs.getLong("recorded_by");
        if (!rs.wasNull()) a.setRecordedBy(recBy);
        a.setRecorderName(rs.getString("recorder_name"));
        
        Timestamp cAt = rs.getTimestamp("created_at");
        if (cAt != null) a.setCreatedAt(cAt.toLocalDateTime());
        
        Timestamp uAt = rs.getTimestamp("updated_at");
        if (uAt != null) a.setUpdatedAt(uAt.toLocalDateTime());
        
        return a;
    };

    public List<Attendance> findAllAttendance(Integer projectId, LocalDate date, Integer workerId) {
        StringBuilder sql = new StringBuilder("""
            SELECT a.*, w.full_name AS worker_name, p.project_name, u.full_name AS recorder_name
            FROM attendance a
            JOIN workers w ON a.worker_id = w.worker_id
            JOIN projects p ON a.project_id = p.project_id
            LEFT JOIN users u ON a.recorded_by = u.user_id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();
        if (projectId != null) {
            sql.append(" AND a.project_id = ?");
            params.add(projectId);
        }
        if (date != null) {
            sql.append(" AND a.attendance_date = ?");
            params.add(Date.valueOf(date));
        }
        if (workerId != null) {
            sql.append(" AND a.worker_id = ?");
            params.add(workerId);
        }
        sql.append(" ORDER BY a.attendance_date DESC, a.attendance_id DESC");
        return jdbcTemplate.query(sql.toString(), attendanceRowMapper, params.toArray());
    }

    public Optional<Attendance> findAttendanceById(Integer id) {
        String sql = """
            SELECT a.*, w.full_name AS worker_name, p.project_name, u.full_name AS recorder_name
            FROM attendance a
            JOIN workers w ON a.worker_id = w.worker_id
            JOIN projects p ON a.project_id = p.project_id
            LEFT JOIN users u ON a.recorded_by = u.user_id
            WHERE a.attendance_id = ?
        """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, attendanceRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void recordAttendance(Attendance a) {
        String sql = """
            INSERT INTO attendance (worker_id, project_id, attendance_date, status, remarks, recorded_by)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE status = VALUES(status), remarks = VALUES(remarks), recorded_by = VALUES(recorded_by)
        """;
        jdbcTemplate.update(sql,
            a.getWorkerId(),
            a.getProjectId(),
            Date.valueOf(a.getAttendanceDate()),
            a.getStatus(),
            a.getRemarks(),
            a.getRecordedBy()
        );
    }

    public void correctAttendance(Integer id, String status, String remarks, Long recordedBy) {
        String sql = "UPDATE attendance SET status = ?, remarks = ?, recorded_by = ? WHERE attendance_id = ?";
        jdbcTemplate.update(sql, status, remarks, recordedBy, id);
    }
}
