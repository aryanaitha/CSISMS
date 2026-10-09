package com.csims.dao;

import com.csims.model.AuditLog;
import java.time.LocalDate;
import java.util.List;

/**
 * Append-only and read-only data access object for audit logs.
 * No update or delete methods exist to preserve immutable audit trail.
 */
public interface AuditLogDAO {
    int insert(AuditLog log);
    List<AuditLog> findAll();
    List<AuditLog> search(String username, String action, String module, LocalDate startDate, LocalDate endDate, String status);
    int countTotal();
}
