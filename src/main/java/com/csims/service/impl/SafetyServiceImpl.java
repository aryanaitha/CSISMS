package com.csims.service.impl;

import com.csims.dao.IncidentDAO;
import com.csims.dao.SafetyInspectionDAO;
import com.csims.dao.SafetyViolationDAO;
import com.csims.dao.impl.IncidentDAOImpl;
import com.csims.dao.impl.SafetyInspectionDAOImpl;
import com.csims.dao.impl.SafetyViolationDAOImpl;
import com.csims.model.Incident;
import com.csims.model.SafetyInspection;
import com.csims.model.SafetyViolation;
import com.csims.model.User;
import com.csims.security.PermissionChecker;
import com.csims.security.SessionContext;
import com.csims.service.AuditService;
import com.csims.service.NotificationService;
import com.csims.service.SafetyService;
import com.csims.util.ValidationException;
import com.csims.util.Validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SafetyServiceImpl implements SafetyService {

    private final SafetyInspectionDAO inspectionDAO = new SafetyInspectionDAOImpl();
    private final SafetyViolationDAO violationDAO = new SafetyViolationDAOImpl();
    private final IncidentDAO incidentDAO = new IncidentDAOImpl();
    private final AuditService auditService = new AuditServiceImpl();
    private final NotificationService notificationService = new NotificationServiceImpl();

    // ==========================================
    // Safety Inspections
    // ==========================================

    @Override
    public List<SafetyInspection> getAllInspections() {
        PermissionChecker.require("SAFETY_INSPECTION_VIEW");
        return inspectionDAO.findAll();
    }

    @Override
    public List<SafetyInspection> searchInspections(Integer projectId, Integer inspectorId, LocalDate startDate, LocalDate endDate) {
        PermissionChecker.require("SAFETY_INSPECTION_VIEW");
        return inspectionDAO.search(projectId, inspectorId, startDate, endDate);
    }

    @Override
    public SafetyInspection getInspectionById(int inspectionId) {
        PermissionChecker.require("SAFETY_INSPECTION_VIEW");
        return inspectionDAO.findById(inspectionId)
            .orElseThrow(() -> new ValidationException("Inspection #" + inspectionId + " not found."));
    }

    @Override
    public int createInspection(SafetyInspection inspection) {
        PermissionChecker.require("SAFETY_INSPECTION_CREATE");
        if (inspection.getProjectId() <= 0) {
            throw new ValidationException("Project must be selected.");
        }
        if (inspection.getInspectionDate() == null) {
            inspection.setInspectionDate(LocalDate.now());
        }

        User user = SessionContext.getInstance().getCurrentUser();
        inspection.setInspectorId(user != null ? user.getUserId() : 1);

        inspection.computeOverallScore();

        int id = inspectionDAO.create(inspection);
        auditService.logCurrent("SAFETY_INSPECTION_CREATE", "SAFETY", "INSPECTION", String.valueOf(id),
            null, "Overall Score: " + inspection.getOverallScore() + "%", "SUCCESS",
            "Conducted safety audit inspection on project #" + inspection.getProjectId());
        return id;
    }

    @Override
    public double getAverageSafetyScore() {
        return inspectionDAO.getAverageSafetyScore();
    }

    // ==========================================
    // Safety Violations
    // ==========================================

    @Override
    public List<SafetyViolation> getAllViolations() {
        PermissionChecker.require("SAFETY_VIOLATION_VIEW");
        return violationDAO.findAll();
    }

    @Override
    public List<SafetyViolation> searchViolations(Integer projectId, Integer workerId, String severity, String status) {
        PermissionChecker.require("SAFETY_VIOLATION_VIEW");
        return violationDAO.search(projectId, workerId, severity, status);
    }

    @Override
    public SafetyViolation getViolationById(int violationId) {
        PermissionChecker.require("SAFETY_VIOLATION_VIEW");
        return violationDAO.findById(violationId)
            .orElseThrow(() -> new ValidationException("Safety violation #" + violationId + " not found."));
    }

    @Override
    public int createViolation(SafetyViolation violation) {
        PermissionChecker.require("SAFETY_VIOLATION_CREATE");
        Validator.requireNonEmpty(violation.getDescription(), "Violation Description");
        if (violation.getProjectId() <= 0) {
            throw new ValidationException("Project must be selected.");
        }

        User reporter = SessionContext.getInstance().getCurrentUser();
        if (reporter != null) {
            violation.setReportedBy(reporter.getUserId());
        }

        int id = violationDAO.create(violation);
        auditService.logCurrent("SAFETY_VIOLATION_CREATE", "SAFETY", "VIOLATION", String.valueOf(id),
            null, "[" + violation.getSeverity() + "] " + violation.getDescription(), "SUCCESS",
            "Logged safety violation on project #" + violation.getProjectId());

        // Notify officers if High or Critical
        if ("High".equalsIgnoreCase(violation.getSeverity()) || "Critical".equalsIgnoreCase(violation.getSeverity())) {
            notificationService.notifySafetyViolation(violation.getProjectId(), violation.getSeverity(), violation.getDescription());
        }
        return id;
    }

    @Override
    public boolean updateViolation(SafetyViolation violation) {
        PermissionChecker.require("SAFETY_VIOLATION_UPDATE");
        Validator.requireNonEmpty(violation.getDescription(), "Violation Description");

        boolean updated = violationDAO.update(violation);
        if (updated) {
            auditService.logCurrent("SAFETY_VIOLATION_UPDATE", "SAFETY", "VIOLATION", String.valueOf(violation.getViolationId()),
                null, violation.getStatus(), "SUCCESS", "Updated safety violation details.");
        }
        return updated;
    }

    @Override
    public boolean resolveViolation(int violationId, String status, String correctiveAction, LocalDate resolutionDate) {
        PermissionChecker.require("SAFETY_VIOLATION_UPDATE");
        SafetyViolation old = getViolationById(violationId);

        if (resolutionDate == null && ("RESOLVED".equalsIgnoreCase(status) || "CLOSED".equalsIgnoreCase(status))) {
            resolutionDate = LocalDate.now();
        }

        boolean updated = violationDAO.updateResolution(violationId, status, correctiveAction, resolutionDate);
        if (updated) {
            auditService.logCurrent("SAFETY_VIOLATION_RESOLVE", "SAFETY", "VIOLATION", String.valueOf(violationId),
                old.getStatus(), status, "SUCCESS", "Updated corrective action and violation status to " + status);
        }
        return updated;
    }

    @Override
    public Map<String, Integer> getViolationStats() {
        return violationDAO.countBySeverity();
    }

    // ==========================================
    // Incidents
    // ==========================================

    @Override
    public List<Incident> getAllIncidents() {
        PermissionChecker.require("INCIDENT_VIEW");
        return incidentDAO.findAll();
    }

    @Override
    public List<Incident> searchIncidents(Integer projectId, String severity, String status, LocalDate startDate, LocalDate endDate) {
        PermissionChecker.require("INCIDENT_VIEW");
        return incidentDAO.search(projectId, severity, status, startDate, endDate);
    }

    @Override
    public Incident getIncidentById(int incidentId) {
        PermissionChecker.require("INCIDENT_VIEW");
        return incidentDAO.findById(incidentId)
            .orElseThrow(() -> new ValidationException("Incident #" + incidentId + " not found."));
    }

    @Override
    public int reportIncident(Incident incident) {
        PermissionChecker.require("INCIDENT_CREATE");
        Validator.requireNonEmpty(incident.getTitle(), "Incident Title");
        Validator.requireNonEmpty(incident.getDescription(), "Incident Description");
        if (incident.getProjectId() <= 0) {
            throw new ValidationException("Project must be selected.");
        }
        if (incident.getIncidentDate() == null) {
            incident.setIncidentDate(LocalDateTime.now());
        }

        User reporter = SessionContext.getInstance().getCurrentUser();
        if (reporter != null) {
            incident.setReportedBy(reporter.getUserId());
        }

        incident.setStatus("Reported");

        int id = incidentDAO.create(incident);
        auditService.logCurrent("INCIDENT_REPORTED", "INCIDENT", "INCIDENT", String.valueOf(id),
            null, "[" + incident.getSeverity() + "] " + incident.getTitle(), "SUCCESS",
            "Reported incident on project #" + incident.getProjectId());

        if ("Critical".equalsIgnoreCase(incident.getSeverity()) || "Major".equalsIgnoreCase(incident.getSeverity())) {
            notificationService.notifyCriticalIncident(incident.getProjectId(), incident.getTitle(), incident.getSeverity());
        }
        return id;
    }

    @Override
    public boolean updateIncident(Incident incident) {
        PermissionChecker.require("INCIDENT_UPDATE");
        Validator.requireNonEmpty(incident.getTitle(), "Incident Title");

        boolean updated = incidentDAO.update(incident);
        if (updated) {
            auditService.logCurrent("INCIDENT_UPDATE", "INCIDENT", "INCIDENT", String.valueOf(incident.getIncidentId()),
                null, incident.getStatus(), "SUCCESS", "Updated incident details.");
        }
        return updated;
    }

    @Override
    public boolean transitionIncidentStatus(int incidentId, String targetStatus, Integer investigatorId, String actionsTaken) {
        PermissionChecker.require("INCIDENT_UPDATE");
        Incident current = getIncidentById(incidentId);
        String currentStatus = current.getStatus();

        // Enforce Workflow: Reported -> Under Investigation -> Action Taken -> Resolved
        boolean valid = switch (currentStatus) {
            case "Reported" -> targetStatus.equals("Under Investigation");
            case "Under Investigation" -> targetStatus.equals("Action Taken") || targetStatus.equals("Resolved");
            case "Action Taken" -> targetStatus.equals("Resolved") || targetStatus.equals("Under Investigation");
            case "Resolved" -> targetStatus.equals("Under Investigation"); // reopened
            default -> false;
        };

        if (!valid) {
            throw new ValidationException("Invalid status transition: Cannot change from '" + currentStatus + "' to '" + targetStatus +
                                          "'. Workflow: Reported -> Under Investigation -> Action Taken -> Resolved.");
        }

        boolean updated = incidentDAO.updateStatusAndInvestigation(incidentId, targetStatus, investigatorId, actionsTaken);
        if (updated) {
            auditService.logCurrent("INCIDENT_WORKFLOW_TRANSITION", "INCIDENT", "INCIDENT", String.valueOf(incidentId),
                currentStatus, targetStatus, "SUCCESS", "Transitioned incident workflow.");
        }
        return updated;
    }

    @Override
    public Map<String, Integer> getIncidentStats() {
        return incidentDAO.countBySeverity();
    }
}
