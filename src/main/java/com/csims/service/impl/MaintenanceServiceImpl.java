package com.csims.service.impl;

import com.csims.dao.EquipmentDAO;
import com.csims.dao.MaintenanceDAO;
import com.csims.dao.impl.EquipmentDAOImpl;
import com.csims.dao.impl.MaintenanceDAOImpl;
import com.csims.model.Equipment;
import com.csims.model.Maintenance;
import com.csims.security.PermissionChecker;
import com.csims.service.AuditService;
import com.csims.service.MaintenanceService;
import com.csims.service.NotificationService;
import com.csims.util.ValidationException;
import com.csims.util.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAOImpl();
    private final EquipmentDAO equipmentDAO = new EquipmentDAOImpl();
    private final AuditService auditService = new AuditServiceImpl();
    private final NotificationService notificationService = new NotificationServiceImpl();

    @Override
    public List<Maintenance> getAllMaintenance() {
        PermissionChecker.require("MAINTENANCE_VIEW");
        return maintenanceDAO.findAll();
    }

    @Override
    public List<Maintenance> searchMaintenance(Integer equipmentId, String status, String type) {
        PermissionChecker.require("MAINTENANCE_VIEW");
        return maintenanceDAO.search(equipmentId, status, type);
    }

    @Override
    public Maintenance getMaintenanceById(int maintenanceId) {
        PermissionChecker.require("MAINTENANCE_VIEW");
        return maintenanceDAO.findById(maintenanceId)
            .orElseThrow(() -> new ValidationException("Maintenance order #" + maintenanceId + " not found."));
    }

    @Override
    public List<Maintenance> getDueMaintenance() {
        PermissionChecker.require("MAINTENANCE_VIEW");
        return maintenanceDAO.findDueMaintenance();
    }

    @Override
    public int scheduleMaintenance(Maintenance maintenance) {
        PermissionChecker.require("MAINTENANCE_SCHEDULE");
        Validator.requireNonEmpty(maintenance.getMaintenanceType(), "Maintenance Type");
        if (maintenance.getEquipmentId() <= 0) {
            throw new ValidationException("Equipment must be selected.");
        }
        if (maintenance.getScheduledDate() == null) {
            maintenance.setScheduledDate(LocalDate.now());
        }

        // When scheduled, place equipment Under Maintenance
        equipmentDAO.updateStatus(maintenance.getEquipmentId(), "Under Maintenance");

        int id = maintenanceDAO.create(maintenance);
        auditService.logCurrent("MAINTENANCE_SCHEDULED", "MAINTENANCE", "MAINTENANCE", String.valueOf(id),
            null, maintenance.getMaintenanceType() + " on " + maintenance.getScheduledDate(),
            "SUCCESS", "Scheduled maintenance for equipment #" + maintenance.getEquipmentId());
        return id;
    }

    @Override
    public boolean updateMaintenance(Maintenance maintenance) {
        PermissionChecker.require("MAINTENANCE_UPDATE");
        Validator.requireNonEmpty(maintenance.getMaintenanceType(), "Maintenance Type");

        boolean updated = maintenanceDAO.update(maintenance);
        if (updated) {
            auditService.logCurrent("MAINTENANCE_UPDATE", "MAINTENANCE", "MAINTENANCE", String.valueOf(maintenance.getMaintenanceId()),
                null, maintenance.getStatus(), "SUCCESS", "Updated maintenance details.");
        }
        return updated;
    }

    @Override
    public boolean completeMaintenance(int maintenanceId, LocalDate completedDate, BigDecimal cost,
                                       String technician, LocalDate nextServiceDue, String returnStatus) {
        PermissionChecker.require("MAINTENANCE_UPDATE");
        Maintenance m = getMaintenanceById(maintenanceId);

        if (completedDate == null) {
            completedDate = LocalDate.now();
        }

        m.setStatus("Completed");
        m.setCompletedDate(completedDate);
        if (cost != null) m.setCost(cost);
        if (technician != null && !technician.trim().isEmpty()) m.setTechnician(technician.trim());

        boolean updated = maintenanceDAO.update(m);
        if (updated) {
            // Update Equipment status and service dates
            String newStatus = returnStatus != null && !returnStatus.trim().isEmpty() ? returnStatus : "Available";
            equipmentDAO.updateStatus(m.getEquipmentId(), newStatus);
            if (nextServiceDue == null) {
                nextServiceDue = completedDate.plusMonths(6); // Default 6 month interval
            }
            equipmentDAO.updateServiceDates(m.getEquipmentId(), completedDate, nextServiceDue);

            auditService.logCurrent("MAINTENANCE_COMPLETED", "MAINTENANCE", "MAINTENANCE", String.valueOf(maintenanceId),
                "Under Maintenance", newStatus, "SUCCESS", "Completed maintenance order #" + maintenanceId + " for equipment #" + m.getEquipmentId());
        }
        return updated;
    }

    @Override
    public boolean deleteMaintenance(int maintenanceId) {
        PermissionChecker.require("MAINTENANCE_UPDATE");
        Maintenance m = getMaintenanceById(maintenanceId);
        boolean deleted = maintenanceDAO.delete(maintenanceId);
        if (deleted) {
            auditService.logCurrent("MAINTENANCE_DELETE", "MAINTENANCE", "MAINTENANCE", String.valueOf(maintenanceId),
                m.getMaintenanceType(), null, "SUCCESS", "Deleted maintenance record #" + maintenanceId);
        }
        return deleted;
    }
}
