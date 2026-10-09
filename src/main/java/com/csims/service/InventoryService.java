package com.csims.service;

import com.csims.model.InventoryTransaction;
import com.csims.model.Material;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InventoryService {
    List<Material> getAllMaterials();
    List<Material> searchMaterials(String keyword, String category, Boolean lowStockOnly);
    Material getMaterialById(int materialId);
    int createMaterial(Material material);
    boolean updateMaterial(Material material);
    boolean deleteMaterial(int materialId);

    List<Material> getLowStockMaterials();

    /**
     * Executes atomic DB transaction for material movement (receipt, issue, adjustment).
     * Fails and rolls back if an issue quantity exceeds current on-hand stock.
     */
    int recordTransaction(int materialId, String type, BigDecimal quantity, Integer projectId, String refNo, String remarks);

    List<InventoryTransaction> searchTransactions(Integer materialId, Integer projectId, String type, LocalDate startDate, LocalDate endDate);
}
