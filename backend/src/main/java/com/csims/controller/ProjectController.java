package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.Project;
import com.csims.model.ProjectWorker;
import com.csims.security.CustomUserDetails;
import com.csims.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Project>>> getAllProjects(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getAllProjects(status)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Project>> getProjectById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getProjectById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_CREATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Integer>> createProject(@RequestBody Project project,
                                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer id = projectService.createProject(project, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Project created successfully", id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateProject(@PathVariable Integer id,
                                                           @RequestBody Project project,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.updateProject(id, project, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Project updated successfully", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(@PathVariable Integer id,
                                                          @RequestBody Map<String, String> body,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        String status = body.get("status");
        projectService.updateStatus(id, status, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Project status updated", null));
    }

    @PatchMapping("/{id}/progress")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateProgress(@PathVariable Integer id,
                                                            @RequestBody Map<String, Integer> body,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer progress = body.get("progressPercent");
        projectService.updateProgress(id, progress, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Project progress updated", null));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('PROJECT_ARCHIVE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> archiveProject(@PathVariable Integer id,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.archiveProject(id, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Project archived", null));
    }

    // Project Workers
    @GetMapping("/{id}/workers")
    @PreAuthorize("hasAuthority('PROJECT_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<ProjectWorker>>> getProjectWorkers(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(projectService.getProjectWorkers(id)));
    }

    @PostMapping("/{id}/workers")
    @PreAuthorize("hasAuthority('WORKFORCE_ASSIGN') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> assignWorker(@PathVariable Integer id,
                                                          @RequestBody Map<String, Object> body,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer workerId = ((Number) body.get("workerId")).intValue();
        String roleInProject = (String) body.getOrDefault("roleInProject", "Site Worker");
        projectService.assignWorker(id, workerId, roleInProject, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Worker assigned to project", null));
    }

    @DeleteMapping("/{id}/workers/{workerId}")
    @PreAuthorize("hasAuthority('WORKFORCE_ASSIGN') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> removeWorker(@PathVariable Integer id,
                                                          @PathVariable Integer workerId,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.removeWorker(id, workerId, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Worker removed from project", null));
    }
}
