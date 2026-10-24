package com.csims.repository;

import com.csims.model.Task;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepository {
    private final ProjectRepository projectRepository;

    public TaskRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Task> findAll(Integer projectId, String status, Integer workerId) {
        return projectRepository.findAllTasks(projectId, status, workerId);
    }

    public Optional<Task> findById(Integer id) {
        return projectRepository.findTaskById(id);
    }

    public Integer create(Task t) {
        return projectRepository.createTask(t);
    }

    public void update(Task t) {
        projectRepository.updateTask(t);
    }

    public void updateStatus(Integer id, String status, Integer completionPercent) {
        projectRepository.updateTaskStatus(id, status, completionPercent);
    }

    public void assignWorker(Integer id, Integer workerId) {
        projectRepository.assignTaskWorker(id, workerId);
    }
}
