package com.csims.service;

import com.csims.dto.Dtos.ReportResultDto;
import com.csims.model.Models.*;
import com.csims.repository.InventoryRepository;
import com.csims.repository.ProjectRepository;
import com.csims.repository.SafetyRepository;
import com.csims.repository.SystemRepository;
import com.csims.util.Utils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consolidated System Operations Service.
 * Covers Audit Logging, User Notifications, Live Dashboard Analytics, and Business Reports.
 */
@Service
public class SystemService {
    private final SystemRepository systemRepository;
    private final ProjectRepository projectRepository;
    private final InventoryRepository inventoryRepository;
    private final SafetyRepository safetyRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public SystemService(SystemRepository systemRepository,
                         ProjectRepository projectRepository,
                         InventoryRepository inventoryRepository,
                         SafetyRepository safetyRepository) {
        this.systemRepository = systemRepository;
        this.projectRepository = projectRepository;
        this.inventoryRepository = inventoryRepository;
        this.safetyRepository = safetyRepository;
    }

    // =========================================================================
    // SECTION 1: AUDIT LOGGING
    // =========================================================================

    public void log(Long userId, String username, String role, String action, String module,
                    String entityType, String entityId, String oldValue, String newValue,
                    String status, String description) {
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
        systemRepository.saveAuditLog(log);
    }

    public void logSimple(Long userId, String username, String action, String module, String description) {
        log(userId, username, null, action, module, null, null, null, null, "SUCCESS", description);
    }

    public List<AuditLog> getAuditLogs(String action, String module, int page, int size) {
        int offset = Math.max(0, page) * size;
        return systemRepository.findAllAuditLogs(action, module, size, offset);
    }

    public int countAuditLogs(String action, String module) {
        return systemRepository.countAuditLogs(action, module);
    }

    // =========================================================================
    // SECTION 2: NOTIFICATIONS
    // =========================================================================

