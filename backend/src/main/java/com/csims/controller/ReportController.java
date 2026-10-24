package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.dto.Dtos.ReportResultDto;
import com.csims.model.Models.AuditLog;
import com.csims.model.Models.Notification;
import com.csims.security.CustomUserDetails;
import com.csims.service.SystemService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consolidated System Controller.
 * Handles Reports, Dashboard metrics, Notifications, Audit Logs, and System Health.
 */
@RestController
public class ReportController {

    private final SystemService systemService;
    private final JdbcTemplate jdbcTemplate;

    public ReportController(SystemService systemService, JdbcTemplate jdbcTemplate) {
        this.systemService = systemService;
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================================================================
    // SECTION 1: REPORTS (/api/reports)
    // =========================================================================

    @GetMapping("/api/reports/list")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableReports() {
        return ResponseEntity.ok(ApiResponse.success(systemService.getAvailableReports()));
    }

    @GetMapping("/api/reports/preview")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<ApiResponse<ReportResultDto>> previewReport(@RequestParam String type) {
        return ResponseEntity.ok(ApiResponse.success(systemService.generateReport(type)));
    }

    @GetMapping("/api/reports/export/csv")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    public ResponseEntity<byte[]> exportCsv(@RequestParam String type) throws IOException {
        byte[] csvData = systemService.exportReportToCsv(type);
        String filename = type.toLowerCase() + "_report.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    // =========================================================================
    // SECTION 2: DASHBOARD METRICS (/api/dashboard)
    // =========================================================================

    @GetMapping("/api/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> metrics = systemService.getMetrics(userDetails.getRoleName(), userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(metrics));
    }

    @GetMapping("/api/dashboard/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMetrics(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> metrics = systemService.getMetrics(userDetails.getRoleName(), userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(metrics));
    }

    // =========================================================================
    // SECTION 3: NOTIFICATIONS (/api/notifications)
    // =========================================================================

    @GetMapping("/api/notifications")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Notification> list = systemService.getNotificationsForUser(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/api/notifications/unread-count")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        int count = systemService.getUnreadCount(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("unreadCount", count)));
    }

    @PostMapping("/api/notifications/{id}/read")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        systemService.markNotificationAsRead(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @PostMapping("/api/notifications/read-all")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        systemService.markAllNotificationsAsRead(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }

    // =========================================================================
    // SECTION 4: AUDIT LOGS (/api/audit-logs)
    // =========================================================================

    @GetMapping("/api/audit-logs")
    @PreAuthorize("hasAuthority('AUDIT_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        List<AuditLog> logs = systemService.getAuditLogs(action, module, page, size);
        int total = systemService.countAuditLogs(action, module);

        Map<String, Object> result = new HashMap<>();
        result.put("items", logs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // =========================================================================
    // SECTION 5: HEALTH CHECK (/api/health)
    // =========================================================================

    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                status.put("database", "CONNECTED");
            } else {
                status.put("database", "UNKNOWN");
            }
        } catch (Exception ex) {
            status.put("database", "NOT_CONNECTED");
            status.put("dbError", ex.getMessage());
        }
        return ApiResponse.ok(status);
    }
}
