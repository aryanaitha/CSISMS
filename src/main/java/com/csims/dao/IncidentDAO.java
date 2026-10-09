package com.csims.dao;

import com.csims.model.Incident;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IncidentDAO {
    Optional<Incident> findById(int incidentId);
    List<Incident> findAll();
    List<Incident> search(Integer projectId, String severity, String status, LocalDate startDate, LocalDate endDate);
    int create(Incident incident);
    boolean update(Incident incident);
    boolean updateStatusAndInvestigation(int incidentId, String status, Integer investigatorId, String actionsTaken);
    boolean delete(int incidentId);
    Map<String, Integer> countBySeverity();
    int countActiveIncidents();
    int countTotal();
}
