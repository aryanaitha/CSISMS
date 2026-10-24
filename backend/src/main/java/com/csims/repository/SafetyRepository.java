package com.csims.repository;

import com.csims.model.Incident;
import com.csims.model.SafetyInspection;
import com.csims.model.SafetyViolation;
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

/**
 * Consolidated Safety & Compliance Repository.
 * Covers Safety Inspections, Violations, and Incident Reports.
 */
@Repository
public class SafetyRepository {
    private final JdbcTemplate jdbcTemplate;

    public SafetyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================================================================
    // SECTION 1: SAFETY INSPECTIONS
    // =========================================================================

    private final RowMapper<SafetyInspection> inspectionRowMapper = (rs, rowNum) -> {
        SafetyInspection si = new SafetyInspection();
        si.setId(rs.getInt("inspection_id"));
        si.setProjectId(rs.getInt("project_id"));
        si.setProjectName(rs.getString("project_name"));
        si.setInspectorId(rs.getLong("inspector_id"));
        si.setInspectorName(rs.getString("inspector_name"));
        
        Date d = rs.getDate("inspection_date");
        if (d != null) si.setInspectionDate(d.toLocalDate());
        
        si.setPpeScore(rs.getInt("ppe_score"));
        si.setWorkerSafetyScore(rs.getInt("worker_safety_score"));
        si.setEquipmentScore(rs.getInt("equipment_score"));
        si.setWorkAreaScore(rs.getInt("work_area_score"));
        si.setEmergencyScore(rs.getInt("emergency_score"));
        si.setOverallScore(rs.getBigDecimal("overall_score"));
        si.setRemarks(rs.getString("remarks"));
        
        Timestamp cAt = rs.getTimestamp("created_at");
        if (cAt != null) si.setCreatedAt(cAt.toLocalDateTime());
        return si;
    };

