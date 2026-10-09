package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.Equipment;
import com.csims.security.CustomUserDetails;
import com.csims.service.EquipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {
    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EQUIPMENT_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Equipment>>> getAllEquipment(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ApiResponse.ok(equipmentService.getAllEquipment(status, category)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EQUIPMENT_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Equipment>> getEquipmentById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(equipmentService.getEquipmentById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('EQUIPMENT_CREATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Integer>> createEquipment(
            @RequestBody Equipment equipment,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer id = equipmentService.createEquipment(equipment, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Equipment registered successfully", id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EQUIPMENT_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateEquipment(
            @PathVariable Integer id,
            @RequestBody Equipment equipment,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        equipmentService.updateEquipment(id, equipment, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Equipment updated successfully", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('EQUIPMENT_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String status = body.get("status");
        equipmentService.updateStatus(id, status, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Equipment status updated", null));
    }
}
