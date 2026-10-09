package com.csims.service;

import com.csims.exception.ApiException;
import com.csims.model.Task;
import com.csims.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final AuditService auditService;

    public TaskService(TaskRepository taskRepository, AuditService auditService) {
        this.taskRepository = taskRepository;
        this.auditService = auditService;
    }

    public List<Task> getAllTasks(Integer projectId, String status, Integer workerId) {
        return taskRepository.findAll(projectId, status, workerId);
    }

    public Task getTaskById(Integer id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ApiException("Task not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer createTask(Task t, Long operatorId, String operatorUsername) {
        Integer id = taskRepository.create(t);
        auditService.log(operatorId, operatorUsername, null, "TASK_CREATE", "TASK", "TASK", String.valueOf(id), null, t.getTitle(), "SUCCESS", "Created task " + t.getTitle());
        return id;
    }

    @Transactional
    public void updateTask(Integer id, Task t, Long operatorId, String operatorUsername) {
        Task existing = getTaskById(id);
        t.setId(id);
        taskRepository.update(t);
        auditService.log(operatorId, operatorUsername, null, "TASK_UPDATE", "TASK", "TASK", String.valueOf(id), existing.getStatus(), t.getStatus(), "SUCCESS", "Updated task details for " + t.getTitle());
    }

    @Transactional
    public void updateStatus(Integer id, String status, Integer progressPercent, Long operatorId, String operatorUsername) {
        Task existing = getTaskById(id);
        int finalProgress = progressPercent != null ? progressPercent : existing.getCompletionPercent();
        if ("Completed".equalsIgnoreCase(status)) {
            finalProgress = 100;
        }
        taskRepository.updateStatus(id, status, finalProgress);
        auditService.log(operatorId, operatorUsername, null, "TASK_STATUS_UPDATE", "TASK", "TASK", String.valueOf(id), existing.getStatus(), status, "SUCCESS", "Updated task #" + id + " to " + status + " (" + finalProgress + "%)");
    }

    @Transactional
    public void assignWorker(Integer id, Integer workerId, Long operatorId, String operatorUsername) {
        Task existing = getTaskById(id);
        taskRepository.assignWorker(id, workerId);
        auditService.log(operatorId, operatorUsername, null, "TASK_ASSIGN", "TASK", "TASK", String.valueOf(id), String.valueOf(existing.getAssignedWorkerId()), String.valueOf(workerId), "SUCCESS", "Assigned task #" + id + " to worker #" + workerId);
    }
}
