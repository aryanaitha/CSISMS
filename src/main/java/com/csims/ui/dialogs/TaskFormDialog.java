package com.csims.ui.dialogs;

import com.csims.model.Project;
import com.csims.model.TaskItem;
import com.csims.model.Worker;
import com.csims.service.ProjectService;
import com.csims.service.TaskService;
import com.csims.service.WorkforceService;
import com.csims.service.impl.ProjectServiceImpl;
import com.csims.service.impl.TaskServiceImpl;
import com.csims.service.impl.WorkforceServiceImpl;
import com.csims.ui.common.FormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class TaskFormDialog extends FormDialog {

    private final JComboBox<ProjectItem> cmbProject = new JComboBox<>();
    private final JTextField txtTitle = new JTextField(20);
    private final JComboBox<String> cmbPriority = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH", "CRITICAL"});
    private final JComboBox<WorkerItem> cmbWorker = new JComboBox<>();
    private final JTextField txtStartDate = new JTextField(12);
    private final JTextField txtDueDate = new JTextField(12);
    private final JTextArea txtDescription = new JTextArea(3, 20);

    private final TaskItem taskToEdit;
    private final TaskService taskService = new TaskServiceImpl();
    private final ProjectService projectService = new ProjectServiceImpl();
    private final WorkforceService workforceService = new WorkforceServiceImpl();

    private static class ProjectItem {
        final int id;
        final String name;

        ProjectItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class WorkerItem {
        final Integer id;
        final String name;

        WorkerItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public TaskFormDialog(Window owner, TaskItem taskToEdit) {
        super(owner, taskToEdit == null ? "Create New Task" : "Edit Task Details", 480, 420);
        this.taskToEdit = taskToEdit;

        loadProjects();
        loadWorkers();

        addFormField("Project:", cmbProject);
        addFormField("Task Title:", txtTitle);
        addFormField("Priority:", cmbPriority);
        addFormField("Assignee:", cmbWorker);
        addFormField("Start Date (YYYY-MM-DD):", txtStartDate);
        addFormField("Due Date (YYYY-MM-DD):", txtDueDate);

        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        addFormField("Description:", new JScrollPane(txtDescription));

        if (taskToEdit != null) {
            txtTitle.setText(taskToEdit.getTitle());
            cmbPriority.setSelectedItem(taskToEdit.getPriority());
            txtStartDate.setText(DateUtil.formatDate(taskToEdit.getStartDate()));
            txtDueDate.setText(DateUtil.formatDate(taskToEdit.getDueDate()));
            txtDescription.setText(taskToEdit.getDescription());

            for (int i = 0; i < cmbProject.getItemCount(); i++) {
                if (cmbProject.getItemAt(i).id == taskToEdit.getProjectId()) {
                    cmbProject.setSelectedIndex(i);
                    break;
                }
            }
            if (taskToEdit.getAssignedWorkerId() != null) {
                for (int i = 0; i < cmbWorker.getItemCount(); i++) {
                    if (taskToEdit.getAssignedWorkerId().equals(cmbWorker.getItemAt(i).id)) {
                        cmbWorker.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } else {
            txtStartDate.setText(DateUtil.formatDate(LocalDate.now()));
            txtDueDate.setText(DateUtil.formatDate(LocalDate.now().plusDays(7)));
            cmbPriority.setSelectedItem("MEDIUM");
        }
    }

    private void loadProjects() {
        try {
            List<Project> list = projectService.getAllProjects();
            for (Project p : list) {
                if (!"Archived".equalsIgnoreCase(p.getStatus())) {
                    cmbProject.addItem(new ProjectItem(p.getProjectId(), p.getProjectName()));
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadWorkers() {
        cmbWorker.addItem(new WorkerItem(null, "-- Unassigned --"));
        try {
            List<Worker> list = workforceService.getAllWorkers();
            for (Worker w : list) {
                if ("ACTIVE".equalsIgnoreCase(w.getStatus())) {
                    cmbWorker.addItem(new WorkerItem(w.getWorkerId(), w.getFullName() + " (" + w.getTrade() + ")"));
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected boolean validateForm() {
        if (cmbProject.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a project.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtTitle.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Task title is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        TaskItem t = taskToEdit != null ? taskToEdit : new TaskItem();
        ProjectItem pi = (ProjectItem) cmbProject.getSelectedItem();
        t.setProjectId(pi.id);
        t.setTitle(txtTitle.getText().trim());
        t.setPriority((String) cmbPriority.getSelectedItem());

        WorkerItem wi = (WorkerItem) cmbWorker.getSelectedItem();
        t.setAssignedWorkerId(wi != null ? wi.id : null);

        t.setStartDate(DateUtil.parseDate(txtStartDate.getText().trim()));
        t.setDueDate(DateUtil.parseDate(txtDueDate.getText().trim()));
        t.setDescription(txtDescription.getText().trim());

        if (taskToEdit == null) {
            taskService.createTask(t);
        } else {
            taskService.updateTask(t);
        }
    }
}
