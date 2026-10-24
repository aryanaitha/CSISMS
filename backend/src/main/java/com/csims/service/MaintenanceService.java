package com.csims.service;

import com.csims.model.MaintenanceRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceService {
    private final InventoryService inventoryService;

    public MaintenanceService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public List<MaintenanceRecord> getAllMaintenance(Integer equipmentId, String status) {
        return inventoryService.getAllMaintenance(equipmentId, status);
    }

    public MaintenanceRecord getMaintenanceById(Integer id) {
        return inventoryService.getMaintenanceById(id);
    }

    public Integer scheduleMaintenance(MaintenanceRecord m, Long operatorId, String operatorUsername) {
        return inventoryService.scheduleMaintenance(m, operatorId, operatorUsername);
    }

    public void updateMaintenance(Integer id, MaintenanceRecord m, Long operatorId, String operatorUsername) {
        inventoryService.updateMaintenance(id, m, operatorId, operatorUsername);
    }
}
