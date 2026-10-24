package com.csims.service;

import com.csims.model.Equipment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {
    private final InventoryService inventoryService;

    public EquipmentService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public List<Equipment> getAllEquipment(String status, String category) {
        return inventoryService.getAllEquipment(status, category);
    }

    public Equipment getEquipmentById(Integer id) {
        return inventoryService.getEquipmentById(id);
    }

    public Integer createEquipment(Equipment e, Long operatorId, String operatorUsername) {
        return inventoryService.createEquipment(e, operatorId, operatorUsername);
    }

    public void updateEquipment(Integer id, Equipment e, Long operatorId, String operatorUsername) {
        inventoryService.updateEquipment(id, e, operatorId, operatorUsername);
    }

    public void updateStatus(Integer id, String status, Long operatorId, String operatorUsername) {
        inventoryService.updateEquipmentStatus(id, status, operatorId, operatorUsername);
    }
}
