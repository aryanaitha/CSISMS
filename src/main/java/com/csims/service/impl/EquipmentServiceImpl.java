package com.csims.service.impl;

import com.csims.dao.EquipmentDAO;
import com.csims.dao.impl.EquipmentDAOImpl;
import com.csims.model.Equipment;
import com.csims.security.PermissionChecker;
import com.csims.service.AuditService;
import com.csims.service.EquipmentService;
import com.csims.util.ValidationException;
import com.csims.util.Validator;

import java.util.List;
import java.util.Map;

public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentDAO equipmentDAO = new EquipmentDAOImpl();
    private final AuditService auditService = new AuditServiceImpl();

    @Override
    public List<Equipment> getAllEquipment() {
        PermissionChecker.require("EQUIPMENT_VIEW");
        return equipmentDAO.findAll();
    }

    @Override
    public List<Equipment> searchEquipment(String keyword, String category, String status, Integer projectId) {
        PermissionChecker.require("EQUIPMENT_VIEW");
        return equipmentDAO.search(keyword, category, status, projectId);
    }

    @Override
    public Equipment getEquipmentById(int equipmentId) {
        PermissionChecker.require("EQUIPMENT_VIEW");
        return equipmentDAO.findById(equipmentId)
            .orElseThrow(() -> new ValidationException("Equipment #" + equipmentId + " not found."));
    }

    @Override
    public int createEquipment(Equipment equipment) {
        PermissionChecker.require("EQUIPMENT_CREATE");
        Validator.requireNonEmpty(equipment.getEquipmentCode(), "Equipment Code");
        Validator.requireNonEmpty(equipment.getName(), "Equipment Name");
        Validator.requireNonEmpty(equipment.getCategory(), "Category");

        if (equipmentDAO.findByCode(equipment.getEquipmentCode()).isPresent()) {
            throw new ValidationException("Equipment code '" + equipment.getEquipmentCode() + "' already exists.");
        }

        int id = equipmentDAO.create(equipment);
        auditService.logCurrent("EQUIPMENT_CREATE", "EQUIPMENT", "EQUIPMENT", String.valueOf(id),
            null, equipment.getName() + " (" + equipment.getEquipmentCode() + ")", "SUCCESS", "Registered machinery/equipment.");
        return id;
    }

    @Override
    public boolean updateEquipment(Equipment equipment) {
        PermissionChecker.require("EQUIPMENT_UPDATE");
        Validator.requireNonEmpty(equipment.getEquipmentCode(), "Equipment Code");
        Validator.requireNonEmpty(equipment.getName(), "Equipment Name");

        Equipment old = getEquipmentById(equipment.getEquipmentId());
        boolean updated = equipmentDAO.update(equipment);
        if (updated) {
            auditService.logCurrent("EQUIPMENT_UPDATE", "EQUIPMENT", "EQUIPMENT", String.valueOf(equipment.getEquipmentId()),
                old.getName(), equipment.getName(), "SUCCESS", "Updated equipment details.");
        }
        return updated;
    }

    @Override
    public boolean updateStatus(int equipmentId, String status) {
        PermissionChecker.require("EQUIPMENT_UPDATE");
        Equipment old = getEquipmentById(equipmentId);
        boolean updated = equipmentDAO.updateStatus(equipmentId, status);
        if (updated) {
            auditService.logCurrent("EQUIPMENT_STATUS_UPDATE", "EQUIPMENT", "EQUIPMENT", String.valueOf(equipmentId),
                old.getStatus(), status, "SUCCESS", "Changed equipment status to " + status);
        }
        return updated;
    }

    @Override
    public boolean assignToProject(int equipmentId, Integer projectId, String status) {
        PermissionChecker.require("EQUIPMENT_UPDATE");
        Equipment old = getEquipmentById(equipmentId);
        boolean updated = equipmentDAO.assignProject(equipmentId, projectId, status);
        if (updated) {
            auditService.logCurrent("EQUIPMENT_ASSIGN_PROJECT", "EQUIPMENT", "EQUIPMENT", String.valueOf(equipmentId),
                String.valueOf(old.getProjectId()), String.valueOf(projectId), "SUCCESS",
                "Assigned equipment #" + equipmentId + " to project #" + projectId + " (Status: " + status + ")");
        }
        return updated;
    }

    @Override
    public boolean deleteEquipment(int equipmentId) {
        PermissionChecker.require("EQUIPMENT_UPDATE");
        Equipment e = getEquipmentById(equipmentId);
        boolean deleted = equipmentDAO.delete(equipmentId);
        if (deleted) {
            auditService.logCurrent("EQUIPMENT_DELETE", "EQUIPMENT", "EQUIPMENT", String.valueOf(equipmentId),
                e.getEquipmentCode(), null, "SUCCESS", "Deleted equipment #" + equipmentId);
        }
        return deleted;
    }

    @Override
    public Map<String, Integer> getEquipmentStats() {
        return equipmentDAO.countByStatus();
    }
}
