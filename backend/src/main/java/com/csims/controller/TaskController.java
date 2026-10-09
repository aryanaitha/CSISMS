package com.csims.controller;

import com.csims.dto.ApiResponse;
import com.csims.model.Task;
import com.csims.security.CustomUserDetails;
import com.csims.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TASK_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<List<Task>>> getAllTasks(
            @RequestParam(required = false) Integer projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer workerId) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.getAllTasks(projectId, status, workerId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TASK_VIEW') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.getTaskById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TASK_CREATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Integer>> createTask(
            @RequestBody Task task,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer id = taskService.createTask(task, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Task created successfully", id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TASK_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateTask(
            @PathVariable Integer id,
            @RequestBody Task task,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        taskService.updateTask(id, task, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Task updated successfully", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('TASK_UPDATE') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String status = (String) body.get("status");
        Integer progress = body.get("completionPercent") != null ? ((Number) body.get("completionPercent")).intValue() : null;
        taskService.updateStatus(id, status, progress, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Task workflow status updated", null));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('TASK_ASSIGN') or hasRole('Administrator')")
    public ResponseEntity<ApiResponse<Void>> assignWorker(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer workerId = body.get("workerId");
        taskService.assignWorker(id, workerId, userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Task assigned successfully", null));
    }
}
