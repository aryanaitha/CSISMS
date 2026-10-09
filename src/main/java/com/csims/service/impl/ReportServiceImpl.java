package com.csims.service.impl;

import com.csims.dao.*;
import com.csims.dao.impl.*;
import com.csims.model.*;
import com.csims.security.PermissionChecker;
import com.csims.service.ReportService;
import com.csims.util.CsvExporter;
import com.csims.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReportServiceImpl implements ReportService {

    private final ProjectDAO projectDAO = new ProjectDAOImpl();
    private final WorkerDAO workerDAO = new WorkerDAOImpl();
    private final TaskDAO taskDAO = new TaskDAOImpl();
    private final MaterialDAO materialDAO = new MaterialDAOImpl();
    private final EquipmentDAO equipmentDAO = new EquipmentDAOImpl();
    private final SafetyViolationDAO violationDAO = new SafetyViolationDAOImpl();
    private final IncidentDAO incidentDAO = new IncidentDAOImpl();
    private final AuditLogDAO auditLogDAO = new AuditLogDAOImpl();

    @Override
    public List<String> getAvailableReports() {
        return List.of(
            "Project Summary Report",
            "Workforce & Personnel Report",
            "Task Execution & Progress Report",
            "Inventory & Stock Valuation Report",
            "Equipment & Fleet Status Report",
            "Safety Violations & Incidents Report",
            "System Audit Trail Report"
        );
    }

    @Override
    public ReportResult generateReport(String reportName) {
        PermissionChecker.require("REPORT_VIEW");

        return switch (reportName) {
            case "Project Summary Report" -> generateProjectReport();
            case "Workforce & Personnel Report" -> generateWorkforceReport();
            case "Task Execution & Progress Report" -> generateTaskReport();
            case "Inventory & Stock Valuation Report" -> generateInventoryReport();
            case "Equipment & Fleet Status Report" -> generateEquipmentReport();
            case "Safety Violations & Incidents Report" -> generateSafetyReport();
            case "System Audit Trail Report" -> generateAuditReport();
            default -> new ReportResult(reportName, new String[]{"Message"}, new ArrayList<>());
        };
    }

    private ReportResult generateProjectReport() {
        String[] headers = new String[]{"ID", "Project Name", "Status", "Progress (%)", "Budget ($)", "Start Date", "End Date", "Location"};
        List<Project> list = projectDAO.findAll();
        List<String[]> rows = new ArrayList<>();
        for (Project p : list) {
            rows.add(new String[]{
                String.valueOf(p.getProjectId()),
                p.getProjectName(),
                p.getStatus(),
                p.getProgressPercent() + "%",
                p.getBudget() != null ? String.format("%.2f", p.getBudget().doubleValue()) : "0.00",
                DateUtil.formatDisplayDate(p.getStartDate()),
                DateUtil.formatDisplayDate(p.getEndDate()),
                p.getLocation() != null ? p.getLocation() : ""
            });
        }
        return new ReportResult("Project Summary Report", headers, rows);
    }

    private ReportResult generateWorkforceReport() {
        String[] headers = new String[]{"ID", "Full Name", "Trade", "Phone", "Status", "Joined Date", "Contractor"};
        List<Worker> list = workerDAO.findAll();
        List<String[]> rows = new ArrayList<>();
        for (Worker w : list) {
            rows.add(new String[]{
                String.valueOf(w.getWorkerId()),
                w.getFullName(),
                w.getTrade(),
                w.getPhone() != null ? w.getPhone() : "",
                w.getStatus(),
                DateUtil.formatDisplayDate(w.getJoinedDate()),
                w.getContractorName() != null ? w.getContractorName() : "Direct"
            });
        }
        return new ReportResult("Workforce & Personnel Report", headers, rows);
    }

    private ReportResult generateTaskReport() {
        String[] headers = new String[]{"ID", "Project", "Title", "Assigned Worker", "Priority", "Status", "Due Date"};
        List<TaskItem> list = taskDAO.findAll();
        List<String[]> rows = new ArrayList<>();
        for (TaskItem t : list) {
            rows.add(new String[]{
                String.valueOf(t.getTaskId()),
                t.getProjectName() != null ? t.getProjectName() : "Project #" + t.getProjectId(),
                t.getTitle(),
                t.getWorkerName() != null ? t.getWorkerName() : "Unassigned",
                t.getPriority(),
                t.getStatus(),
                DateUtil.formatDisplayDate(t.getDueDate())
            });
        }
        return new ReportResult("Task Execution & Progress Report", headers, rows);
    }

    private ReportResult generateInventoryReport() {
        String[] headers = new String[]{"ID", "Code", "Material Name", "Category", "On-Hand", "Min Stock", "Unit", "Unit Price ($)", "Total Value ($)", "Stock Status"};
        List<Material> list = materialDAO.findAll();
        List<String[]> rows = new ArrayList<>();
        for (Material m : list) {
            double qty = m.getQuantityOnHand() != null ? m.getQuantityOnHand().doubleValue() : 0.0;
            double min = m.getMinStock() != null ? m.getMinStock().doubleValue() : 0.0;
            double price = m.getUnitPrice() != null ? m.getUnitPrice().doubleValue() : 0.0;
            double val = qty * price;
            String status = qty <= min ? "LOW STOCK" : "NORMAL";

            rows.add(new String[]{
                String.valueOf(m.getMaterialId()),
                m.getMaterialCode(),
                m.getName(),
                m.getCategory(),
                String.format("%.2f", qty),
                String.format("%.2f", min),
                m.getUnit() != null ? m.getUnit() : "",
                String.format("%.2f", price),
                String.format("%.2f", val),
                status
            });
        }
        return new ReportResult("Inventory & Stock Valuation Report", headers, rows);
    }

    private ReportResult generateEquipmentReport() {
        String[] headers = new String[]{"ID", "Code", "Equipment Name", "Category", "Status", "Next Service Due"};
        List<Equipment> list = equipmentDAO.findAll();
        List<String[]> rows = new ArrayList<>();
        for (Equipment eq : list) {
            rows.add(new String[]{
                String.valueOf(eq.getEquipmentId()),
                eq.getEquipmentCode(),
                eq.getName(),
                eq.getCategory(),
                eq.getStatus(),
                DateUtil.formatDisplayDate(eq.getNextServiceDue())
            });
        }
        return new ReportResult("Equipment & Fleet Status Report", headers, rows);
    }

    private ReportResult generateSafetyReport() {
        String[] headers = new String[]{"Record Type", "ID", "Project", "Severity / Rating", "Title / Description", "Status", "Date"};
        List<String[]> rows = new ArrayList<>();

        List<SafetyViolation> violations = violationDAO.findAll();
        for (SafetyViolation v : violations) {
            rows.add(new String[]{
                "Violation",
                String.valueOf(v.getViolationId()),
                v.getProjectName() != null ? v.getProjectName() : "Project #" + v.getProjectId(),
                v.getSeverity(),
                v.getDescription(),
                v.getStatus(),
                DateUtil.formatDateTime(v.getCreatedAt())
            });
        }

        List<Incident> incidents = incidentDAO.findAll();
        for (Incident inc : incidents) {
            rows.add(new String[]{
                "Incident",
                String.valueOf(inc.getIncidentId()),
                inc.getProjectName() != null ? inc.getProjectName() : "Project #" + inc.getProjectId(),
                inc.getSeverity(),
                inc.getTitle(),
                inc.getStatus(),
                DateUtil.formatDateTime(inc.getIncidentDate())
            });
        }

        return new ReportResult("Safety Violations & Incidents Report", headers, rows);
    }

    private ReportResult generateAuditReport() {
        String[] headers = new String[]{"Audit ID", "User", "Action", "Module", "Entity ID", "Status", "Timestamp"};
        List<AuditLog> list = auditLogDAO.findAll();
        List<String[]> rows = new ArrayList<>();
        for (AuditLog a : list) {
            rows.add(new String[]{
                String.valueOf(a.getAuditId()),
                a.getUsername() != null ? a.getUsername() : "User #" + a.getUserId(),
                a.getAction(),
                a.getModule(),
                a.getEntityId() != null ? a.getEntityId() : "-",
                a.getStatus(),
                DateUtil.formatDateTime(a.getTimestamp())
            });
        }
        return new ReportResult("System Audit Trail Report", headers, rows);
    }

    @Override
    public void exportReportToCsv(ReportResult report, File targetFile) throws IOException {
        CsvExporter.exportToFile(targetFile, report.getHeaders(), report.getRows());
    }
}
