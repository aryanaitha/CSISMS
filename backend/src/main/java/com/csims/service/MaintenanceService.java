package com.csims.service;

import com.csims.exception.ApiException;
import com.csims.model.Equipment;
import com.csims.model.MaintenanceRecord;
import com.csims.repository.EquipmentRepository;
import com.csims.repository.MaintenanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MaintenanceService {
    private final MaintenanceRepository maintenanceRepository;
    private final EquipmentRepository equipmentRepository;
    private final AuditService auditService;

    public MaintenanceService(MaintenanceRepository maintenanceRepository,
                              EquipmentRepository equipmentRepository,
                              AuditService auditService) {
        this.maintenanceRepository = maintenanceRepository;
        this.equipmentRepository = equipmentRepository;
        this.auditService = auditService;
    }

    public List<MaintenanceRecord> getAllMaintenance(Integer equipmentId, String status) {
        return maintenanceRepository.findAll(equipmentId, status);
    }

    public MaintenanceRecord getMaintenanceById(Integer id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ApiException("Maintenance record not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer scheduleMaintenance(MaintenanceRecord m, Long operatorId, String operatorUsername) {
        // Mark equipment as Maintenance
        equipmentRepository.updateStatus(m.getEquipmentId(), "Maintenance");
        Integer id = maintenanceRepository.create(m);
        auditService.log(operatorId, operatorUsername, null, "MAINTENANCE_SCHEDULE", "MAINTENANCE", "MAINTENANCE", String.valueOf(id), null, m.getMaintenanceType(), "SUCCESS", "Scheduled maintenance for equipment #" + m.getEquipmentId());
        return id;
    }

    @Transactional
    public void updateMaintenance(Integer id, MaintenanceRecord m, Long operatorId, String operatorUsername) {
        MaintenanceRecord existing = getMaintenanceById(id);
        m.setId(id);
        
        // If transitioning to Completed
        if ("Completed".equalsIgnoreCase(m.getStatus())) {
            if (m.getCompletedDate() == null) {
                m.setCompletedDate(LocalDate.now());
            }
            LocalDate nextDue = LocalDate.now().plusDays(90);
            equipmentRepository.updateServiceDates(existing.getEquipmentId(), m.getCompletedDate(), nextDue);
        }

        maintenanceRepository.update(m);
        auditService.log(operatorId, operatorUsername, null, "MAINTENANCE_UPDATE", "MAINTENANCE", "MAINTENANCE", String.valueOf(id), existing.getStatus(), m.getStatus(), "SUCCESS", "Updated maintenance service order #" + id + " to " + m.getStatus());
    }
}
