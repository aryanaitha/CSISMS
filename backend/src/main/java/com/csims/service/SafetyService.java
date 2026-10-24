package com.csims.service;

import com.csims.exception.AppExceptions.ApiException;
import com.csims.model.Models.Incident;
import com.csims.model.Models.SafetyInspection;
import com.csims.model.Models.SafetyViolation;
import com.csims.repository.SafetyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Consolidated Safety & Compliance Service.
 * Covers Safety Inspections, Hazard Violations, and Incident Reports.
 */
@Service
public class SafetyService {
    private final SafetyRepository safetyRepository;
    private final SystemService systemService;

    public SafetyService(SafetyRepository safetyRepository,
                         SystemService systemService) {
        this.safetyRepository = safetyRepository;
        this.systemService = systemService;
    }

    // =========================================================================
    // SECTION 1: SAFETY INSPECTIONS
    // =========================================================================

    public List<SafetyInspection> getAllInspections(Integer projectId) {
        return safetyRepository.findAllInspections(projectId);
    }

    public SafetyInspection getInspectionById(Integer id) {
        return safetyRepository.findInspectionById(id)
                .orElseThrow(() -> new ApiException("Safety inspection not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer conductInspection(SafetyInspection si, Long inspectorId, String inspectorUsername) {
        si.setInspectorId(inspectorId);
        
        // Calculate overall score (average of 5 categories)
        double total = si.getPpeScore() + si.getWorkerSafetyScore() + si.getEquipmentScore() + si.getWorkAreaScore() + si.getEmergencyScore();
        double avg = total / 5.0;
        BigDecimal overall = BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
        si.setOverallScore(overall);

        Integer id = safetyRepository.createInspection(si);
        systemService.log(inspectorId, inspectorUsername, null, "SAFETY_INSPECTION_CONDUCT", "SAFETY", "SAFETY_INSPECTION", String.valueOf(id), null, String.valueOf(overall), "SUCCESS", "Completed site safety inspection with score " + overall + "%");

        // If score < 70, trigger warning notification
        if (overall.compareTo(BigDecimal.valueOf(70)) < 0) {
            systemService.notify(inspectorId, "Safety Score Alert (< 70%)", "Site inspection #" + id + " scored only " + overall + "%. Urgent corrective safety actions required.", "DANGER", "/safety");
        }

        return id;
    }

    // =========================================================================
    // SECTION 2: SAFETY VIOLATIONS
    // =========================================================================

    public List<SafetyViolation> getAllViolations(Integer projectId, String status, String severity) {
        return safetyRepository.findAllViolations(projectId, status, severity);
    }

    public SafetyViolation getViolationById(Integer id) {
        return safetyRepository.findViolationById(id)
                .orElseThrow(() -> new ApiException("Safety violation not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer logViolation(SafetyViolation sv, Long reporterId, String reporterUsername) {
        sv.setReportedBy(reporterId);
        if (sv.getStatus() == null) sv.setStatus("OPEN");
        Integer id = safetyRepository.createViolation(sv);
        systemService.log(reporterId, reporterUsername, null, "SAFETY_VIOLATION_LOG", "SAFETY", "SAFETY_VIOLATION", String.valueOf(id), null, sv.getSeverity(), "SUCCESS", "Logged " + sv.getSeverity() + " safety violation: " + sv.getDescription());
        return id;
    }

    @Transactional
    public void updateViolation(Integer id, SafetyViolation sv, Long operatorId, String operatorUsername) {
        SafetyViolation existing = getViolationById(id);
        sv.setId(id);
        if ("RESOLVED".equalsIgnoreCase(sv.getStatus()) && sv.getResolutionDate() == null) {
            sv.setResolutionDate(LocalDate.now());
        }
        safetyRepository.updateViolation(sv);
        systemService.log(operatorId, operatorUsername, null, "SAFETY_VIOLATION_UPDATE", "SAFETY", "SAFETY_VIOLATION", String.valueOf(id), existing.getStatus(), sv.getStatus(), "SUCCESS", "Updated safety violation #" + id + " status to " + sv.getStatus());
    }

    // =========================================================================
    // SECTION 3: INCIDENTS
    // =========================================================================

    public List<Incident> getAllIncidents(Integer projectId, String severity, String status) {
        return safetyRepository.findAllIncidents(projectId, severity, status);
    }

    public Incident getIncidentById(Integer id) {
        return safetyRepository.findIncidentById(id)
                .orElseThrow(() -> new ApiException("Incident report not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer reportIncident(Incident inc, Long reporterId, String reporterUsername) {
        inc.setReportedBy(reporterId);
        Integer id = safetyRepository.createIncident(inc);
        systemService.log(reporterId, reporterUsername, null, "INCIDENT_REPORT", "INCIDENT", "INCIDENT", String.valueOf(id), null, inc.getSeverity(), "SUCCESS", "Reported " + inc.getSeverity() + " incident: " + inc.getTitle());

        // High priority broadcast alert if FATALITY or INJURY
        if ("FATALITY".equalsIgnoreCase(inc.getSeverity()) || "INJURY".equalsIgnoreCase(inc.getSeverity())) {
            systemService.notify(reporterId, "EMERGENCY: " + inc.getSeverity() + " Reported!", "Accident reported at project #" + inc.getProjectId() + ": " + inc.getTitle() + ". Immediate response required.", "DANGER", "/incidents");
        }

        return id;
    }

    @Transactional
    public void updateIncident(Integer id, Incident inc, Long operatorId, String operatorUsername) {
        Incident existing = getIncidentById(id);
        inc.setId(id);
        safetyRepository.updateIncident(inc);
        systemService.log(operatorId, operatorUsername, null, "INCIDENT_UPDATE", "INCIDENT", "INCIDENT", String.valueOf(id), existing.getStatus(), inc.getStatus(), "SUCCESS", "Updated incident #" + id + " to " + inc.getStatus());
    }
}
