package com.csims.service;

import com.csims.model.Equipment;
import java.util.List;
import java.util.Map;

public interface EquipmentService {
    List<Equipment> getAllEquipment();
    List<Equipment> searchEquipment(String keyword, String category, String status, Integer projectId);
    Equipment getEquipmentById(int equipmentId);
    int createEquipment(Equipment equipment);
    boolean updateEquipment(Equipment equipment);
    boolean updateStatus(int equipmentId, String status);
    boolean assignToProject(int equipmentId, Integer projectId, String status);
    boolean deleteEquipment(int equipmentId);
    Map<String, Integer> getEquipmentStats();
}
