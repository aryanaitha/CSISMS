package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.Contractor;
import com.csims.model.Worker;
import com.csims.security.CustomUserDetails;
import com.csims.service.WorkforceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workforce")
public class WorkforceController {
    private final WorkforceService workforceService;

    public WorkforceController(WorkforceService workforceService) {
        this.workforceService = workforceService;
    }

    // Workers
    @GetMapping("/workers")
    @PreAuthorize("hasAuthority('WORKFORCE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Worker>>> getAllWorkers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer contractorId) {
        return ResponseEntity.ok(ApiResponse.ok(workforceService.getAllWorkers(status, contractorId)));
    }

    @GetMapping("/workers/{id}")
    @PreAuthorize("hasAuthority('WORKFORCE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Worker>> getWorkerById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(workforceService.getWorkerById(id)));
    }

    @PostMapping("/workers")
    @PreAuthorize("hasAuthority('WORKFORCE_CREATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Integer>> createWorker(
            @RequestBody Worker worker,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer id = workforceService.createWorker(worker, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Worker added successfully", id));
    }

    @PutMapping("/workers/{id}")
    @PreAuthorize("hasAuthority('WORKFORCE_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateWorker(
            @PathVariable Integer id,
            @RequestBody Worker worker,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        workforceService.updateWorker(id, worker, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Worker updated successfully", null));
    }

    // Contractors
    @GetMapping("/contractors")
    @PreAuthorize("hasAuthority('WORKFORCE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Contractor>>> getAllContractors() {
        return ResponseEntity.ok(ApiResponse.ok(workforceService.getAllContractors()));
    }

    @GetMapping("/contractors/{id}")
    @PreAuthorize("hasAuthority('WORKFORCE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Contractor>> getContractorById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(workforceService.getContractorById(id)));
    }

    @PostMapping("/contractors")
    @PreAuthorize("hasAuthority('WORKFORCE_CREATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Integer>> createContractor(
            @RequestBody Contractor contractor,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer id = workforceService.createContractor(contractor, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Contractor registered successfully", id));
    }

    @PutMapping("/contractors/{id}")
    @PreAuthorize("hasAuthority('WORKFORCE_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateContractor(
            @PathVariable Integer id,
            @RequestBody Contractor contractor,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        workforceService.updateContractor(id, contractor, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Contractor updated successfully", null));
    }
}
