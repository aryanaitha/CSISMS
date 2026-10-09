package com.csims.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Worker project allocation entity.
 */
public class ProjectWorker implements Serializable {
    private static final long serialVersionUID = 1L;

    private int projectId;
    private int workerId;
    private String roleInProject = "Site Worker";
    private LocalDate assignedDate;

    // Joined fields
    private String workerName;
    private String trade;
    private String projectName;

    public ProjectWorker() {}

    public ProjectWorker(int projectId, int workerId, String roleInProject, LocalDate assignedDate) {
        this.projectId = projectId;
        this.workerId = workerId;
        this.roleInProject = roleInProject;
        this.assignedDate = assignedDate;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public String getRoleInProject() {
        return roleInProject;
    }

    public void setRoleInProject(String roleInProject) {
        this.roleInProject = roleInProject;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
