package com.csims.repository;

import com.csims.model.MaintenanceRecord;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MaintenanceRepository {
    private final InventoryRepository inventoryRepository;

    public MaintenanceRepository(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<MaintenanceRecord> findAll(Integer equipmentId, String status) {
        return inventoryRepository.findAllMaintenance(equipmentId, status);
    }

    public Optional<MaintenanceRecord> findById(Integer id) {
        return inventoryRepository.findMaintenanceById(id);
    }

    public Integer create(MaintenanceRecord m) {
        return inventoryRepository.createMaintenance(m);
    }

    public void update(MaintenanceRecord m) {
        inventoryRepository.updateMaintenance(m);
    }
}