    public List<SafetyInspection> findAllInspections(Integer projectId) {
        StringBuilder sql = new StringBuilder("""
            SELECT si.*, p.project_name, u.full_name AS inspector_name
            FROM safety_inspections si
            JOIN projects p ON si.project_id = p.project_id
            JOIN users u ON si.inspector_id = u.user_id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();
        if (projectId != null) {
            sql.append(" AND si.project_id = ?");
            params.add(projectId);
        }
        sql.append(" ORDER BY si.inspection_date DESC, si.inspection_id DESC");
        return jdbcTemplate.query(sql.toString(), inspectionRowMapper, params.toArray());
    }

    public Optional<SafetyInspection> findInspectionById(Integer id) {
        String sql = """
            SELECT si.*, p.project_name, u.full_name AS inspector_name
            FROM safety_inspections si
            JOIN projects p ON si.project_id = p.project_id
            JOIN users u ON si.inspector_id = u.user_id
            WHERE si.inspection_id = ?
        """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, inspectionRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer createInspection(SafetyInspection si) {
        String sql = """
            INSERT INTO safety_inspections (project_id, inspector_id, inspection_date, ppe_score, worker_safety_score, equipment_score, work_area_score, emergency_score, overall_score, remarks)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, si.getProjectId());
            ps.setLong(2, si.getInspectorId());
            ps.setDate(3, Date.valueOf(si.getInspectionDate()));
            ps.setInt(4, si.getPpeScore());
            ps.setInt(5, si.getWorkerSafetyScore());
            ps.setInt(6, si.getEquipmentScore());
            ps.setInt(7, si.getWorkAreaScore());
            ps.setInt(8, si.getEmergencyScore());
            ps.setBigDecimal(9, si.getOverallScore());
            ps.setString(10, si.getRemarks());
            return ps;
        }, kh);
        return kh.getKey() != null ? kh.getKey().intValue() : null;
    }

    // =========================================================================
    // SECTION 2: SAFETY VIOLATIONS
    // =========================================================================

    private final RowMapper<SafetyViolation> violationRowMapper = (rs, rowNum) -> {
        SafetyViolation sv = new SafetyViolation();
        sv.setId(rs.getInt("violation_id"));
        
        int iid = rs.getInt("inspection_id");
        if (!rs.wasNull()) sv.setInspectionId(iid);
        
        sv.setProjectId(rs.getInt("project_id"));
        sv.setProjectName(rs.getString("project_name"));
        
        int wid = rs.getInt("worker_id");
        if (!rs.wasNull()) sv.setWorkerId(wid);
        sv.setWorkerName(rs.getString("worker_name"));
        
        sv.setSeverity(rs.getString("severity"));
        sv.setDescription(rs.getString("description"));
        sv.setCorrectiveAction(rs.getString("corrective_action"));
        sv.setStatus(rs.getString("status"));
        
        Date rDate = rs.getDate("resolution_date");
        if (rDate != null) sv.setResolutionDate(rDate.toLocalDate());
        
        long rBy = rs.getLong("reported_by");
        if (!rs.wasNull()) sv.setReportedBy(rBy);
        sv.setReporterName(rs.getString("reporter_name"));
        
        Timestamp cAt = rs.getTimestamp("created_at");
        if (cAt != null) sv.setCreatedAt(cAt.toLocalDateTime());
        return sv;
    };

    public List<SafetyViolation> findAllViolations(Integer projectId, String status, String severity) {
        StringBuilder sql = new StringBuilder("""
            SELECT sv.*, p.project_name, w.full_name AS worker_name, u.full_name AS reporter_name
            FROM safety_violations sv
            JOIN projects p ON sv.project_id = p.project_id
            LEFT JOIN workers w ON sv.worker_id = w.worker_id
            LEFT JOIN users u ON sv.reported_by = u.user_id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();
        if (projectId != null) {
            sql.append(" AND sv.project_id = ?");
            params.add(projectId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND sv.status = ?");
            params.add(status);
        }
        if (severity != null && !severity.isBlank()) {
            sql.append(" AND sv.severity = ?");
            params.add(severity);
        }
        sql.append(" ORDER BY sv.violation_id DESC");
        return jdbcTemplate.query(sql.toString(), violationRowMapper, params.toArray());
    }

    public Optional<SafetyViolation> findViolationById(Integer id) {
        String sql = """
            SELECT sv.*, p.project_name, w.full_name AS worker_name, u.full_name AS reporter_name
            FROM safety_violations sv
            JOIN projects p ON sv.project_id = p.project_id
            LEFT JOIN workers w ON sv.worker_id = w.worker_id
            LEFT JOIN users u ON sv.reported_by = u.user_id
            WHERE sv.violation_id = ?
        """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, violationRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer createViolation(SafetyViolation sv) {
        String sql = """
            INSERT INTO safety_violations (inspection_id, project_id, worker_id, severity, description, corrective_action, status, reported_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (sv.getInspectionId() != null) ps.setInt(1, sv.getInspectionId());
            else ps.setNull(1, java.sql.Types.INTEGER);
            ps.setInt(2, sv.getProjectId());
            if (sv.getWorkerId() != null) ps.setInt(3, sv.getWorkerId());
            else ps.setNull(3, java.sql.Types.INTEGER);
            ps.setString(4, sv.getSeverity());
            ps.setString(5, sv.getDescription());
            ps.setString(6, sv.getCorrectiveAction());
            ps.setString(7, sv.getStatus() != null ? sv.getStatus() : "OPEN");
            if (sv.getReportedBy() != null) ps.setLong(8, sv.getReportedBy());
            else ps.setNull(8, java.sql.Types.INTEGER);
            return ps;
        }, kh);
        return kh.getKey() != null ? kh.getKey().intValue() : null;
    }

    public void updateViolation(SafetyViolation sv) {
        String sql = """
            UPDATE safety_violations
            SET severity = ?, description = ?, corrective_action = ?, status = ?, resolution_date = ?
            WHERE violation_id = ?
        """;
        jdbcTemplate.update(sql,
            sv.getSeverity(),
            sv.getDescription(),
            sv.getCorrectiveAction(),
            sv.getStatus(),
            sv.getResolutionDate() != null ? Date.valueOf(sv.getResolutionDate()) : null,
            sv.getId()
        );
    }

    // =========================================================================
    // SECTION 3: INCIDENTS
    // =========================================================================

    private final RowMapper<Incident> incidentRowMapper = (rs, rowNum) -> {
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

    public List<Incident> findAllIncidents(Integer projectId, String severity, String status) {
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
        return jdbcTemplate.query(sql.toString(), incidentRowMapper, params.toArray());
    }

    public Optional<Incident> findIncidentById(Integer id) {
        String sql = """
            SELECT inc.*, p.project_name, u1.full_name AS investigator_name, u2.full_name AS reporter_name
            FROM incidents inc
            JOIN projects p ON inc.project_id = p.project_id
            LEFT JOIN users u1 ON inc.investigator_id = u1.user_id
            LEFT JOIN users u2 ON inc.reported_by = u2.user_id
            WHERE inc.incident_id = ?
        """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, incidentRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer createIncident(Incident inc) {
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

    public void updateIncident(Incident inc) {
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
