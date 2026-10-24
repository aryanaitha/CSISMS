package com.csims.service;

import com.csims.model.Incident;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentService {
    private final SafetyService safetyService;

    public IncidentService(SafetyService safetyService) {
        this.safetyService = safetyService;
    }

    public List<Incident> getAllIncidents(Integer projectId, String severity, String status) {
        return safetyService.getAllIncidents(projectId, severity, status);
    }

    public Incident getIncidentById(Integer id) {
        return safetyService.getIncidentById(id);
    }

    public Integer reportIncident(Incident inc, Long reporterId, String reporterUsername) {
        return safetyService.reportIncident(inc, reporterId, reporterUsername);
    }

    public void updateIncident(Integer id, Incident inc, Long operatorId, String operatorUsername) {
        safetyService.updateIncident(id, inc, operatorId, operatorUsername);
    }
}
