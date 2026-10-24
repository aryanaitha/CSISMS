package com.csims.service;

import com.csims.exception.AppExceptions.ApiException;
import com.csims.model.Models.Attendance;
import com.csims.model.Models.Contractor;
import com.csims.model.Models.Project;
import com.csims.model.Models.ProjectWorker;
import com.csims.model.Models.Task;
import com.csims.model.Models.Worker;
import com.csims.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Consolidated Project Management Service.
 * Covers Projects, Workforce (Workers & Contractors), Tasks, and Attendance.
 */
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final SystemService systemService;

    public ProjectService(ProjectRepository projectRepository,
                          SystemService systemService) {
        this.projectRepository = projectRepository;
        this.systemService = systemService;
    }

    // =========================================================================
    // SECTION 1: PROJECTS
    // =========================================================================

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
        systemService.log(operatorId, operatorUsername, null, "PROJECT_CREATE", "PROJECT", "PROJECT", String.valueOf(id), null, p.getProjectName(), "SUCCESS", "Created new project " + p.getProjectName());
        return id;
    }

    @Transactional
    public void updateProject(Integer id, Project p, Long operatorId, String operatorUsername) {
        Project existing = getProjectById(id);
        p.setId(id);
        projectRepository.update(p);
        systemService.log(operatorId, operatorUsername, null, "PROJECT_UPDATE", "PROJECT", "PROJECT", String.valueOf(id), existing.getStatus(), p.getStatus(), "SUCCESS", "Updated project details for " + p.getProjectName());
    }

    @Transactional
    public void updateStatus(Integer id, String newStatus, Long operatorId, String operatorUsername) {
        Project existing = getProjectById(id);
        projectRepository.updateStatus(id, newStatus);
        systemService.log(operatorId, operatorUsername, null, "PROJECT_STATUS_UPDATE", "PROJECT", "PROJECT", String.valueOf(id), existing.getStatus(), newStatus, "SUCCESS", "Transitioned status of project " + existing.getProjectName() + " to " + newStatus);
    }

    @Transactional
    public void updateProgress(Integer id, Integer progressPercent, Long operatorId, String operatorUsername) {
        Project existing = getProjectById(id);
        projectRepository.updateProgress(id, progressPercent);
        systemService.log(operatorId, operatorUsername, null, "PROJECT_PROGRESS_UPDATE", "PROJECT", "PROJECT", String.valueOf(id), String.valueOf(existing.getProgressPercent()), String.valueOf(progressPercent), "SUCCESS", "Updated progress of project " + existing.getProjectName() + " to " + progressPercent + "%");
    }

    @Transactional
    public void archiveProject(Integer id, Long operatorId, String operatorUsername) {
        updateStatus(id, "Archived", operatorId, operatorUsername);
    }

    // Workforce assignments in Projects
    public List<ProjectWorker> getProjectWorkers(Integer projectId) {
        return projectRepository.findWorkersByProjectId(projectId);
    }

    @Transactional
    public void assignWorker(Integer projectId, Integer workerId, String roleInProject, Long operatorId, String operatorUsername) {
        Project p = getProjectById(projectId);
        projectRepository.assignWorkerToProject(projectId, workerId, roleInProject);
        systemService.log(operatorId, operatorUsername, null, "PROJECT_WORKER_ASSIGN", "PROJECT", "WORKER", String.valueOf(workerId), null, String.valueOf(projectId), "SUCCESS", "Assigned worker #" + workerId + " to project " + p.getProjectName());
    }

    @Transactional
    public void removeWorker(Integer projectId, Integer workerId, Long operatorId, String operatorUsername) {
        Project p = getProjectById(projectId);
        projectRepository.removeWorkerFromProject(projectId, workerId);
        systemService.log(operatorId, operatorUsername, null, "PROJECT_WORKER_REMOVE", "PROJECT", "WORKER", String.valueOf(workerId), String.valueOf(projectId), null, "SUCCESS", "Removed worker #" + workerId + " from project " + p.getProjectName());
    }

    // =========================================================================
    // SECTION 2: WORKFORCE & CONTRACTORS
    // =========================================================================

    public List<Contractor> getAllContractors() {
        return projectRepository.findAllContractors();
    }

    public Contractor getContractorById(Integer id) {
        return projectRepository.findContractorById(id)
                .orElseThrow(() -> new ApiException("Contractor not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer createContractor(Contractor c, Long operatorId, String operatorUsername) {
        Integer id = projectRepository.createContractor(c);
        systemService.log(operatorId, operatorUsername, null, "CONTRACTOR_CREATE", "WORKFORCE", "CONTRACTOR", String.valueOf(id), null, c.getCompanyName(), "SUCCESS", "Created contractor " + c.getCompanyName());
        return id;
    }

    @Transactional
    public void updateContractor(Integer id, Contractor c, Long operatorId, String operatorUsername) {
        Contractor existing = getContractorById(id);
        c.setId(id);
        projectRepository.updateContractor(c);
        systemService.log(operatorId, operatorUsername, null, "CONTRACTOR_UPDATE", "WORKFORCE", "CONTRACTOR", String.valueOf(id), existing.getCompanyName(), c.getCompanyName(), "SUCCESS", "Updated contractor details for " + c.getCompanyName());
    }

    public List<Worker> getAllWorkers(String status, Integer contractorId) {
        return projectRepository.findAllWorkers(status, contractorId);
    }

    public Worker getWorkerById(Integer id) {
        return projectRepository.findWorkerById(id)
                .orElseThrow(() -> new ApiException("Worker not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer createWorker(Worker w, Long operatorId, String operatorUsername) {
        Integer id = projectRepository.createWorker(w);
        systemService.log(operatorId, operatorUsername, null, "WORKER_CREATE", "WORKFORCE", "WORKER", String.valueOf(id), null, w.getFullName(), "SUCCESS", "Created worker " + w.getFullName());
        return id;
    }

    @Transactional
    public void updateWorker(Integer id, Worker w, Long operatorId, String operatorUsername) {
        Worker existing = getWorkerById(id);
        w.setId(id);
        projectRepository.updateWorker(w);
        systemService.log(operatorId, operatorUsername, null, "WORKER_UPDATE", "WORKFORCE", "WORKER", String.valueOf(id), existing.getFullName(), w.getFullName(), "SUCCESS", "Updated worker details for " + w.getFullName());
    }

    // =========================================================================
    // SECTION 3: TASKS
    // =========================================================================

    public List<Task> getAllTasks(Integer projectId, String status, Integer workerId) {
        return projectRepository.findAllTasks(projectId, status, workerId);
    }

    public Task getTaskById(Integer id) {
        return projectRepository.findTaskById(id)
                .orElseThrow(() -> new ApiException("Task not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer createTask(Task t, Long operatorId, String operatorUsername) {
        Integer id = projectRepository.createTask(t);
        systemService.log(operatorId, operatorUsername, null, "TASK_CREATE", "TASK", "TASK", String.valueOf(id), null, t.getTitle(), "SUCCESS", "Created task " + t.getTitle());
        return id;
    }

    @Transactional
    public void updateTask(Integer id, Task t, Long operatorId, String operatorUsername) {
        Task existing = getTaskById(id);
        t.setId(id);
        projectRepository.updateTask(t);
        systemService.log(operatorId, operatorUsername, null, "TASK_UPDATE", "TASK", "TASK", String.valueOf(id), existing.getStatus(), t.getStatus(), "SUCCESS", "Updated task details for " + t.getTitle());
    }

    @Transactional
    public void updateTaskStatus(Integer id, String status, Integer progressPercent, Long operatorId, String operatorUsername) {
        Task existing = getTaskById(id);
        int finalProgress = progressPercent != null ? progressPercent : existing.getCompletionPercent();
        if ("Completed".equalsIgnoreCase(status)) {
            finalProgress = 100;
        }
        projectRepository.updateTaskStatus(id, status, finalProgress);
        systemService.log(operatorId, operatorUsername, null, "TASK_STATUS_UPDATE", "TASK", "TASK", String.valueOf(id), existing.getStatus(), status, "SUCCESS", "Updated task #" + id + " to " + status + " (" + finalProgress + "%)");
    }

    @Transactional
    public void assignTaskWorker(Integer id, Integer workerId, Long operatorId, String operatorUsername) {
        Task existing = getTaskById(id);
        projectRepository.assignTaskWorker(id, workerId);
        systemService.log(operatorId, operatorUsername, null, "TASK_ASSIGN", "TASK", "TASK", String.valueOf(id), String.valueOf(existing.getAssignedWorkerId()), String.valueOf(workerId), "SUCCESS", "Assigned task #" + id + " to worker #" + workerId);
    }

    // =========================================================================
    // SECTION 4: ATTENDANCE
    // =========================================================================

    public List<Attendance> getAttendance(Integer projectId, LocalDate date, Integer workerId) {
        return projectRepository.findAllAttendance(projectId, date, workerId);
    }

    public Attendance getAttendanceById(Integer id) {
        return projectRepository.findAttendanceById(id)
                .orElseThrow(() -> new ApiException("Attendance record not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void recordAttendance(Attendance a, Long operatorId, String operatorUsername) {
        a.setRecordedBy(operatorId);
        projectRepository.recordAttendance(a);
        systemService.log(operatorId, operatorUsername, null, "ATTENDANCE_RECORD", "ATTENDANCE", "ATTENDANCE", String.valueOf(a.getWorkerId()), null, a.getStatus(), "SUCCESS", "Recorded attendance for worker #" + a.getWorkerId() + " on " + a.getAttendanceDate() + ": " + a.getStatus());
    }

    @Transactional
    public void correctAttendance(Integer id, String status, String remarks, Long operatorId, String operatorUsername) {
        Attendance existing = getAttendanceById(id);
        projectRepository.correctAttendance(id, status, remarks, operatorId);
        systemService.log(operatorId, operatorUsername, null, "ATTENDANCE_CORRECT", "ATTENDANCE", "ATTENDANCE", String.valueOf(id), existing.getStatus(), status, "SUCCESS", "Corrected attendance record #" + id + " from " + existing.getStatus() + " to " + status + ". Reason: " + remarks);
    }
}
