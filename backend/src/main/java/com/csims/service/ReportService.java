package com.csims.service;

import com.csims.dto.ReportResultDto;
import com.csims.model.*;
import com.csims.repository.*;
import com.csims.util.CsvExportUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    private final ProjectRepository projectRepository;
    private final WorkforceRepository workforceRepository;
    private final TaskRepository taskRepository;
    private final MaterialRepository materialRepository;
    private final EquipmentRepository equipmentRepository;
    private final SafetyRepository safetyRepository;
    private final IncidentRepository incidentRepository;
    private final AuditLogRepository auditLogRepository;
    private final AttendanceRepository attendanceRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReportService(ProjectRepository projectRepository,
                         WorkforceRepository workforceRepository,
                         TaskRepository taskRepository,
                         MaterialRepository materialRepository,
                         EquipmentRepository equipmentRepository,
                         SafetyRepository safetyRepository,
                         IncidentRepository incidentRepository,
                         AuditLogRepository auditLogRepository,
                         AttendanceRepository attendanceRepository) {
        this.projectRepository = projectRepository;
        this.workforceRepository = workforceRepository;
        this.taskRepository = taskRepository;
        this.materialRepository = materialRepository;
        this.equipmentRepository = equipmentRepository;
        this.safetyRepository = safetyRepository;
        this.incidentRepository = incidentRepository;
        this.auditLogRepository = auditLogRepository;
        this.attendanceRepository = attendanceRepository;
    }

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
        return CsvExportUtil.generateCsv(report.getHeaders(), report.getRows());
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
        List<Worker> list = workforceRepository.findAllWorkers(null, null);
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
        List<Attendance> list = attendanceRepository.findAll(null, null, null);
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
        List<Task> list = taskRepository.findAll(null, null, null);
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
        List<Material> list = materialRepository.findAll(null, false);
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
        List<Equipment> list = equipmentRepository.findAll(null, null);
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

        List<Incident> incidents = incidentRepository.findAll(null, null, null);
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
        List<AuditLog> list = auditLogRepository.findAll(null, null, 200, 0);
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
