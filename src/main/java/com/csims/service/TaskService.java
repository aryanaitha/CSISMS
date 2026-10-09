package com.csims.service;

import com.csims.model.TaskItem;
import java.util.List;
import java.util.Map;

public interface TaskService {
    List<TaskItem> getAllTasks();
    List<TaskItem> searchTasks(String keyword, Integer projectId, Integer workerId, String status, String priority);
    TaskItem getTaskById(int taskId);
    List<TaskItem> getOverdueTasks();
    int createTask(TaskItem task);
    boolean updateTask(TaskItem task);
    boolean transitionStatus(int taskId, String targetStatus, int completionPercent);
    boolean assignWorker(int taskId, Integer workerId);
    boolean deleteTask(int taskId);
    Map<String, Integer> getTaskStats();
}
