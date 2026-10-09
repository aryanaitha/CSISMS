package com.csims.service;

import com.csims.model.AuditLog;
import com.csims.model.User;
import java.time.LocalDate;
import java.util.List;

public interface AuditService {
    void log(User user, String action, String module, String entityType, String entityId,
             String oldValue, String newValue, String status, String description);

    void log(String username, String role, String action, String module, String entityType,
             String entityId, String oldValue, String newValue, String status, String description);

    void logCurrent(String action, String module, String entityType, String entityId,
                    String oldValue, String newValue, String status, String description);

    List<AuditLog> getAllAuditLogs();
    List<AuditLog> searchAuditLogs(String username, String action, String module, LocalDate startDate, LocalDate endDate, String status);
}
