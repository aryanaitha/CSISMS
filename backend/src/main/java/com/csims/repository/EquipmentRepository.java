package com.csims.repository;

import com.csims.model.Equipment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EquipmentRepository {
    private final JdbcTemplate jdbcTemplate;

    public EquipmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Equipment> rowMapper = (rs, rowNum) -> {
        Equipment e = new Equipment();
        e.setId(rs.getInt("equipment_id"));
        e.setEquipmentCode(rs.getString("equipment_code"));
        e.setName(rs.getString("name"));
        e.setCategory(rs.getString("category"));
        e.setStatus(rs.getString("status"));
        
        int pid = rs.getInt("project_id");
        if (!rs.wasNull()) e.setProjectId(pid);
        e.setProjectName(rs.getString("project_name"));
        
        Date pDate = rs.getDate("purchase_date");
        if (pDate != null) e.setPurchaseDate(pDate.toLocalDate());
        
        Date lsDate = rs.getDate("last_service_date");
        if (lsDate != null) e.setLastServiceDate(lsDate.toLocalDate());
        
        Date nsDate = rs.getDate("next_service_due");
        if (nsDate != null) e.setNextServiceDue(nsDate.toLocalDate());
        
        return e;
    };

    public List<Equipment> findAll(String status, String category) {
        StringBuilder sql = new StringBuilder("""
            SELECT e.*, p.project_name
            FROM equipment e
            LEFT JOIN projects p ON e.project_id = p.project_id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            sql.append(" AND e.status = ?");
            params.add(status);
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND e.category = ?");
            params.add(category);
        }
        sql.append(" ORDER BY e.equipment_id ASC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public Optional<Equipment> findById(Integer id) {
        String sql = """
            SELECT e.*, p.project_name
            FROM equipment e
            LEFT JOIN projects p ON e.project_id = p.project_id
            WHERE e.equipment_id = ?
        """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer create(Equipment e) {
        String sql = """
            INSERT INTO equipment (equipment_code, name, category, status, project_id, purchase_date, last_service_date, next_service_due)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, e.getEquipmentCode());
            ps.setString(2, e.getName());
            ps.setString(3, e.getCategory());
            ps.setString(4, e.getStatus() != null ? e.getStatus() : "Available");
            if (e.getProjectId() != null) ps.setInt(5, e.getProjectId());
            else ps.setNull(5, java.sql.Types.INTEGER);
            ps.setDate(6, e.getPurchaseDate() != null ? Date.valueOf(e.getPurchaseDate()) : null);
            ps.setDate(7, e.getLastServiceDate() != null ? Date.valueOf(e.getLastServiceDate()) : null);
            ps.setDate(8, e.getNextServiceDue() != null ? Date.valueOf(e.getNextServiceDue()) : null);
            return ps;
        }, kh);
        return kh.getKey() != null ? kh.getKey().intValue() : null;
    }

    public void update(Equipment e) {
        String sql = """
            UPDATE equipment
            SET name = ?, category = ?, status = ?, project_id = ?, last_service_date = ?, next_service_due = ?
            WHERE equipment_id = ?
        """;
        jdbcTemplate.update(sql,
            e.getName(),
            e.getCategory(),
            e.getStatus(),
            e.getProjectId(),
            e.getLastServiceDate() != null ? Date.valueOf(e.getLastServiceDate()) : null,
            e.getNextServiceDue() != null ? Date.valueOf(e.getNextServiceDue()) : null,
            e.getId()
        );
    }

    public void updateStatus(Integer id, String status) {
        jdbcTemplate.update("UPDATE equipment SET status = ? WHERE equipment_id = ?", status, id);
    }

    public void updateServiceDates(Integer id, LocalDate lastService, LocalDate nextDue) {
        jdbcTemplate.update("UPDATE equipment SET status = 'Available', last_service_date = ?, next_service_due = ? WHERE equipment_id = ?",
            Date.valueOf(lastService), Date.valueOf(nextDue), id);
    }
}
