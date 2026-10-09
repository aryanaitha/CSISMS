package com.csims.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {
    private final JdbcTemplate jdbcTemplate;

    public DashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getMetrics(String roleName, Long userId) {
        Map<String, Object> metrics = new HashMap<>();

        // Core counts matching exact schema
        metrics.put("totalProjects", queryInt("SELECT COUNT(*) FROM projects"));
        metrics.put("activeProjects", queryInt("SELECT COUNT(*) FROM projects WHERE status NOT IN ('Completed', 'Archived')"));
        metrics.put("totalWorkers", queryInt("SELECT COUNT(*) FROM workers WHERE status = 'ACTIVE'"));
        metrics.put("totalTasks", queryInt("SELECT COUNT(*) FROM tasks"));
        metrics.put("inProgressTasks", queryInt("SELECT COUNT(*) FROM tasks WHERE status = 'In Progress'"));
        metrics.put("lowStockCount", queryInt("SELECT COUNT(*) FROM materials WHERE quantity_on_hand <= min_stock"));
        metrics.put("openViolations", queryInt("SELECT COUNT(*) FROM safety_violations WHERE status = 'OPEN'"));
        metrics.put("openIncidents", queryInt("SELECT COUNT(*) FROM incidents WHERE status NOT IN ('Resolved', 'Closed')"));
        metrics.put("pendingRequests", queryInt("SELECT COUNT(*) FROM account_requests WHERE status = 'PENDING'"));
        metrics.put("equipmentCount", queryInt("SELECT COUNT(*) FROM equipment"));
        metrics.put("equipmentMaintenanceDue", queryInt("SELECT COUNT(*) FROM equipment WHERE status = 'Maintenance' OR next_service_due <= CURDATE()"));

        // Recent items matching exact schema
        metrics.put("recentProjects", jdbcTemplate.queryForList("SELECT project_id, project_name, status, progress_percent, budget FROM projects ORDER BY updated_at DESC LIMIT 5"));
        metrics.put("recentIncidents", jdbcTemplate.queryForList("SELECT incident_id, title, severity, status, incident_date FROM incidents ORDER BY created_at DESC LIMIT 5"));
        metrics.put("lowStockItems", jdbcTemplate.queryForList("SELECT material_id AS item_id, name AS item_name, quantity_on_hand AS current_stock, min_stock AS reorder_level, unit FROM materials WHERE quantity_on_hand <= min_stock LIMIT 5"));

        return metrics;
    }

    private int queryInt(String sql) {
        try {
            Integer val = jdbcTemplate.queryForObject(sql, Integer.class);
            return val != null ? val : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
