package com.csims.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Safety audit inspection with checklist scoring.
 */
public class SafetyInspection implements Serializable {
    private static final long serialVersionUID = 1L;

    private int inspectionId;
    private int projectId;
    private int inspectorId;
    private LocalDate inspectionDate;
    private int ppeScore = 100;
    private int workerSafetyScore = 100;
    private int equipmentScore = 100;
    private int workAreaScore = 100;
    private int emergencyScore = 100;
    private BigDecimal overallScore = new BigDecimal("100.00");
    private String remarks;
    private LocalDateTime createdAt;

    // Joined fields
    private String projectName;
    private String inspectorName;

    public SafetyInspection() {}

    public int getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(int inspectionId) {
        this.inspectionId = inspectionId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getInspectorId() {
        return inspectorId;
    }

    public void setInspectorId(int inspectorId) {
        this.inspectorId = inspectorId;
    }

    public LocalDate getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(LocalDate inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public int getPpeScore() {
        return ppeScore;
    }

    public void setPpeScore(int ppeScore) {
        this.ppeScore = ppeScore;
        computeOverallScore();
    }

    public int getWorkerSafetyScore() {
        return workerSafetyScore;
    }

    public void setWorkerSafetyScore(int workerSafetyScore) {
        this.workerSafetyScore = workerSafetyScore;
        computeOverallScore();
    }

    public int getEquipmentScore() {
        return equipmentScore;
    }

    public void setEquipmentScore(int equipmentScore) {
        this.equipmentScore = equipmentScore;
        computeOverallScore();
    }

    public int getWorkAreaScore() {
        return workAreaScore;
    }

    public void setWorkAreaScore(int workAreaScore) {
        this.workAreaScore = workAreaScore;
        computeOverallScore();
    }

    public int getEmergencyScore() {
        return emergencyScore;
    }

    public void setEmergencyScore(int emergencyScore) {
        this.emergencyScore = emergencyScore;
        computeOverallScore();
    }

    public BigDecimal getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }

    public void computeOverallScore() {
        double avg = (ppeScore + workerSafetyScore + equipmentScore + workAreaScore + emergencyScore) / 5.0;
        this.overallScore = BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getInspectorName() {
        return inspectorName;
    }

    public void setInspectorName(String inspectorName) {
        this.inspectorName = inspectorName;
    }

    @Override
    public String toString() {
        return "Inspection #" + inspectionId + " (" + overallScore + "%)";
    }
}
