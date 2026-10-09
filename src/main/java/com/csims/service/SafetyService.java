package com.csims.service;

import com.csims.model.Incident;
import com.csims.model.SafetyInspection;
import com.csims.model.SafetyViolation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SafetyService {
    // Inspections
    List<SafetyInspection> getAllInspections();
    List<SafetyInspection> searchInspections(Integer projectId, Integer inspectorId, LocalDate startDate, LocalDate endDate);
    SafetyInspection getInspectionById(int inspectionId);
    int createInspection(SafetyInspection inspection);
    double getAverageSafetyScore();

    // Violations
    List<SafetyViolation> getAllViolations();
    List<SafetyViolation> searchViolations(Integer projectId, Integer workerId, String severity, String status);
    SafetyViolation getViolationById(int violationId);
    int createViolation(SafetyViolation violation);
    boolean updateViolation(SafetyViolation violation);
    boolean resolveViolation(int violationId, String status, String correctiveAction, LocalDate resolutionDate);
    Map<String, Integer> getViolationStats();

    // Incidents
    List<Incident> getAllIncidents();
    List<Incident> searchIncidents(Integer projectId, String severity, String status, LocalDate startDate, LocalDate endDate);
    Incident getIncidentById(int incidentId);
    int reportIncident(Incident incident);
    boolean updateIncident(Incident incident);
    boolean transitionIncidentStatus(int incidentId, String status, Integer investigatorId, String actionsTaken);
    Map<String, Integer> getIncidentStats();
}
