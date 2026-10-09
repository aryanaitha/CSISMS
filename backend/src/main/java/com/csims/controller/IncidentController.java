package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.Incident;
import com.csims.security.CustomUserDetails;
import com.csims.service.IncidentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INCIDENT_VIEW')")
    public ResponseEntity<ApiResponse<List<Incident>>> getAllIncidents(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(incidentService.getAllIncidents(projectId, severity, status)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INCIDENT_VIEW')")
    public ResponseEntity<ApiResponse<Incident>> getIncidentById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(incidentService.getIncidentById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INCIDENT_CREATE')")
    public ResponseEntity<ApiResponse<Integer>> reportIncident(
            @RequestBody Incident incident,
            @AuthenticationPrincipal CustomUserDetails user) {
        Integer id = incidentService.reportIncident(incident, user.getUserId(), user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Incident reported successfully", id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INCIDENT_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> updateIncident(
            @PathVariable Integer id,
            @RequestBody Incident incident,
            @AuthenticationPrincipal CustomUserDetails user) {
        incidentService.updateIncident(id, incident, user.getUserId(), user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Incident updated successfully", null));
    }
}
