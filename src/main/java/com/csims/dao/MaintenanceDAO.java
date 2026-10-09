package com.csims.dao;

import com.csims.model.Maintenance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MaintenanceDAO {
    Optional<Maintenance> findById(int maintenanceId);
    List<Maintenance> findAll();
    List<Maintenance> search(Integer equipmentId, String status, String maintenanceType);
    List<Maintenance> findDueMaintenance();
    int create(Maintenance maintenance);
    boolean update(Maintenance maintenance);
    boolean updateStatus(int maintenanceId, String status, LocalDate completedDate);
    boolean delete(int maintenanceId);
    int countPending();
}
