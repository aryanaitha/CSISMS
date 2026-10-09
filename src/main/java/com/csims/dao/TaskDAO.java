package com.csims.dao;

import com.csims.model.TaskItem;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TaskDAO {
    Optional<TaskItem> findById(int taskId);
    List<TaskItem> findAll();
    List<TaskItem> search(String keyword, Integer projectId, Integer workerId, String status, String priority);
    List<TaskItem> findOverdueTasks();
    int create(TaskItem task);
    boolean update(TaskItem task);
    boolean updateStatusAndProgress(int taskId, String status, int completionPercent);
    boolean assignWorker(int taskId, Integer workerId);
    boolean delete(int taskId);
    Map<String, Integer> countByStatus();
    int countTotal();
}
