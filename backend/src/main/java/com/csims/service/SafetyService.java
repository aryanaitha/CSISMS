package com.csims.service;

import com.csims.exception.ApiException;
import com.csims.model.SafetyInspection;
import com.csims.model.SafetyViolation;
import com.csims.repository.SafetyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class SafetyService {
    private final SafetyRepository safetyRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public SafetyService(SafetyRepository safetyRepository,
                         AuditService auditService,
                         NotificationService notificationService) {
        this.safetyRepository = safetyRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

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
        auditService.log(inspectorId, inspectorUsername, null, "SAFETY_INSPECTION_CONDUCT", "SAFETY", "SAFETY_INSPECTION", String.valueOf(id), null, String.valueOf(overall), "SUCCESS", "Completed site safety inspection with score " + overall + "%");

        // If score < 70, trigger warning notification
        if (overall.compareTo(BigDecimal.valueOf(70)) < 0) {
            notificationService.notify(inspectorId, "Safety Score Alert (< 70%)", "Site inspection #" + id + " scored only " + overall + "%. Urgent corrective safety actions required.", "DANGER", "/safety");
        }

        return id;
    }

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
        auditService.log(reporterId, reporterUsername, null, "SAFETY_VIOLATION_LOG", "SAFETY", "SAFETY_VIOLATION", String.valueOf(id), null, sv.getSeverity(), "SUCCESS", "Logged " + sv.getSeverity() + " safety violation: " + sv.getDescription());
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
        auditService.log(operatorId, operatorUsername, null, "SAFETY_VIOLATION_UPDATE", "SAFETY", "SAFETY_VIOLATION", String.valueOf(id), existing.getStatus(), sv.getStatus(), "SUCCESS", "Updated safety violation #" + id + " status to " + sv.getStatus());
    }
}
