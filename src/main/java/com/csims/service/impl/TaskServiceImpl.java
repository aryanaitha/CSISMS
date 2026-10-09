package com.csims.service.impl;

import com.csims.dao.TaskDAO;
import com.csims.dao.impl.TaskDAOImpl;
import com.csims.model.TaskItem;
import com.csims.security.PermissionChecker;
import com.csims.service.AuditService;
import com.csims.service.NotificationService;
import com.csims.service.TaskService;
import com.csims.util.ValidationException;
import com.csims.util.Validator;

import java.util.List;
import java.util.Map;

public class TaskServiceImpl implements TaskService {

    private final TaskDAO taskDAO = new TaskDAOImpl();
    private final AuditService auditService = new AuditServiceImpl();
    private final NotificationService notificationService = new NotificationServiceImpl();

    @Override
    public List<TaskItem> getAllTasks() {
        PermissionChecker.require("TASK_VIEW");
        return taskDAO.findAll();
    }

    @Override
    public List<TaskItem> searchTasks(String keyword, Integer projectId, Integer workerId, String status, String priority) {
        PermissionChecker.require("TASK_VIEW");
        return taskDAO.search(keyword, projectId, workerId, status, priority);
    }

    @Override
    public TaskItem getTaskById(int taskId) {
        PermissionChecker.require("TASK_VIEW");
        return taskDAO.findById(taskId)
            .orElseThrow(() -> new ValidationException("Task #" + taskId + " not found."));
    }

    @Override
    public List<TaskItem> getOverdueTasks() {
        PermissionChecker.require("TASK_VIEW");
        return taskDAO.findOverdueTasks();
    }

    @Override
    public int createTask(TaskItem task) {
        PermissionChecker.require("TASK_CREATE");
        Validator.requireNonEmpty(task.getTitle(), "Task Title");
        if (task.getProjectId() <= 0) {
            throw new ValidationException("A valid project must be selected.");
        }
        if (task.getStartDate() != null && task.getDueDate() != null && task.getDueDate().isBefore(task.getStartDate())) {
            throw new ValidationException("Due date cannot be earlier than start date.");
        }

        // Auto determine status
        if (task.getAssignedWorkerId() != null && task.getAssignedWorkerId() > 0) {
            task.setStatus("Assigned");
        } else {
            task.setStatus("Created");
        }

        int id = taskDAO.create(task);
        auditService.logCurrent("TASK_CREATE", "TASK", "TASK", String.valueOf(id),
            null, task.getTitle(), "SUCCESS", "Created task: " + task.getTitle());

        if (task.getAssignedWorkerId() != null) {
            notificationService.notifyTaskAssignment(task.getAssignedWorkerId(), task.getTitle(), "Project #" + task.getProjectId());
        }
        return id;
    }

    @Override
    public boolean updateTask(TaskItem task) {
        PermissionChecker.require("TASK_UPDATE");
        Validator.requireNonEmpty(task.getTitle(), "Task Title");
        if (task.getStartDate() != null && task.getDueDate() != null && task.getDueDate().isBefore(task.getStartDate())) {
            throw new ValidationException("Due date cannot be earlier than start date.");
        }

        TaskItem old = getTaskById(task.getTaskId());
        boolean updated = taskDAO.update(task);
        if (updated) {
            auditService.logCurrent("TASK_UPDATE", "TASK", "TASK", String.valueOf(task.getTaskId()),
                old.getTitle() + " (" + old.getStatus() + ")", task.getTitle() + " (" + task.getStatus() + ")",
                "SUCCESS", "Updated task details.");
        }
        return updated;
    }

    @Override
    public boolean transitionStatus(int taskId, String targetStatus, int completionPercent) {
        PermissionChecker.require("TASK_UPDATE");
        Validator.validateRange(completionPercent, 0, 100, "Completion Percent");

        TaskItem task = getTaskById(taskId);
        String currentStatus = task.getStatus();

        // Enforce Workflow: Created -> Assigned -> In Progress -> Completed
        boolean valid = switch (currentStatus) {
            case "Created" -> targetStatus.equals("Assigned") || (targetStatus.equals("In Progress") && task.getAssignedWorkerId() != null);
            case "Assigned" -> targetStatus.equals("In Progress") || targetStatus.equals("Created");
            case "In Progress" -> targetStatus.equals("Completed") || targetStatus.equals("Assigned");
            case "Completed" -> targetStatus.equals("In Progress"); // reopening
            default -> false;
        };

        if (!valid) {
            throw new ValidationException("Invalid status transition: Cannot change from '" + currentStatus + "' to '" + targetStatus +
                                          "'. Workflow must follow Created -> Assigned -> In Progress -> Completed.");
        }

        if ("Completed".equals(targetStatus)) {
            completionPercent = 100;
        }

        boolean updated = taskDAO.updateStatusAndProgress(taskId, targetStatus, completionPercent);
        if (updated) {
            auditService.logCurrent("TASK_WORKFLOW_TRANSITION", "TASK", "TASK", String.valueOf(taskId),
                currentStatus + " (" + task.getCompletionPercent() + "%)",
                targetStatus + " (" + completionPercent + "%)",
                "SUCCESS", "Transitioned task workflow.");
        }
        return updated;
    }

    @Override
    public boolean assignWorker(int taskId, Integer workerId) {
        PermissionChecker.require("TASK_ASSIGN");
        TaskItem task = getTaskById(taskId);

        boolean updated = taskDAO.assignWorker(taskId, workerId);
        if (updated) {
            auditService.logCurrent("TASK_ASSIGN", "TASK", "TASK", String.valueOf(taskId),
                String.valueOf(task.getAssignedWorkerId()), String.valueOf(workerId),
                "SUCCESS", "Assigned worker #" + workerId + " to task #" + taskId);

            if (workerId != null) {
                notificationService.notifyTaskAssignment(workerId, task.getTitle(), "Project #" + task.getProjectId());
            }
        }
        return updated;
    }

    @Override
    public boolean deleteTask(int taskId) {
        PermissionChecker.require("TASK_CREATE");
        TaskItem t = getTaskById(taskId);
        boolean deleted = taskDAO.delete(taskId);
        if (deleted) {
            auditService.logCurrent("TASK_DELETE", "TASK", "TASK", String.valueOf(taskId),
                t.getTitle(), null, "SUCCESS", "Deleted task #" + taskId);
        }
        return deleted;
    }

    @Override
    public Map<String, Integer> getTaskStats() {
        return taskDAO.countByStatus();
    }
}
