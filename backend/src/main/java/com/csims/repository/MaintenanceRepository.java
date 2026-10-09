package com.csims.repository;

import com.csims.model.MaintenanceRecord;
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
public class MaintenanceRepository {
    private final JdbcTemplate jdbcTemplate;

    public MaintenanceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MaintenanceRecord> rowMapper = (rs, rowNum) -> {
        MaintenanceRecord m = new MaintenanceRecord();
        m.setId(rs.getInt("maintenance_id"));
        m.setEquipmentId(rs.getInt("equipment_id"));
        m.setEquipmentName(rs.getString("equipment_name"));
        m.setEquipmentCode(rs.getString("equipment_code"));
        m.setMaintenanceType(rs.getString("maintenance_type"));
        m.setStatus(rs.getString("status"));
        
        Date sched = rs.getDate("scheduled_date");
        if (sched != null) m.setScheduledDate(sched.toLocalDate());
        
        Date comp = rs.getDate("completed_date");
        if (comp != null) m.setCompletedDate(comp.toLocalDate());
        
        m.setCost(rs.getBigDecimal("cost"));
        m.setTechnician(rs.getString("technician"));
        m.setNotes(rs.getString("notes"));
        
        Timestamp cAt = rs.getTimestamp("created_at");
        if (cAt != null) m.setCreatedAt(cAt.toLocalDateTime());
        return m;
    };

    public List<MaintenanceRecord> findAll(Integer equipmentId, String status) {
        StringBuilder sql = new StringBuilder("""
            SELECT m.*, e.name AS equipment_name, e.equipment_code
            FROM maintenance m
            JOIN equipment e ON m.equipment_id = e.equipment_id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();
        if (equipmentId != null) {
            sql.append(" AND m.equipment_id = ?");
            params.add(equipmentId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND m.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY m.scheduled_date DESC, m.maintenance_id DESC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public Optional<MaintenanceRecord> findById(Integer id) {
        String sql = """
            SELECT m.*, e.name AS equipment_name, e.equipment_code
            FROM maintenance m
            JOIN equipment e ON m.equipment_id = e.equipment_id
            WHERE m.maintenance_id = ?
        """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer create(MaintenanceRecord m) {
        String sql = """
            INSERT INTO maintenance (equipment_id, maintenance_type, status, scheduled_date, completed_date, cost, technician, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, m.getEquipmentId());
            ps.setString(2, m.getMaintenanceType());
            ps.setString(3, m.getStatus() != null ? m.getStatus() : "Due");
            ps.setDate(4, m.getScheduledDate() != null ? Date.valueOf(m.getScheduledDate()) : null);
            ps.setDate(5, m.getCompletedDate() != null ? Date.valueOf(m.getCompletedDate()) : null);
            ps.setBigDecimal(6, m.getCost());
            ps.setString(7, m.getTechnician());
            ps.setString(8, m.getNotes());
            return ps;
        }, kh);
        return kh.getKey() != null ? kh.getKey().intValue() : null;
    }

    public void update(MaintenanceRecord m) {
        String sql = """
            UPDATE maintenance
            SET maintenance_type = ?, status = ?, scheduled_date = ?, completed_date = ?, cost = ?, technician = ?, notes = ?
            WHERE maintenance_id = ?
        """;
        jdbcTemplate.update(sql,
            m.getMaintenanceType(),
            m.getStatus(),
            m.getScheduledDate() != null ? Date.valueOf(m.getScheduledDate()) : null,
            m.getCompletedDate() != null ? Date.valueOf(m.getCompletedDate()) : null,
            m.getCost(),
            m.getTechnician(),
            m.getNotes(),
            m.getId()
        );
    }
}
