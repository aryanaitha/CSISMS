package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.MaintenanceRecord;
import com.csims.security.CustomUserDetails;
import com.csims.service.MaintenanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {
    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MAINTENANCE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<MaintenanceRecord>>> getAllMaintenance(
            @RequestParam(required = false) Integer equipmentId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(maintenanceService.getAllMaintenance(equipmentId, status)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MAINTENANCE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<MaintenanceRecord>> getMaintenanceById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(maintenanceService.getMaintenanceById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MAINTENANCE_SCHEDULE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Integer>> scheduleMaintenance(
            @RequestBody MaintenanceRecord record,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer id = maintenanceService.scheduleMaintenance(record, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Maintenance scheduled successfully", id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MAINTENANCE_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateMaintenance(
            @PathVariable Integer id,
            @RequestBody MaintenanceRecord record,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        maintenanceService.updateMaintenance(id, record, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Maintenance updated successfully", null));
    }
}
