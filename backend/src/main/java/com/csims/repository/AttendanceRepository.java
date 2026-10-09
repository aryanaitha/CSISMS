package com.csims.repository;

import com.csims.model.Attendance;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AttendanceRepository {
    private final JdbcTemplate jdbcTemplate;

    public AttendanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Attendance> rowMapper = (rs, rowNum) -> {
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

    public List<Attendance> findAll(Integer projectId, LocalDate date, Integer workerId) {
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
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public Optional<Attendance> findById(Integer id) {
        String sql = """
            SELECT a.*, w.full_name AS worker_name, p.project_name, u.full_name AS recorder_name
            FROM attendance a
            JOIN workers w ON a.worker_id = w.worker_id
            JOIN projects p ON a.project_id = p.project_id
            LEFT JOIN users u ON a.recorded_by = u.user_id
            WHERE a.attendance_id = ?
        """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void record(Attendance a) {
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

    public void correct(Integer id, String status, String remarks, Long recordedBy) {
        String sql = "UPDATE attendance SET status = ?, remarks = ?, recorded_by = ? WHERE attendance_id = ?";
        jdbcTemplate.update(sql, status, remarks, recordedBy, id);
    }
}
