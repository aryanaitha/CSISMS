package com.csims.service;

import com.csims.exception.ApiException;
import com.csims.model.Project;
import com.csims.model.ProjectWorker;
import com.csims.repository.ProjectRepository;
import com.csims.repository.WorkforceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final WorkforceRepository workforceRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public ProjectService(ProjectRepository projectRepository,
                          WorkforceRepository workforceRepository,
                          AuditService auditService,
                          NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.workforceRepository = workforceRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public List<Project> getAllProjects(String status) {
        return projectRepository.findAll(status);
    }

    public Project getProjectById(Integer id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ApiException("Project not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer createProject(Project p, Long operatorId, String operatorUsername) {
        Integer id = projectRepository.create(p);
        auditService.log(operatorId, operatorUsername, null, "PROJECT_CREATE", "PROJECT", "PROJECT", String.valueOf(id), null, p.getProjectName(), "SUCCESS", "Created new project " + p.getProjectName());
        return id;
    }

    @Transactional
    public void updateProject(Integer id, Project p, Long operatorId, String operatorUsername) {
        Project existing = getProjectById(id);
        p.setId(id);
        projectRepository.update(p);
        auditService.log(operatorId, operatorUsername, null, "PROJECT_UPDATE", "PROJECT", "PROJECT", String.valueOf(id), existing.getStatus(), p.getStatus(), "SUCCESS", "Updated project details for " + p.getProjectName());
    }

    @Transactional
    public void updateStatus(Integer id, String newStatus, Long operatorId, String operatorUsername) {
        Project existing = getProjectById(id);
        projectRepository.updateStatus(id, newStatus);
        auditService.log(operatorId, operatorUsername, null, "PROJECT_STATUS_UPDATE", "PROJECT", "PROJECT", String.valueOf(id), existing.getStatus(), newStatus, "SUCCESS", "Transitioned status of project " + existing.getProjectName() + " to " + newStatus);
    }

    @Transactional
    public void updateProgress(Integer id, Integer progressPercent, Long operatorId, String operatorUsername) {
        Project existing = getProjectById(id);
        projectRepository.updateProgress(id, progressPercent);
        auditService.log(operatorId, operatorUsername, null, "PROJECT_PROGRESS_UPDATE", "PROJECT", "PROJECT", String.valueOf(id), String.valueOf(existing.getProgressPercent()), String.valueOf(progressPercent), "SUCCESS", "Updated progress of project " + existing.getProjectName() + " to " + progressPercent + "%");
    }

    @Transactional
    public void archiveProject(Integer id, Long operatorId, String operatorUsername) {
        updateStatus(id, "Archived", operatorId, operatorUsername);
    }

    // Workforce assignments
    public List<ProjectWorker> getProjectWorkers(Integer projectId) {
        return workforceRepository.findWorkersByProjectId(projectId);
    }

    @Transactional
    public void assignWorker(Integer projectId, Integer workerId, String roleInProject, Long operatorId, String operatorUsername) {
        Project p = getProjectById(projectId);
        workforceRepository.assignWorkerToProject(projectId, workerId, roleInProject);
        auditService.log(operatorId, operatorUsername, null, "PROJECT_WORKER_ASSIGN", "PROJECT", "WORKER", String.valueOf(workerId), null, String.valueOf(projectId), "SUCCESS", "Assigned worker #" + workerId + " to project " + p.getProjectName());
    }

    @Transactional
    public void removeWorker(Integer projectId, Integer workerId, Long operatorId, String operatorUsername) {
        Project p = getProjectById(projectId);
        workforceRepository.removeWorkerFromProject(projectId, workerId);
        auditService.log(operatorId, operatorUsername, null, "PROJECT_WORKER_REMOVE", "PROJECT", "WORKER", String.valueOf(workerId), String.valueOf(projectId), null, "SUCCESS", "Removed worker #" + workerId + " from project " + p.getProjectName());
    }
}
