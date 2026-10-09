package com.csims.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Site accident or incident report.
 * Workflow: Reported -> Under Investigation -> Action Taken -> Resolved.
 */
public class Incident implements Serializable {
    private static final long serialVersionUID = 1L;

    private int incidentId;
    private int projectId;
    private LocalDateTime incidentDate;
    private String severity = "Minor"; // Minor, Moderate, Major, Critical
    private String title;
    private String description;
    private Integer investigatorId;
    private String actionsTaken;
    private String status = "Reported"; // Reported, Under Investigation, Action Taken, Resolved
    private Integer reportedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Joined fields
    private String projectName;
    private String investigatorName;
    private String reporterName;

    public Incident() {}

    public int getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(int incidentId) {
        this.incidentId = incidentId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public LocalDateTime getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDateTime incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getInvestigatorId() {
        return investigatorId;
    }

    public void setInvestigatorId(Integer investigatorId) {
        this.investigatorId = investigatorId;
    }

    public String getActionsTaken() {
        return actionsTaken;
    }

    public void setActionsTaken(String actionsTaken) {
        this.actionsTaken = actionsTaken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(Integer reportedBy) {
        this.reportedBy = reportedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getInvestigatorName() {
        return investigatorName;
    }

    public void setInvestigatorName(String investigatorName) {
        this.investigatorName = investigatorName;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    @Override
    public String toString() {
        return "[" + severity + "] " + title + " (" + status + ")";
    }
}
