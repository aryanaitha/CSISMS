package com.csims.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Project task entity with workflow: Created -> Assigned -> In Progress -> Completed.
 */
public class TaskItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int taskId;
    private int projectId;
    private String title;
    private String description;
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL
    private String status = "Created";   // Created, Assigned, In Progress, Completed
    private int completionPercent = 0;
    private Integer assignedWorkerId;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Joined fields
    private String projectName;
    private String workerName;

    public TaskItem() {}

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCompletionPercent() {
        return completionPercent;
    }

    public void setCompletionPercent(int completionPercent) {
        this.completionPercent = completionPercent;
    }

    public Integer getAssignedWorkerId() {
        return assignedWorkerId;
    }

    public void setAssignedWorkerId(Integer assignedWorkerId) {
        this.assignedWorkerId = assignedWorkerId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
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

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public boolean isOverdue() {
        if (dueDate == null || "Completed".equalsIgnoreCase(status)) {
            return false;
        }
        return dueDate.isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return title + " [" + status + "]";
    }
}
