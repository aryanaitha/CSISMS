package com.csims.repository;

import com.csims.model.InventoryTransaction;
import com.csims.model.Material;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MaterialRepository {
    private final JdbcTemplate jdbcTemplate;

    public MaterialRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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

    public List<Material> findAll(String category, Boolean lowStockOnly) {
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

    public Optional<Material> findById(Integer id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT * FROM materials WHERE material_id = ?", materialRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Integer create(Material m) {
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

    public void update(Material m) {
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
}
