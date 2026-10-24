package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.Models.Equipment;
import com.csims.model.Models.InventoryTransaction;
import com.csims.model.Models.MaintenanceRecord;
import com.csims.model.Models.Material;
import com.csims.security.CustomUserDetails;
import com.csims.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Consolidated Inventory, Equipment and Maintenance Controller.
 * Handles /api/inventory, /api/equipment, and /api/maintenance.
 */
@RestController
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // =========================================================================
    // SECTION 1: INVENTORY & MATERIALS (/api/inventory)
    // =========================================================================

    @GetMapping("/api/inventory")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Material>>> getAllMaterials(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean lowStockOnly) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllMaterials(category, lowStockOnly)));
    }

    @GetMapping("/api/inventory/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Material>> getMaterialById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getMaterialById(id)));
    }

    @PostMapping("/api/inventory")
    @PreAuthorize("hasAuthority('INVENTORY_CREATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Integer>> createMaterial(
            @RequestBody Material material,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer id = inventoryService.createMaterial(material, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Material created successfully", id));
    }

    @PutMapping("/api/inventory/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateMaterial(
            @PathVariable Integer id,
            @RequestBody Material material,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        inventoryService.updateMaterial(id, material, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Material updated successfully", null));
    }

    @PostMapping("/api/inventory/receipt")
    @PreAuthorize("hasAuthority('INVENTORY_RECEIPT') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> recordReceipt(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer materialId = ((Number) body.get("materialId")).intValue();
        BigDecimal quantity = new BigDecimal(body.get("quantity").toString());
        String ref = (String) body.get("referenceNo");
        String remarks = (String) body.get("remarks");

        inventoryService.recordReceipt(materialId, quantity, ref, remarks, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Material receipt logged successfully", null));
    }

    @PostMapping("/api/inventory/issue")
    @PreAuthorize("hasAuthority('INVENTORY_ISSUE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> recordIssue(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer materialId = ((Number) body.get("materialId")).intValue();
        BigDecimal quantity = new BigDecimal(body.get("quantity").toString());
        Integer projectId = body.get("projectId") != null ? ((Number) body.get("projectId")).intValue() : null;
        String ref = (String) body.get("referenceNo");
        String remarks = (String) body.get("remarks");

        inventoryService.recordIssue(materialId, quantity, projectId, ref, remarks, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Material issued to project successfully", null));
    }

    @GetMapping("/api/inventory/transactions")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<InventoryTransaction>>> getTransactions(
            @RequestParam(required = false) Integer materialId,
            @RequestParam(required = false) Integer projectId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getTransactions(materialId, projectId)));
    }

    // =========================================================================
    // SECTION 2: EQUIPMENT (/api/equipment)
    // =========================================================================

    @GetMapping("/api/equipment")
    @PreAuthorize("hasAuthority('EQUIPMENT_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Equipment>>> getAllEquipment(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllEquipment(status, category)));
    }

    @GetMapping("/api/equipment/{id}")
    @PreAuthorize("hasAuthority('EQUIPMENT_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Equipment>> getEquipmentById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getEquipmentById(id)));
    }

    @PostMapping("/api/equipment")
    @PreAuthorize("hasAuthority('EQUIPMENT_CREATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Integer>> createEquipment(
            @RequestBody Equipment equipment,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer id = inventoryService.createEquipment(equipment, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Equipment registered successfully", id));
    }

    @PutMapping("/api/equipment/{id}")
    @PreAuthorize("hasAuthority('EQUIPMENT_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateEquipment(
            @PathVariable Integer id,
            @RequestBody Equipment equipment,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        inventoryService.updateEquipment(id, equipment, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Equipment updated successfully", null));
    }

    @PatchMapping("/api/equipment/{id}/status")
    @PreAuthorize("hasAuthority('EQUIPMENT_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateEquipmentStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String status = body.get("status");
        inventoryService.updateEquipmentStatus(id, status, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Equipment status updated", null));
    }

    // =========================================================================
    // SECTION 3: MAINTENANCE (/api/maintenance)
    // =========================================================================

    @GetMapping("/api/maintenance")
    @PreAuthorize("hasAuthority('MAINTENANCE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<MaintenanceRecord>>> getAllMaintenance(
            @RequestParam(required = false) Integer equipmentId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllMaintenance(equipmentId, status)));
    }

    @GetMapping("/api/maintenance/{id}")
    @PreAuthorize("hasAuthority('MAINTENANCE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<MaintenanceRecord>> getMaintenanceById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getMaintenanceById(id)));
    }

    @PostMapping("/api/maintenance")
    @PreAuthorize("hasAuthority('MAINTENANCE_SCHEDULE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Integer>> scheduleMaintenance(
            @RequestBody MaintenanceRecord record,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer id = inventoryService.scheduleMaintenance(record, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Maintenance scheduled successfully", id));
    }

    @PutMapping("/api/maintenance/{id}")
    @PreAuthorize("hasAuthority('MAINTENANCE_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateMaintenance(
            @PathVariable Integer id,
            @RequestBody MaintenanceRecord record,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        inventoryService.updateMaintenance(id, record, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Maintenance updated successfully", null));
    }
}
