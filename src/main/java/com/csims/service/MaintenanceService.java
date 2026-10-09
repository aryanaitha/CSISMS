package com.csims.service;

import com.csims.model.Maintenance;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MaintenanceService {
    List<Maintenance> getAllMaintenance();
    List<Maintenance> searchMaintenance(Integer equipmentId, String status, String type);
    Maintenance getMaintenanceById(int maintenanceId);
    List<Maintenance> getDueMaintenance();
    int scheduleMaintenance(Maintenance maintenance);
    boolean updateMaintenance(Maintenance maintenance);
    boolean completeMaintenance(int maintenanceId, LocalDate completedDate, BigDecimal cost, String technician, LocalDate nextServiceDue, String returnStatus);
    boolean deleteMaintenance(int maintenanceId);
}
