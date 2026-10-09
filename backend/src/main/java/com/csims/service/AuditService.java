package com.csims.service;

import com.csims.model.AuditLog;
import com.csims.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(Long userId, String username, String role, String action, String module, String entityType, String entityId, String oldValue, String newValue, String status, String description) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setUsername(username != null ? username : "SYSTEM");
        log.setRole(role);
        log.setAction(action);
        log.setModule(module != null ? module : "SYSTEM");
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setStatus(status != null ? status : "SUCCESS");
        log.setDescription(description);
        auditLogRepository.save(log);
    }

    public void logSimple(Long userId, String username, String action, String module, String description) {
        log(userId, username, null, action, module, null, null, null, null, "SUCCESS", description);
    }

    public List<AuditLog> getAuditLogs(String action, String module, int page, int size) {
        int offset = Math.max(0, page) * size;
        return auditLogRepository.findAll(action, module, size, offset);
    }

    public int countAuditLogs(String action, String module) {
        return auditLogRepository.count(action, module);
    }
}
