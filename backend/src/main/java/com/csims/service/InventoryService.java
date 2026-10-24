package com.csims.service;

import com.csims.exception.AppExceptions.ApiException;
import com.csims.model.Models.Equipment;
import com.csims.model.Models.InventoryTransaction;
import com.csims.model.Models.MaintenanceRecord;
import com.csims.model.Models.Material;
import com.csims.repository.InventoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Consolidated Inventory & Assets Service.
 * Covers Materials, Inventory Transactions, Equipment Registry, and Maintenance Operations.
 */
@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final SystemService systemService;

    public InventoryService(InventoryRepository inventoryRepository,
                            SystemService systemService) {
        this.inventoryRepository = inventoryRepository;
        this.systemService = systemService;
    }

    // =========================================================================
    // SECTION 1: MATERIALS & INVENTORY TRANSACTIONS
    // =========================================================================

    public List<Material> getAllMaterials(String category, Boolean lowStockOnly) {
        return inventoryRepository.findAllMaterials(category, lowStockOnly);
    }

    public Material getMaterialById(Integer id) {
        return inventoryRepository.findMaterialById(id)
                .orElseThrow(() -> new ApiException("Material not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer createMaterial(Material m, Long operatorId, String operatorUsername) {
        Integer id = inventoryRepository.createMaterial(m);
        systemService.log(operatorId, operatorUsername, null, "INVENTORY_CREATE", "INVENTORY", "MATERIAL", String.valueOf(id), null, m.getName(), "SUCCESS", "Added new material to catalog: " + m.getName() + " (" + m.getMaterialCode() + ")");
        return id;
    }

    @Transactional
    public void updateMaterial(Integer id, Material m, Long operatorId, String operatorUsername) {
        Material existing = getMaterialById(id);
        m.setId(id);
        inventoryRepository.updateMaterial(m);
        systemService.log(operatorId, operatorUsername, null, "INVENTORY_UPDATE", "INVENTORY", "MATERIAL", String.valueOf(id), existing.getName(), m.getName(), "SUCCESS", "Updated material details for " + m.getName());
    }

    @Transactional
    public void recordReceipt(Integer materialId, BigDecimal quantity, String referenceNo, String remarks, Long operatorId, String operatorUsername) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Receipt quantity must be greater than zero", HttpStatus.BAD_REQUEST);
        }

        Material m = getMaterialById(materialId);
        inventoryRepository.adjustStock(materialId, quantity);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setMaterialId(materialId);
        tx.setTransactionType("RECEIPT");
        tx.setQuantity(quantity);
        tx.setReferenceNo(referenceNo);
        tx.setRemarks(remarks);
        tx.setPerformedBy(operatorId);
        inventoryRepository.recordTransaction(tx);

        systemService.log(operatorId, operatorUsername, null, "INVENTORY_RECEIPT", "INVENTORY", "MATERIAL", String.valueOf(materialId), String.valueOf(m.getQuantityOnHand()), String.valueOf(m.getQuantityOnHand().add(quantity)), "SUCCESS", "Received " + quantity + " " + m.getUnit() + " of " + m.getName());
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
            systemService.log(operatorId, operatorUsername, null, "INVENTORY_ISSUE_FAILED", "INVENTORY", "MATERIAL", String.valueOf(materialId), String.valueOf(m.getQuantityOnHand()), String.valueOf(quantity), "FAILURE", errorMsg);
            throw new ApiException(errorMsg, HttpStatus.BAD_REQUEST);
        }

        inventoryRepository.adjustStock(materialId, quantity.negate());

        InventoryTransaction tx = new InventoryTransaction();
        tx.setMaterialId(materialId);
        tx.setTransactionType("ISSUE");
        tx.setQuantity(quantity);
        tx.setProjectId(projectId);
        tx.setReferenceNo(referenceNo);
        tx.setRemarks(remarks);
        tx.setPerformedBy(operatorId);
        inventoryRepository.recordTransaction(tx);

        BigDecimal remaining = m.getQuantityOnHand().subtract(quantity);
        systemService.log(operatorId, operatorUsername, null, "INVENTORY_ISSUE", "INVENTORY", "MATERIAL", String.valueOf(materialId), String.valueOf(m.getQuantityOnHand()), String.valueOf(remaining), "SUCCESS", "Issued " + quantity + " " + m.getUnit() + " of " + m.getName() + " to project #" + projectId);

        // Low stock threshold alert check
        if (remaining.compareTo(m.getMinStock()) <= 0) {
            systemService.log(null, "SYSTEM", "SYSTEM", "INVENTORY_LOW_STOCK", "INVENTORY", "MATERIAL", String.valueOf(materialId), null, String.valueOf(remaining), "WARNING", "Material " + m.getName() + " reached low stock threshold: " + remaining + " remaining");
            systemService.notify(1L, "Low Stock Warning", "Material " + m.getName() + " is at or below minimum stock level (" + remaining + " remaining).", "WARNING", "/inventory");
        }
    }

    public List<InventoryTransaction> getTransactions(Integer materialId, Integer projectId) {
        return inventoryRepository.findTransactions(materialId, projectId);
    }

    // =========================================================================
    // SECTION 2: EQUIPMENT
    // =========================================================================

    public List<Equipment> getAllEquipment(String status, String category) {
        return inventoryRepository.findAllEquipment(status, category);
    }

    public Equipment getEquipmentById(Integer id) {
        return inventoryRepository.findEquipmentById(id)
                .orElseThrow(() -> new ApiException("Equipment not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer createEquipment(Equipment e, Long operatorId, String operatorUsername) {
        Integer id = inventoryRepository.createEquipment(e);
        systemService.log(operatorId, operatorUsername, null, "EQUIPMENT_CREATE", "EQUIPMENT", "EQUIPMENT", String.valueOf(id), null, e.getName(), "SUCCESS", "Registered machinery: " + e.getName() + " (" + e.getEquipmentCode() + ")");
        return id;
    }

    @Transactional
    public void updateEquipment(Integer id, Equipment e, Long operatorId, String operatorUsername) {
        Equipment existing = getEquipmentById(id);
        e.setId(id);
        inventoryRepository.updateEquipment(e);
        systemService.log(operatorId, operatorUsername, null, "EQUIPMENT_UPDATE", "EQUIPMENT", "EQUIPMENT", String.valueOf(id), existing.getStatus(), e.getStatus(), "SUCCESS", "Updated equipment details for " + e.getName());
    }

    @Transactional
    public void updateEquipmentStatus(Integer id, String status, Long operatorId, String operatorUsername) {
        Equipment existing = getEquipmentById(id);
        inventoryRepository.updateEquipmentStatus(id, status);
        systemService.log(operatorId, operatorUsername, null, "EQUIPMENT_STATUS_UPDATE", "EQUIPMENT", "EQUIPMENT", String.valueOf(id), existing.getStatus(), status, "SUCCESS", "Updated equipment #" + id + " status to " + status);
    }

    // Compatibility method
    @Transactional
    public void updateStatus(Integer id, String status, Long operatorId, String operatorUsername) {
        updateEquipmentStatus(id, status, operatorId, operatorUsername);
    }

    // =========================================================================
    // SECTION 3: MAINTENANCE
    // =========================================================================

    public List<MaintenanceRecord> getAllMaintenance(Integer equipmentId, String status) {
        return inventoryRepository.findAllMaintenance(equipmentId, status);
    }

    public MaintenanceRecord getMaintenanceById(Integer id) {
        return inventoryRepository.findMaintenanceById(id)
                .orElseThrow(() -> new ApiException("Maintenance record not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer scheduleMaintenance(MaintenanceRecord m, Long operatorId, String operatorUsername) {
        inventoryRepository.updateEquipmentStatus(m.getEquipmentId(), "Maintenance");
        Integer id = inventoryRepository.createMaintenance(m);
        systemService.log(operatorId, operatorUsername, null, "MAINTENANCE_SCHEDULE", "MAINTENANCE", "MAINTENANCE", String.valueOf(id), null, m.getMaintenanceType(), "SUCCESS", "Scheduled maintenance for equipment #" + m.getEquipmentId());
        return id;
    }

    @Transactional
    public void updateMaintenance(Integer id, MaintenanceRecord m, Long operatorId, String operatorUsername) {
        MaintenanceRecord existing = getMaintenanceById(id);
        m.setId(id);
        
        if ("Completed".equalsIgnoreCase(m.getStatus())) {
            if (m.getCompletedDate() == null) {
                m.setCompletedDate(LocalDate.now());
            }
            LocalDate nextDue = LocalDate.now().plusDays(90);
            inventoryRepository.updateEquipmentServiceDates(existing.getEquipmentId(), m.getCompletedDate(), nextDue);
        }

        inventoryRepository.updateMaintenance(m);
        systemService.log(operatorId, operatorUsername, null, "MAINTENANCE_UPDATE", "MAINTENANCE", "MAINTENANCE", String.valueOf(id), existing.getStatus(), m.getStatus(), "SUCCESS", "Updated maintenance service order #" + id + " to " + m.getStatus());
    }
}
