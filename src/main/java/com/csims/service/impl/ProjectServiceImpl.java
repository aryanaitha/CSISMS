package com.csims.service.impl;

import com.csims.dao.ProjectDAO;
import com.csims.dao.impl.ProjectDAOImpl;
import com.csims.model.Project;
import com.csims.security.PermissionChecker;
import com.csims.service.AuditService;
import com.csims.service.ProjectService;
import com.csims.util.ValidationException;
import com.csims.util.Validator;

import java.util.List;
import java.util.Map;

public class ProjectServiceImpl implements ProjectService {

    private final ProjectDAO projectDAO = new ProjectDAOImpl();
    private final AuditService auditService = new AuditServiceImpl();

    @Override
    public List<Project> getAllProjects() {
        PermissionChecker.require("PROJECT_VIEW");
        return projectDAO.findAll();
    }

    @Override
    public List<Project> searchProjects(String keyword, String status) {
        PermissionChecker.require("PROJECT_VIEW");
        return projectDAO.search(keyword, status);
    }

    @Override
    public Project getProjectById(int projectId) {
        PermissionChecker.require("PROJECT_VIEW");
        return projectDAO.findById(projectId)
            .orElseThrow(() -> new ValidationException("Project #" + projectId + " not found."));
    }

    @Override
    public int createProject(Project project) {
        PermissionChecker.require("PROJECT_CREATE");
        Validator.requireNonEmpty(project.getProjectName(), "Project Name");
        Validator.requireNonEmpty(project.getLocation(), "Project Location");
        if (project.getStartDate() == null) {
            throw new ValidationException("Start date is required.");
        }
        if (project.getEndDate() != null && project.getEndDate().isBefore(project.getStartDate())) {
            throw new ValidationException("End date cannot be earlier than start date.");
        }
        if (project.getBudget() != null && project.getBudget().doubleValue() < 0) {
            throw new ValidationException("Budget cannot be negative.");
        }
        Validator.validateRange(project.getProgressPercent(), 0, 100, "Progress Percentage");

        int id = projectDAO.create(project);
        auditService.logCurrent("PROJECT_CREATE", "PROJECT", "PROJECT", String.valueOf(id),
            null, project.getProjectName(), "SUCCESS", "Created new project: " + project.getProjectName());
        return id;
    }

    @Override
    public boolean updateProject(Project project) {
        PermissionChecker.require("PROJECT_UPDATE");
        Validator.requireNonEmpty(project.getProjectName(), "Project Name");
        Validator.requireNonEmpty(project.getLocation(), "Project Location");
        if (project.getStartDate() == null) {
            throw new ValidationException("Start date is required.");
        }
        if (project.getEndDate() != null && project.getEndDate().isBefore(project.getStartDate())) {
            throw new ValidationException("End date cannot be earlier than start date.");
        }
        Validator.validateRange(project.getProgressPercent(), 0, 100, "Progress Percentage");

        Project old = getProjectById(project.getProjectId());
        String oldVal = old.getProjectName() + " (" + old.getStatus() + ", " + old.getProgressPercent() + "%)";
        String newVal = project.getProjectName() + " (" + project.getStatus() + ", " + project.getProgressPercent() + "%)";

        boolean updated = projectDAO.update(project);
        if (updated) {
            auditService.logCurrent("PROJECT_UPDATE", "PROJECT", "PROJECT", String.valueOf(project.getProjectId()),
                oldVal, newVal, "SUCCESS", "Updated project details.");
        }
        return updated;
    }

    @Override
    public boolean updateProgress(int projectId, int progressPercent) {
        PermissionChecker.require("PROJECT_UPDATE");
        Validator.validateRange(progressPercent, 0, 100, "Progress Percentage");
        Project old = getProjectById(projectId);
        boolean updated = projectDAO.updateProgress(projectId, progressPercent);
        if (updated) {
            auditService.logCurrent("PROJECT_PROGRESS_UPDATE", "PROJECT", "PROJECT", String.valueOf(projectId),
                old.getProgressPercent() + "%", progressPercent + "%", "SUCCESS", "Updated project progress.");
        }
        return updated;
    }

    @Override
    public boolean archiveProject(int projectId) {
        PermissionChecker.require("PROJECT_ARCHIVE");
        Project old = getProjectById(projectId);
        boolean updated = projectDAO.updateStatus(projectId, "Archived");
        if (updated) {
            auditService.logCurrent("PROJECT_ARCHIVE", "PROJECT", "PROJECT", String.valueOf(projectId),
                old.getStatus(), "Archived", "SUCCESS", "Archived project #" + projectId);
        }
        return updated;
    }

    @Override
    public boolean deleteProject(int projectId) {
        PermissionChecker.require("PROJECT_ARCHIVE");
        Project p = getProjectById(projectId);
        boolean deleted = projectDAO.delete(projectId);
        if (deleted) {
            auditService.logCurrent("PROJECT_DELETE", "PROJECT", "PROJECT", String.valueOf(projectId),
                p.getProjectName(), null, "SUCCESS", "Deleted project #" + projectId);
        }
        return deleted;
    }

    @Override
    public Map<String, Integer> getProjectStats() {
        return projectDAO.countByStatus();
    }
}
