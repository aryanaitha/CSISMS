package com.csims.repository;

import com.csims.model.Models.Equipment;
import com.csims.model.Models.InventoryTransaction;
import com.csims.model.Models.MaintenanceRecord;
import com.csims.model.Models.Material;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Consolidated Inventory & Assets Repository.
 * Covers Materials, Inventory Transactions, Equipment Registry, and Maintenance.
 */
@Repository
public class InventoryRepository {
    private final JdbcTemplate jdbcTemplate;

    public InventoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================================================================
    // SECTION 1: MATERIALS & INVENTORY TRANSACTIONS
    // =========================================================================

    private final RowMapper<Material> materialRowMapper = (rs, rowNum) -> {
        Material m = new Material();
        m.setId(rs.getInt("material_id"));
        m.setMaterialCode(rs.getString("material_code"));
        m.setName(rs.getString("name"));
        m.setCategory(rs.getString("category"));
        m.setUnit(rs.getString("unit"));
        m.setQuantityOnHand(rs.getBigDecimal("quantity_on_hand"));
        m.setMinStock(rs.getBigDecimal("min_stock"));
        m.setMaxStock(rs.getBigDecimal("max_stock"));
        m.setUnitPrice(rs.getBigDecimal("unit_price"));
        m.setLocation(rs.getString("location"));
        
        Timestamp cAt = rs.getTimestamp("created_at");
        if (cAt != null) m.setCreatedAt(cAt.toLocalDateTime());
        
        Timestamp uAt = rs.getTimestamp("updated_at");
        if (uAt != null) m.setUpdatedAt(uAt.toLocalDateTime());
        return m;
    };

    private final RowMapper<InventoryTransaction> transactionRowMapper = (rs, rowNum) -> {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setId(rs.getInt("transaction_id"));
        tx.setMaterialId(rs.getInt("material_id"));
        tx.setMaterialName(rs.getString("material_name"));
        tx.setMaterialCode(rs.getString("material_code"));
        tx.setTransactionType(rs.getString("transaction_type"));
        tx.setQuantity(rs.getBigDecimal("quantity"));
        
        int pid = rs.getInt("project_id");
        if (!rs.wasNull()) tx.setProjectId(pid);
        tx.setProjectName(rs.getString("project_name"));
        
        tx.setReferenceNo(rs.getString("reference_no"));
        tx.setRemarks(rs.getString("remarks"));
        
        long pBy = rs.getLong("performed_by");
        if (!rs.wasNull()) tx.setPerformedBy(pBy);
        tx.setPerformerName(rs.getString("performer_name"));
        
        Timestamp tDate = rs.getTimestamp("transaction_date");
        if (tDate != null) tx.setTransactionDate(tDate.toLocalDateTime());
        return tx;
    };

    public List<Material> findAllMaterials(String category, Boolean lowStockOnly) {
        StringBuilder sql = new StringBuilder("SELECT * FROM materials WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (category != null && !category.isBlank()) {
            sql.append("AND category = ? ");
            params.add(category);
        }
        if (Boolean.TRUE.equals(lowStockOnly)) {
            sql.append("AND quantity_on_hand <= min_stock ");
        }
        sql.append("ORDER BY material_id ASC");
        return jdbcTemplate.query(sql.toString(), materialRowMapper, params.toArray());
    }

