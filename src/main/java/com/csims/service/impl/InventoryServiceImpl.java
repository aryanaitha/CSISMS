package com.csims.service.impl;

import com.csims.dao.InventoryTransactionDAO;
import com.csims.dao.MaterialDAO;
import com.csims.dao.impl.InventoryTransactionDAOImpl;
import com.csims.dao.impl.MaterialDAOImpl;
import com.csims.model.InventoryTransaction;
import com.csims.model.Material;
import com.csims.model.User;
import com.csims.security.PermissionChecker;
import com.csims.security.SessionContext;
import com.csims.service.AuditService;
import com.csims.service.InventoryService;
import com.csims.service.NotificationService;
import com.csims.util.DBConnection;
import com.csims.util.ValidationException;
import com.csims.util.Validator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class InventoryServiceImpl implements InventoryService {

    private final MaterialDAO materialDAO = new MaterialDAOImpl();
    private final InventoryTransactionDAO transactionDAO = new InventoryTransactionDAOImpl();
    private final AuditService auditService = new AuditServiceImpl();
    private final NotificationService notificationService = new NotificationServiceImpl();
    private final DBConnection dbConnection = DBConnection.getInstance();

    @Override
    public List<Material> getAllMaterials() {
        PermissionChecker.require("INVENTORY_VIEW");
        return materialDAO.findAll();
    }

    @Override
    public List<Material> searchMaterials(String keyword, String category, Boolean lowStockOnly) {
        PermissionChecker.require("INVENTORY_VIEW");
        return materialDAO.search(keyword, category, lowStockOnly);
    }

    @Override
    public Material getMaterialById(int materialId) {
        PermissionChecker.require("INVENTORY_VIEW");
        return materialDAO.findById(materialId)
            .orElseThrow(() -> new ValidationException("Material #" + materialId + " not found."));
    }

    @Override
    public int createMaterial(Material material) {
        PermissionChecker.require("INVENTORY_CREATE");
        Validator.requireNonEmpty(material.getMaterialCode(), "Material Code");
        Validator.requireNonEmpty(material.getName(), "Material Name");
        Validator.requireNonEmpty(material.getCategory(), "Category");
        Validator.requireNonEmpty(material.getUnit(), "Unit of Measurement");

        if (materialDAO.findByCode(material.getMaterialCode()).isPresent()) {
            throw new ValidationException("Material code '" + material.getMaterialCode() + "' already exists.");
        }

        int id = materialDAO.create(material);
        auditService.logCurrent("MATERIAL_CREATE", "INVENTORY", "MATERIAL", String.valueOf(id),
            null, material.getName() + " (" + material.getMaterialCode() + ")", "SUCCESS", "Added new material to catalog.");
        return id;
    }

    @Override
    public boolean updateMaterial(Material material) {
        PermissionChecker.require("INVENTORY_UPDATE");
        Validator.requireNonEmpty(material.getMaterialCode(), "Material Code");
        Validator.requireNonEmpty(material.getName(), "Material Name");

        Material old = getMaterialById(material.getMaterialId());
        boolean updated = materialDAO.update(material);
        if (updated) {
            auditService.logCurrent("MATERIAL_UPDATE", "INVENTORY", "MATERIAL", String.valueOf(material.getMaterialId()),
                old.getName(), material.getName(), "SUCCESS", "Updated material catalog details.");
        }
        return updated;
    }

    @Override
    public boolean deleteMaterial(int materialId) {
        PermissionChecker.require("INVENTORY_UPDATE");
        Material m = getMaterialById(materialId);
        boolean deleted = materialDAO.delete(materialId);
        if (deleted) {
            auditService.logCurrent("MATERIAL_DELETE", "INVENTORY", "MATERIAL", String.valueOf(materialId),
                m.getMaterialCode(), null, "SUCCESS", "Deleted material #" + materialId);
        }
        return deleted;
    }

    @Override
    public List<Material> getLowStockMaterials() {
        PermissionChecker.require("INVENTORY_VIEW");
        return materialDAO.findLowStock();
    }

    @Override
    public int recordTransaction(int materialId, String type, BigDecimal quantity, Integer projectId, String refNo, String remarks) {
        if ("RECEIPT".equalsIgnoreCase(type)) {
            PermissionChecker.require("INVENTORY_RECEIPT");
        } else if ("ISSUE".equalsIgnoreCase(type)) {
            PermissionChecker.require("INVENTORY_ISSUE");
        } else {
            PermissionChecker.require("INVENTORY_UPDATE");
        }

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Transaction quantity must be greater than zero.");
        }

        User user = SessionContext.getInstance().getCurrentUser();
        Integer userId = user != null ? user.getUserId() : null;

        Material material = getMaterialById(materialId);
        BigDecimal currentQty = material.getQuantityOnHand();
        BigDecimal newQty;

        if ("ISSUE".equalsIgnoreCase(type)) {
            if (currentQty.compareTo(quantity) < 0) {
                // Stock insufficient: must fail and audit the rejection
                auditService.logCurrent("INVENTORY_ISSUE_REJECTED", "INVENTORY", "MATERIAL", String.valueOf(materialId),
                    "Stock: " + currentQty, "Requested: " + quantity, "FAILED",
                    "Insufficient stock for material '" + material.getName() + "'. Requested: " + quantity + ", Available: " + currentQty);
                throw new ValidationException("Insufficient stock for material '" + material.getName() + "'. Available: " +
                                              currentQty + " " + material.getUnit() + ", Requested: " + quantity + " " + material.getUnit());
            }
            newQty = currentQty.subtract(quantity);
        } else if ("RECEIPT".equalsIgnoreCase(type)) {
            newQty = currentQty.add(quantity);
        } else { // ADJUSTMENT
            newQty = quantity;
        }

        // Execute DB Transaction (Commit / Rollback)
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Update material stock
                materialDAO.updateQuantityInTransaction(conn, materialId, newQty);

                // 2. Insert transaction movement record
                InventoryTransaction tx = new InventoryTransaction();
                tx.setMaterialId(materialId);
                tx.setTransactionType(type.toUpperCase());
                tx.setQuantity(quantity);
                tx.setProjectId(projectId);
                tx.setReferenceNo(refNo);
                tx.setRemarks(remarks);
                tx.setPerformedBy(userId);
                int txId = transactionDAO.createInTransaction(conn, tx);

                // Commit the atomic database transaction
                conn.commit();

                // 3. Log audit event
                auditService.logCurrent("INVENTORY_" + type.toUpperCase(), "INVENTORY", "TRANSACTION", String.valueOf(txId),
                    "Stock: " + currentQty, "New Stock: " + newQty, "SUCCESS",
                    type + " of " + quantity + " " + material.getUnit() + " for " + material.getName());

                // 4. Low stock alert notification if remaining quantity <= min stock
                if (newQty.compareTo(material.getMinStock()) <= 0) {
                    notificationService.notifyLowStock(material.getName(), material.getMaterialCode(),
                        newQty.doubleValue(), material.getMinStock().doubleValue());
                }

                return txId;

            } catch (Exception e) {
                conn.rollback();
                throw new ValidationException("Transaction failed and was rolled back: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection error during transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public List<InventoryTransaction> searchTransactions(Integer materialId, Integer projectId, String type, LocalDate startDate, LocalDate endDate) {
        PermissionChecker.require("INVENTORY_VIEW");
        return transactionDAO.search(materialId, projectId, type, startDate, endDate);
    }
}
