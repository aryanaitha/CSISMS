package com.csims.repository;

import com.csims.model.Equipment;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class EquipmentRepository {
    private final InventoryRepository inventoryRepository;

    public EquipmentRepository(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Equipment> findAll(String status, String category) {
        return inventoryRepository.findAllEquipment(status, category);
    }

    public Optional<Equipment> findById(Integer id) {
        return inventoryRepository.findEquipmentById(id);
    }

    public Integer create(Equipment e) {
        return inventoryRepository.createEquipment(e);
    }

    public void update(Equipment e) {
        inventoryRepository.updateEquipment(e);
    }

    public void updateStatus(Integer id, String status) {
        inventoryRepository.updateEquipmentStatus(id, status);
    }

    public void updateServiceDates(Integer id, LocalDate lastService, LocalDate nextDue) {
        inventoryRepository.updateEquipmentServiceDates(id, lastService, nextDue);
    }
}
