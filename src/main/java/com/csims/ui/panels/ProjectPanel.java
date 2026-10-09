package com.csims.ui.panels;

import com.csims.model.Project;
import com.csims.security.SessionContext;
import com.csims.service.ProjectService;
import com.csims.service.impl.ProjectServiceImpl;
import com.csims.ui.common.BasePanel;
import com.csims.ui.common.BaseTableModel;
import com.csims.ui.dialogs.ProjectFormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ProjectPanel extends BasePanel {

    private final JComboBox<String> cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "Planning", "Active", "On Hold", "Completed", "Archived"});
    private final JButton btnArchive = new JButton("Archive");

    private final ProjectTableModel tableModel = new ProjectTableModel();
    private final ProjectService projectService = new ProjectServiceImpl();

    private static class ProjectTableModel extends BaseTableModel<Project> {
        public ProjectTableModel() {
            super(new String[]{"ID", "Project Name", "Location", "Start Date", "End Date", "Budget ($)", "Status", "Progress"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Project p = getItemAt(rowIndex);
            if (p == null) return null;
            return switch (columnIndex) {
                case 0 -> p.getProjectId();
                case 1 -> p.getProjectName();
                case 2 -> p.getLocation();
                case 3 -> DateUtil.formatDisplayDate(p.getStartDate());
                case 4 -> DateUtil.formatDisplayDate(p.getEndDate());
                case 5 -> p.getBudget() != null ? String.format("%,.2f", p.getBudget()) : "$0.00";
                case 6 -> p.getStatus();
                case 7 -> p.getProgressPercent() + "%";
                default -> "";
            };
        }
    }

    public ProjectPanel() {
        table.setModel(tableModel);

        topFilterPanel.add(new JLabel("Status:"));
        topFilterPanel.add(cmbStatusFilter);

        bottomButtonPanel.add(btnArchive);

        cmbStatusFilter.addActionListener(e -> onSearch());
        btnArchive.addActionListener(this::onArchive);

        // RBAC Permissions check for button visibility/enabled
        SessionContext session = SessionContext.getInstance();
        btnAdd.setEnabled(session.hasPermission("PROJECT_CREATE"));
        btnEdit.setEnabled(session.hasPermission("PROJECT_UPDATE"));
        btnDelete.setEnabled(session.hasPermission("PROJECT_ARCHIVE"));
        btnArchive.setEnabled(session.hasPermission("PROJECT_ARCHIVE"));

        loadData();
    }

    @Override
    public void loadData() {
        String status = (String) cmbStatusFilter.getSelectedItem();
        String kw = txtSearch.getText().trim();
        try {
            List<Project> list = projectService.searchProjects(kw.isEmpty() ? null : kw, status);
            tableModel.setItems(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading projects: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onSearch() {
        loadData();
    }

    @Override
    protected void onResetFilter() {
        txtSearch.setText("");
        cmbStatusFilter.setSelectedIndex(0);
        loadData();
    }

    @Override
    protected void onAdd() {
        ProjectFormDialog dlg = new ProjectFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onEdit() {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Project p = tableModel.getItemAt(idx);
        ProjectFormDialog dlg = new ProjectFormDialog(SwingUtilities.getWindowAncestor(this), p);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onDelete() {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Project p = tableModel.getItemAt(idx);

        int ans = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete project '" + p.getProjectName() + "'?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                projectService.deleteProject(p.getProjectId());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onArchive(ActionEvent e) {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Project p = tableModel.getItemAt(idx);

        int ans = JOptionPane.showConfirmDialog(this,
            "Archive project '" + p.getProjectName() + "'?",
            "Confirm Archive", JOptionPane.YES_NO_OPTION);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                projectService.archiveProject(p.getProjectId());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
