package com.csims.dao.impl;

import com.csims.dao.InventoryTransactionDAO;
import com.csims.model.InventoryTransaction;
import com.csims.util.DBConnection;
import com.csims.util.DateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryTransactionDAOImpl implements InventoryTransactionDAO {

    private final DBConnection dbConnection = DBConnection.getInstance();

    private InventoryTransaction mapRow(ResultSet rs) throws SQLException {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setTransactionId(rs.getInt("transaction_id"));
        tx.setMaterialId(rs.getInt("material_id"));
        tx.setTransactionType(rs.getString("transaction_type"));
        tx.setQuantity(rs.getBigDecimal("quantity"));

        int pId = rs.getInt("project_id");
        if (!rs.wasNull()) tx.setProjectId(pId);

        tx.setReferenceNo(rs.getString("reference_no"));
        tx.setRemarks(rs.getString("remarks"));

        int perfBy = rs.getInt("performed_by");
        if (!rs.wasNull()) tx.setPerformedBy(perfBy);

        Timestamp tTs = rs.getTimestamp("transaction_date");
        if (tTs != null) tx.setTransactionDate(tTs.toLocalDateTime());

        try {
            tx.setMaterialName(rs.getString("material_name"));
            tx.setMaterialCode(rs.getString("material_code"));
            tx.setUnit(rs.getString("unit"));
        } catch (SQLException ignored) {}

        try {
            tx.setProjectName(rs.getString("project_name"));
        } catch (SQLException ignored) {}

        try {
            tx.setPerformerName(rs.getString("performer_name"));
        } catch (SQLException ignored) {}

        return tx;
    }

    @Override
    public Optional<InventoryTransaction> findById(int transactionId) {
        String sql = "SELECT it.*, m.name as material_name, m.material_code, m.unit, p.project_name, u.username as performer_name " +
                     "FROM inventory_transactions it " +
                     "JOIN materials m ON it.material_id = m.material_id " +
                     "LEFT JOIN projects p ON it.project_id = p.project_id " +
                     "LEFT JOIN users u ON it.performed_by = u.user_id " +
                     "WHERE it.transaction_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding inventory transaction: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<InventoryTransaction> findAll() {
        return search(null, null, null, null, null);
    }

    @Override
    public List<InventoryTransaction> search(Integer materialId, Integer projectId, String type, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder(
            "SELECT it.*, m.name as material_name, m.material_code, m.unit, p.project_name, u.username as performer_name " +
            "FROM inventory_transactions it " +
            "JOIN materials m ON it.material_id = m.material_id " +
            "LEFT JOIN projects p ON it.project_id = p.project_id " +
            "LEFT JOIN users u ON it.performed_by = u.user_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (materialId != null && materialId > 0) {
            sql.append("AND it.material_id = ? ");
            params.add(materialId);
        }

        if (projectId != null && projectId > 0) {
            sql.append("AND it.project_id = ? ");
            params.add(projectId);
        }

        if (type != null && !type.trim().isEmpty() && !type.equalsIgnoreCase("ALL")) {
            sql.append("AND it.transaction_type = ? ");
            params.add(type);
        }

        if (startDate != null) {
            sql.append("AND it.transaction_date >= ? ");
            params.add(DateUtil.toSqlTimestamp(startDate.atStartOfDay()));
        }

        if (endDate != null) {
            sql.append("AND it.transaction_date <= ? ");
            params.add(DateUtil.toSqlTimestamp(endDate.atTime(23, 59, 59)));
        }

        sql.append("ORDER BY it.transaction_date DESC");

        List<InventoryTransaction> list = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching transactions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int create(InventoryTransaction tx) {
        try (Connection conn = dbConnection.getConnection()) {
            return createInTransaction(conn, tx);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating inventory transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public int createInTransaction(Connection conn, InventoryTransaction tx) throws SQLException {
        String sql = "INSERT INTO inventory_transactions (material_id, transaction_type, quantity, project_id, " +
                     "reference_no, remarks, performed_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tx.getMaterialId());
            ps.setString(2, tx.getTransactionType());
            ps.setBigDecimal(3, tx.getQuantity());
            if (tx.getProjectId() != null) {
                ps.setInt(4, tx.getProjectId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, tx.getReferenceNo());
            ps.setString(6, tx.getRemarks());
            if (tx.getPerformedBy() != null) {
                ps.setInt(7, tx.getPerformedBy());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    tx.setTransactionId(id);
                    return id;
                }
            }
        }
        return 0;
    }
}
