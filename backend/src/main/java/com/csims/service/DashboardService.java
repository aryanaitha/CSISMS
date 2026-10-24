package com.csims.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DashboardService {
    private final SystemService systemService;

    public DashboardService(SystemService systemService) {
        this.systemService = systemService;
    }

    public Map<String, Object> getMetrics(String roleName, Long userId) {
        return systemService.getMetrics(roleName, userId);
    }
}
