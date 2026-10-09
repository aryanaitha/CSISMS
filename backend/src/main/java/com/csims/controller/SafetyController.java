package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.SafetyInspection;
import com.csims.model.SafetyViolation;
import com.csims.security.CustomUserDetails;
import com.csims.service.SafetyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/safety")
public class SafetyController {

    private final SafetyService safetyService;

    public SafetyController(SafetyService safetyService) {
        this.safetyService = safetyService;
    }

    // Inspections
    @GetMapping("/inspections")
    @PreAuthorize("hasAuthority('SAFETY_INSPECTION_VIEW')")
    public ResponseEntity<ApiResponse<List<SafetyInspection>>> getAllInspections(
            @RequestParam(required = false) Integer projectId) {
        return ResponseEntity.ok(ApiResponse.success(safetyService.getAllInspections(projectId)));
    }

    @GetMapping("/inspections/{id}")
    @PreAuthorize("hasAuthority('SAFETY_INSPECTION_VIEW')")
    public ResponseEntity<ApiResponse<SafetyInspection>> getInspectionById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(safetyService.getInspectionById(id)));
    }

    @PostMapping("/inspections")
    @PreAuthorize("hasAuthority('SAFETY_INSPECTION_CREATE')")
    public ResponseEntity<ApiResponse<Integer>> conductInspection(
            @RequestBody SafetyInspection inspection,
            @AuthenticationPrincipal CustomUserDetails user) {
        Integer id = safetyService.conductInspection(inspection, user.getUserId(), user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Safety inspection recorded successfully", id));
    }

    // Violations
    @GetMapping("/violations")
    @PreAuthorize("hasAuthority('SAFETY_VIOLATION_VIEW')")
    public ResponseEntity<ApiResponse<List<SafetyViolation>>> getAllViolations(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity) {
        return ResponseEntity.ok(ApiResponse.success(safetyService.getAllViolations(projectId, status, severity)));
    }

    @GetMapping("/violations/{id}")
    @PreAuthorize("hasAuthority('SAFETY_VIOLATION_VIEW')")
    public ResponseEntity<ApiResponse<SafetyViolation>> getViolationById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(safetyService.getViolationById(id)));
    }

    @PostMapping("/violations")
    @PreAuthorize("hasAuthority('SAFETY_VIOLATION_CREATE')")
    public ResponseEntity<ApiResponse<Integer>> logViolation(
            @RequestBody SafetyViolation violation,
            @AuthenticationPrincipal CustomUserDetails user) {
        Integer id = safetyService.logViolation(violation, user.getUserId(), user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Safety violation logged successfully", id));
    }

    @PutMapping("/violations/{id}")
    @PreAuthorize("hasAuthority('SAFETY_VIOLATION_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> updateViolation(
            @PathVariable Integer id,
            @RequestBody SafetyViolation violation,
            @AuthenticationPrincipal CustomUserDetails user) {
        safetyService.updateViolation(id, violation, user.getUserId(), user.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Safety violation updated successfully", null));
    }
}
