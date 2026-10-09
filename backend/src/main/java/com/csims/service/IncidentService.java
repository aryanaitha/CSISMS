package com.csims.service;

import com.csims.exception.ApiException;
import com.csims.model.Incident;
import com.csims.repository.IncidentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public IncidentService(IncidentRepository incidentRepository,
                           AuditService auditService,
                           NotificationService notificationService) {
        this.incidentRepository = incidentRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public List<Incident> getAllIncidents(Integer projectId, String severity, String status) {
        return incidentRepository.findAll(projectId, severity, status);
    }

    public Incident getIncidentById(Integer id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ApiException("Incident report not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer reportIncident(Incident inc, Long reporterId, String reporterUsername) {
        inc.setReportedBy(reporterId);
        Integer id = incidentRepository.create(inc);
        auditService.log(reporterId, reporterUsername, null, "INCIDENT_REPORT", "INCIDENT", "INCIDENT", String.valueOf(id), null, inc.getSeverity(), "SUCCESS", "Reported " + inc.getSeverity() + " incident: " + inc.getTitle());

        // High priority broadcast alert if FATALITY or INJURY
        if ("FATALITY".equalsIgnoreCase(inc.getSeverity()) || "INJURY".equalsIgnoreCase(inc.getSeverity())) {
            notificationService.notify(reporterId, "EMERGENCY: " + inc.getSeverity() + " Reported!", "Accident reported at project #" + inc.getProjectId() + ": " + inc.getTitle() + ". Immediate response required.", "DANGER", "/incidents");
        }

        return id;
    }

    @Transactional
    public void updateIncident(Integer id, Incident inc, Long operatorId, String operatorUsername) {
        Incident existing = getIncidentById(id);
        inc.setId(id);
        incidentRepository.update(inc);
        auditService.log(operatorId, operatorUsername, null, "INCIDENT_UPDATE", "INCIDENT", "INCIDENT", String.valueOf(id), existing.getStatus(), inc.getStatus(), "SUCCESS", "Updated incident #" + id + " to " + inc.getStatus());
    }
}
