package com.csims.service;

import com.csims.exception.ApiException;
import com.csims.model.Equipment;
import com.csims.repository.EquipmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final AuditService auditService;

    public EquipmentService(EquipmentRepository equipmentRepository, AuditService auditService) {
        this.equipmentRepository = equipmentRepository;
        this.auditService = auditService;
    }

    public List<Equipment> getAllEquipment(String status, String category) {
        return equipmentRepository.findAll(status, category);
    }

    public Equipment getEquipmentById(Integer id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ApiException("Equipment not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer createEquipment(Equipment e, Long operatorId, String operatorUsername) {
        Integer id = equipmentRepository.create(e);
        auditService.log(operatorId, operatorUsername, null, "EQUIPMENT_CREATE", "EQUIPMENT", "EQUIPMENT", String.valueOf(id), null, e.getName(), "SUCCESS", "Registered machinery: " + e.getName() + " (" + e.getEquipmentCode() + ")");
        return id;
    }

    @Transactional
    public void updateEquipment(Integer id, Equipment e, Long operatorId, String operatorUsername) {
        Equipment existing = getEquipmentById(id);
        e.setId(id);
        equipmentRepository.update(e);
        auditService.log(operatorId, operatorUsername, null, "EQUIPMENT_UPDATE", "EQUIPMENT", "EQUIPMENT", String.valueOf(id), existing.getStatus(), e.getStatus(), "SUCCESS", "Updated equipment details for " + e.getName());
    }

    @Transactional
    public void updateStatus(Integer id, String status, Long operatorId, String operatorUsername) {
        Equipment existing = getEquipmentById(id);
        equipmentRepository.updateStatus(id, status);
        auditService.log(operatorId, operatorUsername, null, "EQUIPMENT_STATUS_UPDATE", "EQUIPMENT", "EQUIPMENT", String.valueOf(id), existing.getStatus(), status, "SUCCESS", "Updated equipment #" + id + " status to " + status);
    }
}
