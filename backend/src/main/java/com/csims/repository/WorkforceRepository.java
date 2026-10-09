package com.csims.repository;

import com.csims.model.Contractor;
import com.csims.model.ProjectWorker;
import com.csims.model.Worker;
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
import java.util.List;
import java.util.Optional;

@Repository
public class WorkforceRepository {
    private final JdbcTemplate jdbcTemplate;

    public WorkforceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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

    // Contractors
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

    // Workers
    public List<Worker> findAllWorkers(String status, Integer contractorId) {
        StringBuilder sql = new StringBuilder("""
            SELECT w.*, c.company_name
            FROM workers w
            LEFT JOIN contractors c ON w.contractor_id = c.contractor_id
            WHERE 1=1
        """);
        if (status != null && !status.isBlank()) {
            sql.append(" AND w.status = '").append(status).append("'");
        }
        if (contractorId != null) {
            sql.append(" AND w.contractor_id = ").append(contractorId);
        }
        sql.append(" ORDER BY w.worker_id ASC");
        return jdbcTemplate.query(sql.toString(), workerRowMapper);
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
            ps.setDate(7, w.getJoinedDate() != null ? Date.valueOf(w.getJoinedDate()) : Date.valueOf(java.time.LocalDate.now()));
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

    // Project Workers
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
}
