package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.AuditLog;
import com.csims.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasAuthority('AUDIT_VIEW') or hasRole('Administrator')")
public class AuditLogController {
    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        List<AuditLog> logs = auditService.getAuditLogs(action, module, page, size);
        int total = auditService.countAuditLogs(action, module);

        Map<String, Object> result = new HashMap<>();
        result.put("items", logs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
