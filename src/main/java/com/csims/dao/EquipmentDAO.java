package com.csims.dao;

import com.csims.model.Equipment;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EquipmentDAO {
    Optional<Equipment> findById(int equipmentId);
    Optional<Equipment> findByCode(String equipmentCode);
    List<Equipment> findAll();
    List<Equipment> search(String keyword, String category, String status, Integer projectId);
    int create(Equipment equipment);
    boolean update(Equipment equipment);
    boolean updateStatus(int equipmentId, String status);
    boolean assignProject(int equipmentId, Integer projectId, String status);
    boolean updateServiceDates(int equipmentId, LocalDate lastServiceDate, LocalDate nextServiceDue);
    boolean delete(int equipmentId);
    Map<String, Integer> countByStatus();
    int countTotal();
}
