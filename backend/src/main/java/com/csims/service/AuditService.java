package com.csims.service;

import com.csims.model.AuditLog;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {
    private final SystemService systemService;

    public AuditService(SystemService systemService) {
        this.systemService = systemService;
    }

    public void log(Long userId, String username, String role, String action, String module,
                    String entityType, String entityId, String oldValue, String newValue,
                    String status, String description) {
        systemService.log(userId, username, role, action, module, entityType, entityId, oldValue, newValue, status, description);
    }

    public void logSimple(Long userId, String username, String action, String module, String description) {
        systemService.logSimple(userId, username, action, module, description);
    }

    public List<AuditLog> getAuditLogs(String action, String module, int page, int size) {
        return systemService.getAuditLogs(action, module, page, size);
    }

    public int countAuditLogs(String action, String module) {
        return systemService.countAuditLogs(action, module);
    }
}
