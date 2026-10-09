package com.csims.service;

import com.csims.exception.ApiException;
import com.csims.model.InventoryTransaction;
import com.csims.model.Material;
import com.csims.repository.MaterialRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryService {
    private final MaterialRepository materialRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public InventoryService(MaterialRepository materialRepository,
                            AuditService auditService,
                            NotificationService notificationService) {
        this.materialRepository = materialRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public List<Material> getAllMaterials(String category, Boolean lowStockOnly) {
        return materialRepository.findAll(category, lowStockOnly);
    }

    public Material getMaterialById(Integer id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ApiException("Material not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer createMaterial(Material m, Long operatorId, String operatorUsername) {
        Integer id = materialRepository.create(m);
        auditService.log(operatorId, operatorUsername, null, "INVENTORY_CREATE", "INVENTORY", "MATERIAL", String.valueOf(id), null, m.getName(), "SUCCESS", "Added new material to catalog: " + m.getName() + " (" + m.getMaterialCode() + ")");
        return id;
    }

    @Transactional
    public void updateMaterial(Integer id, Material m, Long operatorId, String operatorUsername) {
        Material existing = getMaterialById(id);
        m.setId(id);
        materialRepository.update(m);
        auditService.log(operatorId, operatorUsername, null, "INVENTORY_UPDATE", "INVENTORY", "MATERIAL", String.valueOf(id), existing.getName(), m.getName(), "SUCCESS", "Updated material details for " + m.getName());
    }

    @Transactional
    public void recordReceipt(Integer materialId, BigDecimal quantity, String referenceNo, String remarks, Long operatorId, String operatorUsername) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Receipt quantity must be greater than zero", HttpStatus.BAD_REQUEST);
        }

        Material m = getMaterialById(materialId);
        materialRepository.adjustStock(materialId, quantity);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setMaterialId(materialId);
        tx.setTransactionType("RECEIPT");
        tx.setQuantity(quantity);
        tx.setReferenceNo(referenceNo);
        tx.setRemarks(remarks);
        tx.setPerformedBy(operatorId);
        materialRepository.recordTransaction(tx);

        auditService.log(operatorId, operatorUsername, null, "INVENTORY_RECEIPT", "INVENTORY", "MATERIAL", String.valueOf(materialId), String.valueOf(m.getQuantityOnHand()), String.valueOf(m.getQuantityOnHand().add(quantity)), "SUCCESS", "Received " + quantity + " " + m.getUnit() + " of " + m.getName());
    }

    @Transactional
    public void recordIssue(Integer materialId, BigDecimal quantity, Integer projectId, String referenceNo, String remarks, Long operatorId, String operatorUsername) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Issue quantity must be greater than zero", HttpStatus.BAD_REQUEST);
        }

        Material m = getMaterialById(materialId);

        // Atomic check: If stock is insufficient, throw exception and ROLL BACK
        if (m.getQuantityOnHand().compareTo(quantity) < 0) {
            String errorMsg = "Insufficient stock for " + m.getName() + ". Available: " + m.getQuantityOnHand() + " " + m.getUnit() + ", Requested: " + quantity + " " + m.getUnit();
            auditService.log(operatorId, operatorUsername, null, "INVENTORY_ISSUE_FAILED", "INVENTORY", "MATERIAL", String.valueOf(materialId), String.valueOf(m.getQuantityOnHand()), String.valueOf(quantity), "FAILURE", errorMsg);
            throw new ApiException(errorMsg, HttpStatus.BAD_REQUEST);
        }

        materialRepository.adjustStock(materialId, quantity.negate());

        InventoryTransaction tx = new InventoryTransaction();
        tx.setMaterialId(materialId);
        tx.setTransactionType("ISSUE");
        tx.setQuantity(quantity);
        tx.setProjectId(projectId);
        tx.setReferenceNo(referenceNo);
        tx.setRemarks(remarks);
        tx.setPerformedBy(operatorId);
        materialRepository.recordTransaction(tx);

        BigDecimal newStock = m.getQuantityOnHand().subtract(quantity);
        auditService.log(operatorId, operatorUsername, null, "INVENTORY_ISSUE", "INVENTORY", "MATERIAL", String.valueOf(materialId), String.valueOf(m.getQuantityOnHand()), String.valueOf(newStock), "SUCCESS", "Issued " + quantity + " " + m.getUnit() + " of " + m.getName() + " to project #" + projectId);

        // Trigger Low-Stock Notification if below minStock
        if (newStock.compareTo(m.getMinStock()) <= 0) {
            notificationService.notify(operatorId, "Low Stock Warning: " + m.getName(), "Material " + m.getName() + " reached low stock level (" + newStock + " " + m.getUnit() + "). Reorder required.", "WARNING", "/inventory");
        }
    }

    public List<InventoryTransaction> getTransactions(Integer materialId, Integer projectId) {
        return materialRepository.findTransactions(materialId, projectId);
    }
}
