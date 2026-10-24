package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.security.CustomUserDetails;
import com.csims.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> metrics = dashboardService.getMetrics(userDetails.getRoleName(), userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(metrics));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMetrics(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> metrics = dashboardService.getMetrics(userDetails.getRoleName(), userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(metrics));
    }
}
