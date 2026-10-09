package com.csims.repository;

import com.csims.model.Incident;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class IncidentRepository {
    private final JdbcTemplate jdbcTemplate;

    public IncidentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Incident> rowMapper = (rs, rowNum) -> {
        Incident i = new Incident();
        i.setId(rs.getInt("incident_id"));
        i.setProjectId(rs.getInt("project_id"));
        i.setProjectName(rs.getString("project_name"));
        
        Timestamp incDate = rs.getTimestamp("incident_date");
        if (incDate != null) i.setIncidentDate(incDate.toLocalDateTime());
        
        i.setSeverity(rs.getString("severity"));
        i.setTitle(rs.getString("title"));
        i.setDescription(rs.getString("description"));
        
        long invId = rs.getLong("investigator_id");
        if (!rs.wasNull()) i.setInvestigatorId(invId);
        i.setInvestigatorName(rs.getString("investigator_name"));
        
        i.setActionsTaken(rs.getString("actions_taken"));
        i.setStatus(rs.getString("status"));
        
        long rBy = rs.getLong("reported_by");
        if (!rs.wasNull()) i.setReportedBy(rBy);
        i.setReporterName(rs.getString("reporter_name"));
        
        Timestamp cAt = rs.getTimestamp("created_at");
        if (cAt != null) i.setCreatedAt(cAt.toLocalDateTime());
        
        Timestamp uAt = rs.getTimestamp("updated_at");
        if (uAt != null) i.setUpdatedAt(uAt.toLocalDateTime());
        return i;
    };

    public List<Incident> findAll(Integer projectId, String severity, String status) {
        StringBuilder sql = new StringBuilder("""
            SELECT inc.*, p.project_name, u1.full_name AS investigator_name, u2.full_name AS reporter_name
            FROM incidents inc
            JOIN projects p ON inc.project_id = p.project_id
            LEFT JOIN users u1 ON inc.investigator_id = u1.user_id
            LEFT JOIN users u2 ON inc.reported_by = u2.user_id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();
        if (projectId != null) {
            sql.append(" AND inc.project_id = ?");
            params.add(projectId);
        }
        if (severity != null && !severity.isBlank()) {
            sql.append(" AND inc.severity = ?");
            params.add(severity);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND inc.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY inc.incident_date DESC, inc.incident_id DESC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public Optional<Incident> findById(Integer id) {
        String sql = """
            SELECT inc.*, p.project_name, u1.full_name AS investigator_name, u2.full_name AS reporter_name
            FROM incidents inc
            JOIN projects p ON inc.project_id = p.project_id
            LEFT JOIN users u1 ON inc.investigator_id = u1.user_id
            LEFT JOIN users u2 ON inc.reported_by = u2.user_id
            WHERE inc.incident_id = ?
        """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer create(Incident inc) {
        String sql = """
            INSERT INTO incidents (project_id, incident_date, severity, title, description, investigator_id, actions_taken, status, reported_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, inc.getProjectId());
            ps.setTimestamp(2, inc.getIncidentDate() != null ? Timestamp.valueOf(inc.getIncidentDate()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(3, inc.getSeverity());
            ps.setString(4, inc.getTitle());
            ps.setString(5, inc.getDescription());
            if (inc.getInvestigatorId() != null) ps.setLong(6, inc.getInvestigatorId());
            else ps.setNull(6, java.sql.Types.INTEGER);
            ps.setString(7, inc.getActionsTaken());
            ps.setString(8, inc.getStatus() != null ? inc.getStatus() : "Reported");
            if (inc.getReportedBy() != null) ps.setLong(9, inc.getReportedBy());
            else ps.setNull(9, java.sql.Types.INTEGER);
            return ps;
        }, kh);
        return kh.getKey() != null ? kh.getKey().intValue() : null;
    }

    public void update(Incident inc) {
        String sql = """
            UPDATE incidents
            SET severity = ?, title = ?, description = ?, investigator_id = ?, actions_taken = ?, status = ?
            WHERE incident_id = ?
        """;
        jdbcTemplate.update(sql,
            inc.getSeverity(),
            inc.getTitle(),
            inc.getDescription(),
            inc.getInvestigatorId(),
            inc.getActionsTaken(),
            inc.getStatus(),
            inc.getId()
        );
    }
}
