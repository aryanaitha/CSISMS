package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.InventoryTransaction;
import com.csims.model.Material;
import com.csims.security.CustomUserDetails;
import com.csims.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class MaterialController {
    private final InventoryService inventoryService;

    public MaterialController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Material>>> getAllMaterials(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean lowStockOnly) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getAllMaterials(category, lowStockOnly)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Material>> getMaterialById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getMaterialById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_CREATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Integer>> createMaterial(
            @RequestBody Material material,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer id = inventoryService.createMaterial(material, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Material created successfully", id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateMaterial(
            @PathVariable Integer id,
            @RequestBody Material material,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        inventoryService.updateMaterial(id, material, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Material updated successfully", null));
    }

    @PostMapping("/receipt")
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

    @PostMapping("/issue")
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

    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<InventoryTransaction>>> getTransactions(
            @RequestParam(required = false) Integer materialId,
            @RequestParam(required = false) Integer projectId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.getTransactions(materialId, projectId)));
    }
}
