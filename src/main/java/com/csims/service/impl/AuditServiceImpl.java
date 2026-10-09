package com.csims.service.impl;

import com.csims.dao.AuditLogDAO;
import com.csims.dao.impl.AuditLogDAOImpl;
import com.csims.model.AuditLog;
import com.csims.model.User;
import com.csims.security.PermissionChecker;
import com.csims.security.SessionContext;
import com.csims.service.AuditService;

import java.time.LocalDate;
import java.util.List;

public class AuditServiceImpl implements AuditService {

    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    @Override
    public void log(User user, String action, String module, String entityType, String entityId,
                    String oldValue, String newValue, String status, String description) {
        AuditLog log = new AuditLog();
        if (user != null) {
            log.setUserId(user.getUserId());
            log.setUsername(user.getUsername());
            log.setRole(user.getRoleName() != null ? user.getRoleName() : "User #" + user.getRoleId());
        } else {
            log.setUsername("SYSTEM");
            log.setRole("SYSTEM");
        }
        log.setAction(action);
        log.setModule(module);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setStatus(status != null ? status : "SUCCESS");
        log.setDescription(description);

        auditLogDAO.insert(log);
    }

    @Override
    public void log(String username, String role, String action, String module, String entityType,
                    String entityId, String oldValue, String newValue, String status, String description) {
        AuditLog log = new AuditLog();
        log.setUsername(username != null ? username : "ANONYMOUS");
        log.setRole(role != null ? role : "GUEST");
        log.setAction(action);
        log.setModule(module);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setStatus(status != null ? status : "SUCCESS");
        log.setDescription(description);

        auditLogDAO.insert(log);
    }

    @Override
    public void logCurrent(String action, String module, String entityType, String entityId,
                           String oldValue, String newValue, String status, String description) {
        User user = SessionContext.getInstance().getCurrentUser();
        log(user, action, module, entityType, entityId, oldValue, newValue, status, description);
    }

    @Override
    public List<AuditLog> getAllAuditLogs() {
        PermissionChecker.require("AUDIT_VIEW");
        return auditLogDAO.findAll();
    }

    @Override
    public List<AuditLog> searchAuditLogs(String username, String action, String module,
                                          LocalDate startDate, LocalDate endDate, String status) {
        PermissionChecker.require("AUDIT_VIEW");
        return auditLogDAO.search(username, action, module, startDate, endDate, status);
    }
}
