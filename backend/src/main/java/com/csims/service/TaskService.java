package com.csims.service;

import com.csims.model.Task;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final ProjectService projectService;

    public TaskService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public List<Task> getAllTasks(Integer projectId, String status, Integer workerId) {
        return projectService.getAllTasks(projectId, status, workerId);
    }

    public Task getTaskById(Integer id) {
        return projectService.getTaskById(id);
    }

    public Integer createTask(Task t, Long operatorId, String operatorUsername) {
        return projectService.createTask(t, operatorId, operatorUsername);
    }

    public void updateTask(Integer id, Task t, Long operatorId, String operatorUsername) {
        projectService.updateTask(id, t, operatorId, operatorUsername);
    }

    public void updateStatus(Integer id, String status, Integer progressPercent, Long operatorId, String operatorUsername) {
        projectService.updateTaskStatus(id, status, progressPercent, operatorId, operatorUsername);
    }

    public void assignWorker(Integer id, Integer workerId, Long operatorId, String operatorUsername) {
        projectService.assignTaskWorker(id, workerId, operatorId, operatorUsername);
    }
}
