package com.csims.ui.dialogs;

import com.csims.model.Project;
import com.csims.model.Worker;
import com.csims.service.ProjectService;
import com.csims.service.WorkforceService;
import com.csims.service.impl.ProjectServiceImpl;
import com.csims.service.impl.WorkforceServiceImpl;
import com.csims.ui.common.FormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AssignWorkerDialog extends FormDialog {

    private final Worker worker;
    private final JComboBox<ProjectItem> cmbProject = new JComboBox<>();
    private final JTextField txtRoleInProject = new JTextField(18);
    private final JTextField txtAssignedDate = new JTextField(12);

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

    public AssignWorkerDialog(Window owner, Worker worker) {
        super(owner, "Assign Worker to Project", 440, 260);
        this.worker = worker;

        loadProjects();

        JTextField txtWorkerName = new JTextField(worker.getFullName() + " (" + worker.getTrade() + ")");
        txtWorkerName.setEditable(false);
        addFormField("Worker:", txtWorkerName);

        addFormField("Project:", cmbProject);
        txtRoleInProject.setText(worker.getTrade());
        addFormField("Role on Site:", txtRoleInProject);
        txtAssignedDate.setText(DateUtil.formatDate(LocalDate.now()));
        addFormField("Assigned Date (YYYY-MM-DD):", txtAssignedDate);
    }

    private void loadProjects() {
        try {
            List<Project> projects = projectService.getAllProjects();
            for (Project p : projects) {
                if (!"Archived".equalsIgnoreCase(p.getStatus())) {
                    cmbProject.addItem(new ProjectItem(p.getProjectId(), p.getProjectName()));
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
        if (txtRoleInProject.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Role on site is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        ProjectItem pi = (ProjectItem) cmbProject.getSelectedItem();
        LocalDate d = DateUtil.parseDate(txtAssignedDate.getText().trim());
        workforceService.assignWorkerToProject(pi.id, worker.getWorkerId(), txtRoleInProject.getText().trim(), d);
    }
}
