package com.csims.repository;

import com.csims.model.AuditLog;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuditLogRepository {
    private final SystemRepository systemRepository;

    public AuditLogRepository(SystemRepository systemRepository) {
        this.systemRepository = systemRepository;
    }

    public void save(AuditLog log) {
        systemRepository.saveAuditLog(log);
    }

    public List<AuditLog> findAll(String action, String module, int limit, int offset) {
        return systemRepository.findAllAuditLogs(action, module, limit, offset);
    }

    public int count(String action, String module) {
        return systemRepository.countAuditLogs(action, module);
    }
}
