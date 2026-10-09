package com.csims.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SafetyInspection {
    private Integer id;
    private Integer projectId;
    private String projectName;
    private Long inspectorId;
    private String inspectorName;
    private LocalDate inspectionDate;
    private Integer ppeScore;
    private Integer workerSafetyScore;
    private Integer equipmentScore;
    private Integer workAreaScore;
    private Integer emergencyScore;
    private BigDecimal overallScore;
    private String remarks;
    private LocalDateTime createdAt;

    public SafetyInspection() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public Long getInspectorId() { return inspectorId; }
    public void setInspectorId(Long inspectorId) { this.inspectorId = inspectorId; }

    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }

    public LocalDate getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }

    public Integer getPpeScore() { return ppeScore; }
    public void setPpeScore(Integer ppeScore) { this.ppeScore = ppeScore; }

    public Integer getWorkerSafetyScore() { return workerSafetyScore; }
    public void setWorkerSafetyScore(Integer workerSafetyScore) { this.workerSafetyScore = workerSafetyScore; }

    public Integer getEquipmentScore() { return equipmentScore; }
    public void setEquipmentScore(Integer equipmentScore) { this.equipmentScore = equipmentScore; }

    public Integer getWorkAreaScore() { return workAreaScore; }
    public void setWorkAreaScore(Integer workAreaScore) { this.workAreaScore = workAreaScore; }

    public Integer getEmergencyScore() { return emergencyScore; }
    public void setEmergencyScore(Integer emergencyScore) { this.emergencyScore = emergencyScore; }

    public BigDecimal getOverallScore() { return overallScore; }
    public void setOverallScore(BigDecimal overallScore) { this.overallScore = overallScore; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
