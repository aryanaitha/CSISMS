package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.Models.Incident;
import com.csims.model.Models.SafetyInspection;
import com.csims.model.Models.SafetyViolation;
import com.csims.security.CustomUserDetails;
import com.csims.service.SafetyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Consolidated Safety and Incident Controller.
 * Handles /api/safety (inspections & violations) and /api/incidents.
 */
@RestController
public class SafetyController {

    private final SafetyService safetyService;

    public SafetyController(SafetyService safetyService) {
        this.safetyService = safetyService;
    }

    // =========================================================================
    // SECTION 1: SAFETY INSPECTIONS (/api/safety/inspections)
    // =========================================================================

    @GetMapping("/api/safety/inspections")
    @PreAuthorize("hasAuthority('SAFETY_INSPECTION_VIEW')")
    public ResponseEntity<ApiResponse<List<SafetyInspection>>> getAllInspections(
            @RequestParam(required = false) Integer projectId) {
        return ResponseEntity.ok(ApiResponse.success(safetyService.getAllInspections(projectId)));
    }

    @GetMapping("/api/safety/inspections/{id}")
    @PreAuthorize("hasAuthority('SAFETY_INSPECTION_VIEW')")
    public ResponseEntity<ApiResponse<SafetyInspection>> getInspectionById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(safetyService.getInspectionById(id)));
    }

    @PostMapping("/api/safety/inspections")
    @PreAuthorize("hasAuthority('SAFETY_INSPECTION_CREATE')")
    public ResponseEntity<ApiResponse<Integer>> conductInspection(
            @RequestBody SafetyInspection inspection,
            @AuthenticationPrincipal CustomUserDetails user) {
        Integer id = safetyService.conductInspection(inspection, user.getId(), user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Safety inspection recorded successfully", id));
    }

    // =========================================================================
    // SECTION 2: SAFETY VIOLATIONS (/api/safety/violations)
    // =========================================================================

    @GetMapping("/api/safety/violations")
    @PreAuthorize("hasAuthority('SAFETY_VIOLATION_VIEW')")
    public ResponseEntity<ApiResponse<List<SafetyViolation>>> getAllViolations(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity) {
        return ResponseEntity.ok(ApiResponse.success(safetyService.getAllViolations(projectId, status, severity)));
    }

    @GetMapping("/api/safety/violations/{id}")
    @PreAuthorize("hasAuthority('SAFETY_VIOLATION_VIEW')")
    public ResponseEntity<ApiResponse<SafetyViolation>> getViolationById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(safetyService.getViolationById(id)));
    }

    @PostMapping("/api/safety/violations")
    @PreAuthorize("hasAuthority('SAFETY_VIOLATION_CREATE')")
    public ResponseEntity<ApiResponse<Integer>> logViolation(
            @RequestBody SafetyViolation violation,
            @AuthenticationPrincipal CustomUserDetails user) {
        Integer id = safetyService.logViolation(violation, user.getId(), user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Safety violation logged successfully", id));
    }

    @PutMapping("/api/safety/violations/{id}")
    @PreAuthorize("hasAuthority('SAFETY_VIOLATION_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> updateViolation(
            @PathVariable Integer id,
            @RequestBody SafetyViolation violation,
            @AuthenticationPrincipal CustomUserDetails user) {
        safetyService.updateViolation(id, violation, user.getId(), user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Safety violation updated successfully", null));
    }

    // =========================================================================
    // SECTION 3: INCIDENTS (/api/incidents)
    // =========================================================================

    @GetMapping("/api/incidents")
    @PreAuthorize("hasAuthority('INCIDENT_VIEW')")
    public ResponseEntity<ApiResponse<List<Incident>>> getAllIncidents(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(safetyService.getAllIncidents(projectId, severity, status)));
    }

    @GetMapping("/api/incidents/{id}")
    @PreAuthorize("hasAuthority('INCIDENT_VIEW')")
    public ResponseEntity<ApiResponse<Incident>> getIncidentById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(safetyService.getIncidentById(id)));
    }

    @PostMapping("/api/incidents")
    @PreAuthorize("hasAuthority('INCIDENT_CREATE')")
    public ResponseEntity<ApiResponse<Integer>> reportIncident(
            @RequestBody Incident incident,
            @AuthenticationPrincipal CustomUserDetails user) {
        Integer id = safetyService.reportIncident(incident, user.getId(), user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Incident reported successfully", id));
    }

    @PutMapping("/api/incidents/{id}")
    @PreAuthorize("hasAuthority('INCIDENT_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> updateIncident(
            @PathVariable Integer id,
            @RequestBody Incident incident,
            @AuthenticationPrincipal CustomUserDetails user) {
        safetyService.updateIncident(id, incident, user.getId(), user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Incident updated successfully", null));
    }
}