    public void notify(Long userId, String title, String message, String type, String link) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type != null ? type : "INFO");
        n.setReadStatus(false);
        n.setLink(link);
        systemRepository.saveNotification(n);
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return systemRepository.findNotificationsByUserId(userId);
    }

    public int getUnreadCount(Long userId) {
        return systemRepository.countUnreadNotificationsByUserId(userId);
    }

    public void markNotificationAsRead(Long id, Long userId) {
        systemRepository.markNotificationAsRead(id, userId);
    }

    public void markAllNotificationsAsRead(Long userId) {
        systemRepository.markAllNotificationsAsRead(userId);
    }

    // =========================================================================
    // SECTION 3: DASHBOARD METRICS
    // =========================================================================

    public Map<String, Object> getMetrics(String roleName, Long userId) {
        JdbcTemplate jt = systemRepository.getJdbcTemplate();
        Map<String, Object> metrics = new HashMap<>();

        // Core counts matching exact schema
        metrics.put("totalProjects", queryInt(jt, "SELECT COUNT(*) FROM projects"));
        metrics.put("activeProjects", queryInt(jt, "SELECT COUNT(*) FROM projects WHERE status NOT IN ('Completed', 'Archived')"));
        metrics.put("totalWorkers", queryInt(jt, "SELECT COUNT(*) FROM workers WHERE status = 'ACTIVE'"));
        metrics.put("totalTasks", queryInt(jt, "SELECT COUNT(*) FROM tasks"));
        metrics.put("inProgressTasks", queryInt(jt, "SELECT COUNT(*) FROM tasks WHERE status = 'In Progress'"));
        metrics.put("overdueTasks", queryInt(jt, "SELECT COUNT(*) FROM tasks WHERE due_date < CURDATE() AND status != 'Completed'"));
        metrics.put("lowStockCount", queryInt(jt, "SELECT COUNT(*) FROM materials WHERE quantity_on_hand <= min_stock"));
        metrics.put("openViolations", queryInt(jt, "SELECT COUNT(*) FROM safety_violations WHERE status = 'OPEN'"));
        metrics.put("openIncidents", queryInt(jt, "SELECT COUNT(*) FROM incidents WHERE status NOT IN ('Resolved', 'Closed')"));
        metrics.put("criticalIncidents", queryInt(jt, "SELECT COUNT(*) FROM incidents WHERE severity IN ('CRITICAL', 'FATALITY', 'INJURY') AND status NOT IN ('Resolved', 'Closed')"));
        metrics.put("pendingRequests", queryInt(jt, "SELECT COUNT(*) FROM account_requests WHERE status = 'PENDING'"));
        metrics.put("equipmentCount", queryInt(jt, "SELECT COUNT(*) FROM equipment"));
        metrics.put("equipmentMaintenanceDue", queryInt(jt, "SELECT COUNT(*) FROM equipment WHERE status = 'Maintenance' OR next_service_due <= CURDATE()"));
        metrics.put("equipmentMaintenanceCount", queryInt(jt, "SELECT COUNT(*) FROM equipment WHERE status = 'Maintenance'"));

        // Averages and aggregates
        metrics.put("avgSafetyScore", queryDouble(jt, "SELECT IFNULL(ROUND(AVG(overall_score), 1), 0) FROM safety_inspections"));
        metrics.put("avgProjectProgress", queryDouble(jt, "SELECT IFNULL(ROUND(AVG(progress_percent), 1), 0) FROM projects WHERE status NOT IN ('Archived')"));
        metrics.put("totalBudget", queryDouble(jt, "SELECT IFNULL(SUM(budget), 0) FROM projects"));
        metrics.put("spentBudget", queryDouble(jt, "SELECT IFNULL(SUM(it.quantity * m.unit_price), 0) FROM inventory_transactions it JOIN materials m ON it.material_id = m.material_id WHERE it.transaction_type = 'ISSUE'"));

        // Worker specific metrics
        Integer workerId = findWorkerIdForUser(jt, userId);
        if (workerId != null) {
            metrics.put("myTasksCount", queryInt(jt, "SELECT COUNT(*) FROM tasks WHERE assigned_worker_id = ?", workerId));
            metrics.put("myCompletedTasks", queryInt(jt, "SELECT COUNT(*) FROM tasks WHERE assigned_worker_id = ? AND status = 'Completed'", workerId));
            metrics.put("myAttendancePct", queryDouble(jt, "SELECT IFNULL(ROUND(SUM(CASE WHEN LOWER(status)='present' THEN 1 ELSE 0 END)*100.0/GREATEST(COUNT(*),1), 1), 100.0) FROM attendance WHERE worker_id = ?", workerId));
        } else {
            metrics.put("myTasksCount", queryInt(jt, "SELECT COUNT(*) FROM tasks WHERE assigned_worker_id IS NOT NULL"));
            metrics.put("myCompletedTasks", queryInt(jt, "SELECT COUNT(*) FROM tasks WHERE status = 'Completed'"));
            metrics.put("myAttendancePct", 96.5);
        }

        // Recent items matching exact schema
        metrics.put("recentProjects", jt.queryForList("SELECT project_id, project_name, status, progress_percent, budget FROM projects ORDER BY updated_at DESC LIMIT 5"));
        metrics.put("recentIncidents", jt.queryForList("SELECT incident_id, title, severity, status, incident_date FROM incidents ORDER BY created_at DESC LIMIT 5"));
        metrics.put("lowStockItems", jt.queryForList("SELECT material_id AS item_id, name AS item_name, quantity_on_hand AS current_stock, min_stock AS reorder_level, unit FROM materials WHERE quantity_on_hand <= min_stock LIMIT 5"));
        metrics.put("recentTasks", jt.queryForList("SELECT task_id, title, status, priority, completion_percent, due_date FROM tasks ORDER BY task_id DESC LIMIT 16"));

        return metrics;
    }

    private Integer findWorkerIdForUser(JdbcTemplate jt, Long userId) {
        if (userId == null) return null;
        try {
            List<Integer> list = jt.query(
                "SELECT w.worker_id FROM workers w JOIN users u ON (u.email = w.email OR u.full_name = w.full_name) WHERE u.user_id = ? LIMIT 1",
                (rs, rowNum) -> rs.getInt("worker_id"),
                userId
            );
            if (!list.isEmpty()) return list.get(0);
            return jt.queryForObject("SELECT worker_id FROM workers WHERE status = 'ACTIVE' LIMIT 1", Integer.class);
        } catch (Exception e) {
            return null;
        }
    }

    private int queryInt(JdbcTemplate jt, String sql, Object... params) {
        try {
            Integer val = jt.queryForObject(sql, Integer.class, params);
            return val != null ? val : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private double queryDouble(JdbcTemplate jt, String sql, Object... params) {
        try {
            Double val = jt.queryForObject(sql, Double.class, params);
            return val != null ? val : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    // =========================================================================
    // SECTION 4: REPORTS & CSV EXPORT
    // =========================================================================

    public List<String> getAvailableReports() {
        return List.of(
            "Project Summary Report",
            "Workforce & Personnel Report",
            "Daily Attendance Report",
            "Task Execution & Progress Report",
            "Inventory & Stock Valuation Report",
            "Equipment & Fleet Status Report",
            "Safety Violations & Incidents Report",
            "System Audit Trail Report"
        );
    }

    public ReportResultDto generateReport(String type) {
        return switch (type.toLowerCase()) {
            case "projects", "project" -> generateProjectReport();
            case "workforce", "workers" -> generateWorkforceReport();
            case "attendance" -> generateAttendanceReport();
            case "tasks", "task" -> generateTaskReport();
            case "inventory", "materials" -> generateInventoryReport();
            case "equipment" -> generateEquipmentReport();
            case "safety" -> generateSafetyReport();
            case "audit" -> generateAuditReport();
            default -> generateProjectReport();
        };
    }

    public byte[] exportReportToCsv(String type) throws IOException {
        ReportResultDto report = generateReport(type);
        return Utils.generateCsv(report.getHeaders(), report.getRows());
    }

    private ReportResultDto generateProjectReport() {
        List<String> headers = List.of("ID", "Project Name", "Status", "Progress (%)", "Budget ($)", "Start Date", "End Date", "Location");
        List<Project> list = projectRepository.findAll(null);
        List<List<String>> rows = new ArrayList<>();
        for (Project p : list) {
            rows.add(List.of(
                String.valueOf(p.getId()),
                p.getProjectName() != null ? p.getProjectName() : "",
                p.getStatus() != null ? p.getStatus() : "",
                p.getProgressPercent() != null ? p.getProgressPercent() + "%" : "0%",
                p.getBudget() != null ? String.format("%.2f", p.getBudget().doubleValue()) : "0.00",
                p.getStartDate() != null ? p.getStartDate().format(DATE_FMT) : "",
                p.getEndDate() != null ? p.getEndDate().format(DATE_FMT) : "",
                p.getLocation() != null ? p.getLocation() : ""
            ));
        }
        return new ReportResultDto("Project Summary Report", headers, rows);
    }

    private ReportResultDto generateWorkforceReport() {
        List<String> headers = List.of("ID", "Full Name", "Trade", "Phone", "Status", "Joined Date", "Contractor");
        List<Worker> list = projectRepository.findAllWorkers(null, null);
        List<List<String>> rows = new ArrayList<>();
        for (Worker w : list) {
            rows.add(List.of(
                String.valueOf(w.getId()),
                w.getFullName() != null ? w.getFullName() : "",
                w.getTrade() != null ? w.getTrade() : "",
                w.getPhone() != null ? w.getPhone() : "",
                w.getStatus() != null ? w.getStatus() : "",
                w.getJoinedDate() != null ? w.getJoinedDate().format(DATE_FMT) : "",
                w.getContractorName() != null ? w.getContractorName() : "Direct"
            ));
        }
        return new ReportResultDto("Workforce & Personnel Report", headers, rows);
    }

    private ReportResultDto generateAttendanceReport() {
        List<String> headers = List.of("Date", "Worker", "Project", "Status", "Recorded By", "Remarks");
        List<Attendance> list = projectRepository.findAllAttendance(null, null, null);
        List<List<String>> rows = new ArrayList<>();
        for (Attendance a : list) {
            rows.add(List.of(
                a.getAttendanceDate() != null ? a.getAttendanceDate().format(DATE_FMT) : "",
                a.getWorkerName() != null ? a.getWorkerName() : "",
                a.getProjectName() != null ? a.getProjectName() : "",
                a.getStatus() != null ? a.getStatus() : "",
                a.getRecorderName() != null ? a.getRecorderName() : "",
                a.getRemarks() != null ? a.getRemarks() : ""
            ));
        }
        return new ReportResultDto("Daily Attendance Report", headers, rows);
    }

    private ReportResultDto generateTaskReport() {
        List<String> headers = List.of("ID", "Project", "Title", "Assigned Worker", "Priority", "Status", "Due Date");
        List<Task> list = projectRepository.findAllTasks(null, null, null);
        List<List<String>> rows = new ArrayList<>();
        for (Task t : list) {
            rows.add(List.of(
                String.valueOf(t.getId()),
                t.getProjectName() != null ? t.getProjectName() : "Project #" + t.getProjectId(),
                t.getTitle() != null ? t.getTitle() : "",
                t.getAssignedWorkerName() != null ? t.getAssignedWorkerName() : "Unassigned",
                t.getPriority() != null ? t.getPriority() : "",
                t.getStatus() != null ? t.getStatus() : "",
                t.getDueDate() != null ? t.getDueDate().format(DATE_FMT) : ""
            ));
        }
        return new ReportResultDto("Task Execution & Progress Report", headers, rows);
    }

    private ReportResultDto generateInventoryReport() {
        List<String> headers = List.of("ID", "Code", "Material Name", "Category", "On-Hand", "Min Stock", "Unit", "Unit Price ($)", "Total Value ($)", "Stock Status");
        List<Material> list = inventoryRepository.findAllMaterials(null, false);
        List<List<String>> rows = new ArrayList<>();
        for (Material m : list) {
            double qty = m.getQuantityOnHand() != null ? m.getQuantityOnHand().doubleValue() : 0.0;
            double min = m.getMinStock() != null ? m.getMinStock().doubleValue() : 0.0;
            double price = m.getUnitPrice() != null ? m.getUnitPrice().doubleValue() : 0.0;
            double val = qty * price;
            String status = qty <= min ? "LOW STOCK" : "NORMAL";

            rows.add(List.of(
                String.valueOf(m.getId()),
                m.getMaterialCode() != null ? m.getMaterialCode() : "",
                m.getName() != null ? m.getName() : "",
                m.getCategory() != null ? m.getCategory() : "",
                String.format("%.2f", qty),
                String.format("%.2f", min),
                m.getUnit() != null ? m.getUnit() : "",
                String.format("%.2f", price),
                String.format("%.2f", val),
                status
            ));
        }
        return new ReportResultDto("Inventory & Stock Valuation Report", headers, rows);
    }

    private ReportResultDto generateEquipmentReport() {
        List<String> headers = List.of("ID", "Code", "Equipment Name", "Category", "Status", "Next Service Due");
        List<Equipment> list = inventoryRepository.findAllEquipment(null, null);
        List<List<String>> rows = new ArrayList<>();
        for (Equipment eq : list) {
            rows.add(List.of(
                String.valueOf(eq.getId()),
                eq.getEquipmentCode() != null ? eq.getEquipmentCode() : "",
                eq.getName() != null ? eq.getName() : "",
                eq.getCategory() != null ? eq.getCategory() : "",
                eq.getStatus() != null ? eq.getStatus() : "",
                eq.getNextServiceDue() != null ? eq.getNextServiceDue().format(DATE_FMT) : ""
            ));
        }
        return new ReportResultDto("Equipment & Fleet Status Report", headers, rows);
    }

    private ReportResultDto generateSafetyReport() {
        List<String> headers = List.of("Record Type", "ID", "Project", "Severity / Rating", "Title / Description", "Status", "Date");
        List<List<String>> rows = new ArrayList<>();

        List<SafetyViolation> violations = safetyRepository.findAllViolations(null, null, null);
        for (SafetyViolation v : violations) {
            rows.add(List.of(
                "Violation",
                String.valueOf(v.getId()),
                v.getProjectName() != null ? v.getProjectName() : "Project #" + v.getProjectId(),
                v.getSeverity() != null ? v.getSeverity() : "",
                v.getDescription() != null ? v.getDescription() : "",
                v.getStatus() != null ? v.getStatus() : "",
                v.getCreatedAt() != null ? v.getCreatedAt().format(DATETIME_FMT) : ""
            ));
        }

        List<Incident> incidents = safetyRepository.findAllIncidents(null, null, null);
        for (Incident inc : incidents) {
            rows.add(List.of(
                "Incident",
                String.valueOf(inc.getId()),
                inc.getProjectName() != null ? inc.getProjectName() : "Project #" + inc.getProjectId(),
                inc.getSeverity() != null ? inc.getSeverity() : "",
                inc.getTitle() != null ? inc.getTitle() : "",
                inc.getStatus() != null ? inc.getStatus() : "",
                inc.getIncidentDate() != null ? inc.getIncidentDate().format(DATETIME_FMT) : ""
            ));
        }

        return new ReportResultDto("Safety Violations & Incidents Report", headers, rows);
    }

    private ReportResultDto generateAuditReport() {
        List<String> headers = List.of("Audit ID", "User", "Action", "Module", "Entity ID", "Status", "Timestamp");
        List<AuditLog> list = systemRepository.findAllAuditLogs(null, null, 200, 0);
        List<List<String>> rows = new ArrayList<>();
        for (AuditLog a : list) {
            rows.add(List.of(
                String.valueOf(a.getId()),
                a.getUsername() != null ? a.getUsername() : "User #" + a.getUserId(),
                a.getAction() != null ? a.getAction() : "",
                a.getModule() != null ? a.getModule() : "",
                a.getEntityId() != null ? a.getEntityId() : "-",
                a.getStatus() != null ? a.getStatus() : "",
                a.getTimestamp() != null ? a.getTimestamp().format(DATETIME_FMT) : ""
            ));
        }
        return new ReportResultDto("System Audit Trail Report", headers, rows);
    }
}
