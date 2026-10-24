package com.csims.repository;

import com.csims.model.InventoryTransaction;
import com.csims.model.Material;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class MaterialRepository {
    private final InventoryRepository inventoryRepository;

    public MaterialRepository(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Material> findAll(String category, Boolean lowStockOnly) {
        return inventoryRepository.findAllMaterials(category, lowStockOnly);
    }

    public Optional<Material> findById(Integer id) {
        return inventoryRepository.findMaterialById(id);
    }

    public Integer create(Material m) {
        return inventoryRepository.createMaterial(m);
    }

    public void update(Material m) {
        inventoryRepository.updateMaterial(m);
    }

    public void adjustStock(Integer id, BigDecimal delta) {
        inventoryRepository.adjustStock(id, delta);
    }

    public void recordTransaction(InventoryTransaction tx) {
        inventoryRepository.recordTransaction(tx);
    }

    public List<InventoryTransaction> findTransactions(Integer materialId, Integer projectId) {
        return inventoryRepository.findTransactions(materialId, projectId);
    }
}
