package com.csims.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Consolidated Models for CSIMS.
 * Contains all entity definitions as public static nested classes.
 */
public class Models {



    public static class AccountRequest {
    private Long id;
    private String fullName;
    private String email;
    private String username;
    private Integer requestedRoleId;
    private String requestedRoleName;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    private String requestType; // REGISTRATION, PASSWORD_RESET
    private Long reviewedBy;
    private String reviewerName;
    private LocalDateTime reviewedAt;
    private String reviewNotes;
    private LocalDateTime createdAt;

    public AccountRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getRequestedRoleId() { return requestedRoleId; }
    public void setRequestedRoleId(Integer requestedRoleId) { this.requestedRoleId = requestedRoleId; }

    public String getRequestedRoleName() { return requestedRoleName; }
    public void setRequestedRoleName(String requestedRoleName) { this.requestedRoleName = requestedRoleName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }



    public static class Attendance {
    private Integer id;
    private Integer workerId;
    private String workerName;
    private Integer projectId;
    private String projectName;
    private LocalDate attendanceDate;
    private String status; // Present, Absent, Half Day, Leave
    private String remarks;
    private Long recordedBy;
    private String recorderName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Attendance() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getWorkerId() { return workerId; }
    public void setWorkerId(Integer workerId) { this.workerId = workerId; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Long getRecordedBy() { return recordedBy; }
    public void setRecordedBy(Long recordedBy) { this.recordedBy = recordedBy; }

    public String getRecorderName() { return recorderName; }
    public void setRecorderName(String recorderName) { this.recorderName = recorderName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }



    public static class AuditLog {
    private Long id;
    private Long userId;
    private String username;
    private String role;
    private String action;
    private String module;
    private String entityType;
    private String entityId;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;
    private String status;
    private String description;

    public AuditLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    }



    public static class Contractor {
    private Integer id;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime createdAt;

    public Contractor() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }



    public static class Equipment {
    private Integer id;
    private String equipmentCode;
    private String name;
    private String category;
    private String status; // Available, In Use, Maintenance, Retired
    private Integer projectId;
    private String projectName;
    private LocalDate purchaseDate;
    private LocalDate lastServiceDate;
    private LocalDate nextServiceDue;

    public Equipment() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public LocalDate getLastServiceDate() { return lastServiceDate; }
    public void setLastServiceDate(LocalDate lastServiceDate) { this.lastServiceDate = lastServiceDate; }

    public LocalDate getNextServiceDue() { return nextServiceDue; }
    public void setNextServiceDue(LocalDate nextServiceDue) { this.nextServiceDue = nextServiceDue; }
    }



    public static class Incident {
    private Integer id;
    private Integer projectId;
    private String projectName;
    private LocalDateTime incidentDate;
    private String severity; // FATALITY, INJURY, NEAR_MISS, PROPERTY_DAMAGE
    private String title;
    private String description;
    private Long investigatorId;
    private String investigatorName;
    private String actionsTaken;
    private String status; // Reported, Under Investigation, Resolved, Closed
    private Long reportedBy;
    private String reporterName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Incident() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public LocalDateTime getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDateTime incidentDate) { this.incidentDate = incidentDate; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getInvestigatorId() { return investigatorId; }
    public void setInvestigatorId(Long investigatorId) { this.investigatorId = investigatorId; }

    public String getInvestigatorName() { return investigatorName; }
    public void setInvestigatorName(String investigatorName) { this.investigatorName = investigatorName; }

    public String getActionsTaken() { return actionsTaken; }
    public void setActionsTaken(String actionsTaken) { this.actionsTaken = actionsTaken; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getReportedBy() { return reportedBy; }
    public void setReportedBy(Long reportedBy) { this.reportedBy = reportedBy; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }



    public static class InventoryTransaction {
    private Integer id;
    private Integer materialId;
    private String materialName;
    private String materialCode;
    private String transactionType; // RECEIPT, ISSUE, ADJUSTMENT
    private BigDecimal quantity;
    private Integer projectId;
    private String projectName;
    private String referenceNo;
    private String remarks;
    private Long performedBy;
    private String performerName;
    private LocalDateTime transactionDate;

    public InventoryTransaction() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getMaterialId() { return materialId; }
    public void setMaterialId(Integer materialId) { this.materialId = materialId; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Long getPerformedBy() { return performedBy; }
    public void setPerformedBy(Long performedBy) { this.performedBy = performedBy; }

    public String getPerformerName() { return performerName; }
    public void setPerformerName(String performerName) { this.performerName = performerName; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    }



    public static class MaintenanceRecord {
    private Integer id;
    private Integer equipmentId;
    private String equipmentName;
    private String equipmentCode;
    private String maintenanceType; // Preventative, Corrective, Inspection
    private String status; // Due, In Progress, Completed
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private BigDecimal cost;
    private String technician;
    private String notes;
    private LocalDateTime createdAt;

    public MaintenanceRecord() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Integer equipmentId) { this.equipmentId = equipmentId; }

    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }

    public String getMaintenanceType() { return maintenanceType; }
    public void setMaintenanceType(String maintenanceType) { this.maintenanceType = maintenanceType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public LocalDate getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    public String getTechnician() { return technician; }
    public void setTechnician(String technician) { this.technician = technician; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }



    public static class Material {
    private Integer id;
    private String materialCode;
    private String name;
    private String category;
    private String unit;
    private BigDecimal quantityOnHand;
    private BigDecimal minStock;
    private BigDecimal maxStock;
    private BigDecimal unitPrice;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Material() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(BigDecimal quantityOnHand) { this.quantityOnHand = quantityOnHand; }

    public BigDecimal getMinStock() { return minStock; }
    public void setMinStock(BigDecimal minStock) { this.minStock = minStock; }

    public BigDecimal getMaxStock() { return maxStock; }
    public void setMaxStock(BigDecimal maxStock) { this.maxStock = maxStock; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }



    public static class Notification {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private String type; // INFO, WARNING, DANGER, SUCCESS
    private boolean readStatus;
    private String link;
    private LocalDateTime createdAt;

    public Notification() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isReadStatus() { return readStatus; }
    public void setReadStatus(boolean readStatus) { this.readStatus = readStatus; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }


    public static class Permission {
    private Integer id;
    private String name;
    private String module;
    private String description;

    public Permission() {}

    public Permission(Integer id, String name, String module, String description) {
        this.id = id;
        this.name = name;
        this.module = module;
        this.description = description;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    }



    public static class Project {
    private Integer id;
    private String projectName;
    private String description;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private String status; // Planning, In Progress, On Hold, Completed, Archived
    private Integer progressPercent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Project() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getName() { return projectName; }
    public void setName(String name) { this.projectName = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }



    public static class ProjectWorker {
    private Integer projectId;
    private Integer workerId;
    private String workerName;
    private String trade;
    private String roleInProject;
    private LocalDate assignedDate;

    public ProjectWorker() {}

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public Integer getWorkerId() { return workerId; }
    public void setWorkerId(Integer workerId) { this.workerId = workerId; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public String getTrade() { return trade; }
    public void setTrade(String trade) { this.trade = trade; }

    public String getRoleInProject() { return roleInProject; }
    public void setRoleInProject(String roleInProject) { this.roleInProject = roleInProject; }

    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }
    }


    public static class Role {
    private Integer id;
    private String name;
    private String description;

    public Role() {}

    public Role(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    }



    public static class SafetyInspection {
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



    public static class SafetyViolation {
    private Integer id;
    private Integer inspectionId;
    private Integer projectId;
    private String projectName;
    private Integer workerId;
    private String workerName;
    private String severity; // CRITICAL, MAJOR, MINOR
    private String description;
    private String correctiveAction;
    private String status; // OPEN, RESOLVED
    private LocalDate resolutionDate;
    private Long reportedBy;
    private String reporterName;
    private LocalDateTime createdAt;

    public SafetyViolation() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getInspectionId() { return inspectionId; }
    public void setInspectionId(Integer inspectionId) { this.inspectionId = inspectionId; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public Integer getWorkerId() { return workerId; }
    public void setWorkerId(Integer workerId) { this.workerId = workerId; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCorrectiveAction() { return correctiveAction; }
    public void setCorrectiveAction(String correctiveAction) { this.correctiveAction = correctiveAction; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getResolutionDate() { return resolutionDate; }
    public void setResolutionDate(LocalDate resolutionDate) { this.resolutionDate = resolutionDate; }

    public Long getReportedBy() { return reportedBy; }
    public void setReportedBy(Long reportedBy) { this.reportedBy = reportedBy; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }



    public static class Task {
    private Integer id;
    private Integer projectId;
    private String projectName;
    private String title;
    private String description;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private String status;   // Created, In Progress, Under Review, Completed, Blocked
    private Integer completionPercent;
    private Integer assignedWorkerId;
    private String assignedWorkerName;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Task() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCompletionPercent() { return completionPercent; }
    public void setCompletionPercent(Integer completionPercent) { this.completionPercent = completionPercent; }

    public Integer getAssignedWorkerId() { return assignedWorkerId; }
    public void setAssignedWorkerId(Integer assignedWorkerId) { this.assignedWorkerId = assignedWorkerId; }

    public String getAssignedWorkerName() { return assignedWorkerName; }
    public void setAssignedWorkerName(String assignedWorkerName) { this.assignedWorkerName = assignedWorkerName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }



    public static class User {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String fullName;
    private Integer roleId;
    private String roleName;
    private boolean active;
    private int failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private boolean mustChangePassword;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }



    public static class Worker {
    private Integer id;
    private String fullName;
    private String email;
    private String phone;
    private String trade;
    private Integer contractorId;
    private String contractorName;
    private String status; // ACTIVE, INACTIVE
    private LocalDate joinedDate;

    public Worker() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getTrade() { return trade; }
    public void setTrade(String trade) { this.trade = trade; }

    public Integer getContractorId() { return contractorId; }
    public void setContractorId(Integer contractorId) { this.contractorId = contractorId; }

    public String getContractorName() { return contractorName; }
    public void setContractorName(String contractorName) { this.contractorName = contractorName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getJoinedDate() { return joinedDate; }
    public void setJoinedDate(LocalDate joinedDate) { this.joinedDate = joinedDate; }
    }

}