    public Optional<Material> findMaterialById(Integer id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT * FROM materials WHERE material_id = ?", materialRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer createMaterial(Material m) {
        String sql = """
            INSERT INTO materials (material_code, name, category, unit, quantity_on_hand, min_stock, max_stock, unit_price, location)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, m.getMaterialCode());
            ps.setString(2, m.getName());
            ps.setString(3, m.getCategory());
            ps.setString(4, m.getUnit());
            ps.setBigDecimal(5, m.getQuantityOnHand() != null ? m.getQuantityOnHand() : BigDecimal.ZERO);
            ps.setBigDecimal(6, m.getMinStock() != null ? m.getMinStock() : BigDecimal.ZERO);
            ps.setBigDecimal(7, m.getMaxStock() != null ? m.getMaxStock() : BigDecimal.ZERO);
            ps.setBigDecimal(8, m.getUnitPrice() != null ? m.getUnitPrice() : BigDecimal.ZERO);
            ps.setString(9, m.getLocation());
            return ps;
        }, kh);
        return kh.getKey() != null ? kh.getKey().intValue() : null;
    }

    public void updateMaterial(Material m) {
        String sql = """
            UPDATE materials
            SET name = ?, category = ?, unit = ?, min_stock = ?, max_stock = ?, unit_price = ?, location = ?
            WHERE material_id = ?
        """;
        jdbcTemplate.update(sql,
            m.getName(),
            m.getCategory(),
            m.getUnit(),
            m.getMinStock(),
            m.getMaxStock(),
            m.getUnitPrice(),
            m.getLocation(),
            m.getId()
        );
    }

    public void adjustStock(Integer id, BigDecimal delta) {
        jdbcTemplate.update("UPDATE materials SET quantity_on_hand = quantity_on_hand + ? WHERE material_id = ?", delta, id);
    }

    public void recordTransaction(InventoryTransaction tx) {
        String sql = """
            INSERT INTO inventory_transactions (material_id, transaction_type, quantity, project_id, reference_no, remarks, performed_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
            tx.getMaterialId(),
            tx.getTransactionType(),
            tx.getQuantity(),
            tx.getProjectId(),
            tx.getReferenceNo(),
            tx.getRemarks(),
            tx.getPerformedBy()
        );
    }

    public List<InventoryTransaction> findTransactions(Integer materialId, Integer projectId) {
        StringBuilder sql = new StringBuilder("""
            SELECT it.*, m.name AS material_name, m.material_code, p.project_name, u.full_name AS performer_name
            FROM inventory_transactions it
            JOIN materials m ON it.material_id = m.material_id
            LEFT JOIN projects p ON it.project_id = p.project_id
            LEFT JOIN users u ON it.performed_by = u.user_id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();
        if (materialId != null) {
            sql.append(" AND it.material_id = ?");
            params.add(materialId);
        }
        if (projectId != null) {
            sql.append(" AND it.project_id = ?");
            params.add(projectId);
        }
        sql.append(" ORDER BY it.transaction_date DESC LIMIT 100");
        return jdbcTemplate.query(sql.toString(), transactionRowMapper, params.toArray());
    }

    // =========================================================================
    // SECTION 2: EQUIPMENT
    // =========================================================================

    private final RowMapper<Equipment> equipmentRowMapper = (rs, rowNum) -> {
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

    public List<Equipment> findAllEquipment(String status, String category) {
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
        return jdbcTemplate.query(sql.toString(), equipmentRowMapper, params.toArray());
    }

    public Optional<Equipment> findEquipmentById(Integer id) {
        String sql = """
            SELECT e.*, p.project_name
            FROM equipment e
            LEFT JOIN projects p ON e.project_id = p.project_id
            WHERE e.equipment_id = ?
        """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, equipmentRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer createEquipment(Equipment e) {
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

    public void updateEquipment(Equipment e) {
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

    public void updateEquipmentStatus(Integer id, String status) {
        jdbcTemplate.update("UPDATE equipment SET status = ? WHERE equipment_id = ?", status, id);
    }

    public void updateEquipmentServiceDates(Integer id, LocalDate lastService, LocalDate nextDue) {
        jdbcTemplate.update("UPDATE equipment SET status = 'Available', last_service_date = ?, next_service_due = ? WHERE equipment_id = ?",
            Date.valueOf(lastService), Date.valueOf(nextDue), id);
    }

    // =========================================================================
    // SECTION 3: MAINTENANCE
    // =========================================================================

    private final RowMapper<MaintenanceRecord> maintenanceRowMapper = (rs, rowNum) -> {
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

    public List<MaintenanceRecord> findAllMaintenance(Integer equipmentId, String status) {
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
        return jdbcTemplate.query(sql.toString(), maintenanceRowMapper, params.toArray());
    }

    public Optional<MaintenanceRecord> findMaintenanceById(Integer id) {
        String sql = """
            SELECT m.*, e.name AS equipment_name, e.equipment_code
            FROM maintenance m
            JOIN equipment e ON m.equipment_id = e.equipment_id
            WHERE m.maintenance_id = ?
        """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, maintenanceRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer createMaintenance(MaintenanceRecord m) {
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

    public void updateMaintenance(MaintenanceRecord m) {
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
