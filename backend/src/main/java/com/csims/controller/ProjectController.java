package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.Models.*;
import com.csims.security.CustomUserDetails;
import com.csims.service.ProjectService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Consolidated Project, Workforce, Task and Attendance Controller.
 * Handles /api/projects, /api/workforce, /api/tasks, and /api/attendance.
 */
@RestController
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // =========================================================================
    // SECTION 1: PROJECTS (/api/projects)
    // =========================================================================

    @GetMapping("/api/projects")
    @PreAuthorize("hasAuthority('PROJECT_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Project>>> getAllProjects(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getAllProjects(status)));
    }

    @GetMapping("/api/projects/{id}")
    @PreAuthorize("hasAuthority('PROJECT_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Project>> getProjectById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getProjectById(id)));
    }

    @PostMapping("/api/projects")
    @PreAuthorize("hasAuthority('PROJECT_CREATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Integer>> createProject(@RequestBody Project project,
                                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer id = projectService.createProject(project, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Project created successfully", id));
    }

    @PutMapping("/api/projects/{id}")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateProject(@PathVariable Integer id,
                                                           @RequestBody Project project,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.updateProject(id, project, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Project updated successfully", null));
    }

    @PatchMapping("/api/projects/{id}/status")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(@PathVariable Integer id,
                                                          @RequestBody Map<String, String> body,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        String status = body.get("status");
        projectService.updateStatus(id, status, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Project status updated", null));
    }

    @PatchMapping("/api/projects/{id}/progress")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateProgress(@PathVariable Integer id,
                                                            @RequestBody Map<String, Integer> body,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer progress = body.get("progressPercent");
        projectService.updateProgress(id, progress, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Project progress updated", null));
    }

    @PostMapping("/api/projects/{id}/archive")
    @PreAuthorize("hasAuthority('PROJECT_ARCHIVE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> archiveProject(@PathVariable Integer id,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.archiveProject(id, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Project archived", null));
    }

    @GetMapping("/api/projects/{id}/workers")
    @PreAuthorize("hasAuthority('PROJECT_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<ProjectWorker>>> getProjectWorkers(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getProjectWorkers(id)));
    }

    @PostMapping("/api/projects/{id}/workers")
    @PreAuthorize("hasAuthority('WORKFORCE_ASSIGN') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> assignWorker(@PathVariable Integer id,
                                                          @RequestBody Map<String, Object> body,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer workerId = ((Number) body.get("workerId")).intValue();
        String roleInProject = (String) body.getOrDefault("roleInProject", "Site Worker");
        projectService.assignWorker(id, workerId, roleInProject, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Worker assigned to project", null));
    }

    @DeleteMapping("/api/projects/{id}/workers/{workerId}")
    @PreAuthorize("hasAuthority('WORKFORCE_ASSIGN') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> removeWorker(@PathVariable Integer id,
                                                          @PathVariable Integer workerId,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.removeWorker(id, workerId, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Worker removed from project", null));
    }

    // =========================================================================
    // SECTION 2: WORKFORCE (/api/workforce)
    // =========================================================================

    @GetMapping("/api/workforce/workers")
    @PreAuthorize("hasAuthority('WORKFORCE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Worker>>> getAllWorkers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer contractorId) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getAllWorkers(status, contractorId)));
    }

    @GetMapping("/api/workforce/workers/{id}")
    @PreAuthorize("hasAuthority('WORKFORCE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Worker>> getWorkerById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getWorkerById(id)));
    }

    @PostMapping("/api/workforce/workers")
    @PreAuthorize("hasAuthority('WORKFORCE_CREATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Integer>> createWorker(
            @RequestBody Worker worker,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer id = projectService.createWorker(worker, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Worker added successfully", id));
    }

    @PutMapping("/api/workforce/workers/{id}")
    @PreAuthorize("hasAuthority('WORKFORCE_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateWorker(
            @PathVariable Integer id,
            @RequestBody Worker worker,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.updateWorker(id, worker, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Worker updated successfully", null));
    }

    @GetMapping("/api/workforce/contractors")
    @PreAuthorize("hasAuthority('WORKFORCE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Contractor>>> getAllContractors() {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getAllContractors()));
    }

    @GetMapping("/api/workforce/contractors/{id}")
    @PreAuthorize("hasAuthority('WORKFORCE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Contractor>> getContractorById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getContractorById(id)));
    }

    @PostMapping("/api/workforce/contractors")
    @PreAuthorize("hasAuthority('WORKFORCE_CREATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Integer>> createContractor(
            @RequestBody Contractor contractor,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer id = projectService.createContractor(contractor, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Contractor registered successfully", id));
    }

    @PutMapping("/api/workforce/contractors/{id}")
    @PreAuthorize("hasAuthority('WORKFORCE_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateContractor(
            @PathVariable Integer id,
            @RequestBody Contractor contractor,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.updateContractor(id, contractor, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Contractor updated successfully", null));
    }

    // =========================================================================
    // SECTION 3: TASKS (/api/tasks)
    // =========================================================================

    @GetMapping("/api/tasks")
    @PreAuthorize("hasAuthority('TASK_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Task>>> getAllTasks(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer workerId) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getAllTasks(projectId, status, workerId)));
    }

    @GetMapping("/api/tasks/{id}")
    @PreAuthorize("hasAuthority('TASK_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getTaskById(id)));
    }

    @PostMapping("/api/tasks")
    @PreAuthorize("hasAuthority('TASK_CREATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Integer>> createTask(
            @RequestBody Task task,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer id = projectService.createTask(task, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Task created successfully", id));
    }

    @PutMapping("/api/tasks/{id}")
    @PreAuthorize("hasAuthority('TASK_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateTask(
            @PathVariable Integer id,
            @RequestBody Task task,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.updateTask(id, task, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Task updated successfully", null));
    }

    @PatchMapping("/api/tasks/{id}/status")
    @PreAuthorize("hasAuthority('TASK_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateTaskStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String status = (String) body.get("status");
        Integer progress = body.get("completionPercent") != null ? ((Number) body.get("completionPercent")).intValue() : null;
        projectService.updateTaskStatus(id, status, progress, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Task workflow status updated", null));
    }

    @PatchMapping("/api/tasks/{id}/assign")
    @PreAuthorize("hasAuthority('TASK_ASSIGN') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> assignTaskWorker(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer workerId = body.get("workerId");
        projectService.assignTaskWorker(id, workerId, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Task assigned successfully", null));
    }

    // =========================================================================
    // SECTION 4: ATTENDANCE (/api/attendance)
    // =========================================================================

    @GetMapping("/api/attendance")
    @PreAuthorize("hasAuthority('ATTENDANCE_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Attendance>>> getAttendance(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer workerId) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getAttendance(projectId, date, workerId)));
    }

    @PostMapping("/api/attendance")
    @PreAuthorize("hasAuthority('ATTENDANCE_RECORD') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> recordAttendance(
            @RequestBody Attendance attendance,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.recordAttendance(attendance, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Attendance recorded successfully", null));
    }

    @PatchMapping("/api/attendance/{id}/correct")
    @PreAuthorize("hasAuthority('ATTENDANCE_CORRECT') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> correctAttendance(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String status = body.get("status");
        String remarks = body.get("remarks");
        projectService.correctAttendance(id, status, remarks, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Attendance record corrected", null));
    }
}
