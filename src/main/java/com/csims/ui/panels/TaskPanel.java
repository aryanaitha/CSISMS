package com.csims.ui.panels;

import com.csims.model.Project;
import com.csims.model.TaskItem;
import com.csims.security.SessionContext;
import com.csims.service.ProjectService;
import com.csims.service.TaskService;
import com.csims.service.impl.ProjectServiceImpl;
import com.csims.service.impl.TaskServiceImpl;
import com.csims.ui.common.BasePanel;
import com.csims.ui.common.BaseTableModel;
import com.csims.ui.dialogs.TaskFormDialog;
import com.csims.ui.dialogs.TaskStatusDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class TaskPanel extends BasePanel {

    private final JComboBox<ProjectFilterItem> cmbProjectFilter = new JComboBox<>();
    private final JComboBox<String> cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "Created", "Assigned", "In Progress", "Completed"});
    private final JComboBox<String> cmbPriorityFilter = new JComboBox<>(new String[]{"ALL", "LOW", "MEDIUM", "HIGH", "CRITICAL"});

    private final JButton btnUpdateStatus = new JButton("Update Workflow");

    private final TaskTableModel tableModel = new TaskTableModel();
    private final TaskService taskService = new TaskServiceImpl();
    private final ProjectService projectService = new ProjectServiceImpl();

    private static class ProjectFilterItem {
        final Integer id;
        final String name;

        ProjectFilterItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class TaskTableModel extends BaseTableModel<TaskItem> {
        public TaskTableModel() {
            super(new String[]{"ID", "Title", "Project", "Assignee", "Priority", "Status", "Progress", "Due Date", "Overdue"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            TaskItem t = getItemAt(rowIndex);
            if (t == null) return null;
            return switch (columnIndex) {
                case 0 -> t.getTaskId();
                case 1 -> t.getTitle();
                case 2 -> t.getProjectName() != null ? t.getProjectName() : "Project #" + t.getProjectId();
                case 3 -> t.getWorkerName() != null ? t.getWorkerName() : "Unassigned";
                case 4 -> t.getPriority();
                case 5 -> t.getStatus();
                case 6 -> t.getCompletionPercent() + "%";
                case 7 -> DateUtil.formatDisplayDate(t.getDueDate());
                case 8 -> t.isOverdue() ? "YES" : "No";
                default -> "";
            };
        }
    }

    public TaskPanel() {
        table.setModel(tableModel);

        topFilterPanel.add(new JLabel("Project:"));
        loadProjectFilter();
        topFilterPanel.add(cmbProjectFilter);

        topFilterPanel.add(new JLabel("Status:"));
        topFilterPanel.add(cmbStatusFilter);

        topFilterPanel.add(new JLabel("Priority:"));
        topFilterPanel.add(cmbPriorityFilter);

        bottomButtonPanel.add(btnUpdateStatus);

        cmbProjectFilter.addActionListener(e -> onSearch());
        cmbStatusFilter.addActionListener(e -> onSearch());
        cmbPriorityFilter.addActionListener(e -> onSearch());
        btnUpdateStatus.addActionListener(this::onUpdateStatus);

        // RBAC Permissions
        SessionContext session = SessionContext.getInstance();
        btnAdd.setEnabled(session.hasPermission("TASK_CREATE"));
        btnEdit.setEnabled(session.hasPermission("TASK_UPDATE"));
        btnDelete.setEnabled(session.hasPermission("TASK_CREATE"));
        btnUpdateStatus.setEnabled(session.hasPermission("TASK_UPDATE"));

        loadData();
    }

    private void loadProjectFilter() {
        cmbProjectFilter.removeAllItems();
        cmbProjectFilter.addItem(new ProjectFilterItem(null, "All Projects"));
        try {
            List<Project> list = projectService.getAllProjects();
            for (Project p : list) {
                cmbProjectFilter.addItem(new ProjectFilterItem(p.getProjectId(), p.getProjectName()));
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void loadData() {
        ProjectFilterItem pItem = (ProjectFilterItem) cmbProjectFilter.getSelectedItem();
        Integer pId = pItem != null ? pItem.id : null;
        String status = (String) cmbStatusFilter.getSelectedItem();
        String priority = (String) cmbPriorityFilter.getSelectedItem();
        String kw = txtSearch.getText().trim();

        try {
            List<TaskItem> list = taskService.searchTasks(kw.isEmpty() ? null : kw, pId, null, status, priority);
            tableModel.setItems(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onSearch() {
        loadData();
    }

    @Override
    protected void onResetFilter() {
        txtSearch.setText("");
        if (cmbProjectFilter.getItemCount() > 0) cmbProjectFilter.setSelectedIndex(0);
        cmbStatusFilter.setSelectedIndex(0);
        cmbPriorityFilter.setSelectedIndex(0);
        loadData();
    }

    @Override
    protected void onAdd() {
        TaskFormDialog dlg = new TaskFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onEdit() {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        TaskItem t = tableModel.getItemAt(idx);
        TaskFormDialog dlg = new TaskFormDialog(SwingUtilities.getWindowAncestor(this), t);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onDelete() {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        TaskItem t = tableModel.getItemAt(idx);

        int ans = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete task '" + t.getTitle() + "'?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                taskService.deleteTask(t.getTaskId());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onUpdateStatus(ActionEvent e) {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        TaskItem t = tableModel.getItemAt(idx);
        TaskStatusDialog dlg = new TaskStatusDialog(SwingUtilities.getWindowAncestor(this), t);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }
}
