package com.csims.ui.dialogs;

import com.csims.model.Project;
import com.csims.model.SafetyViolation;
import com.csims.model.Worker;
import com.csims.service.ProjectService;
import com.csims.service.SafetyService;
import com.csims.service.WorkforceService;
import com.csims.service.impl.ProjectServiceImpl;
import com.csims.service.impl.SafetyServiceImpl;
import com.csims.service.impl.WorkforceServiceImpl;
import com.csims.ui.common.FormDialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ViolationFormDialog extends FormDialog {

    private final JComboBox<ProjectItem> cmbProject = new JComboBox<>();
    private final JComboBox<WorkerItem> cmbWorker = new JComboBox<>();
    private final JComboBox<String> cmbSeverity = new JComboBox<>(new String[]{"Low", "Medium", "High", "Critical"});
    private final JTextArea txtDescription = new JTextArea(3, 18);
    private final JTextArea txtCorrectiveAction = new JTextArea(3, 18);

    private final SafetyService safetyService = new SafetyServiceImpl();
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

    public ViolationFormDialog(Window owner) {
        super(owner, "Log Safety Hazard / Violation", 460, 400);

        loadProjects();
        loadWorkers();

        addFormField("Project:", cmbProject);
        addFormField("Worker Involved (optional):", cmbWorker);
        addFormField("Severity Level:", cmbSeverity);

        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        addFormField("Violation Description:", new JScrollPane(txtDescription));

        txtCorrectiveAction.setLineWrap(true);
        txtCorrectiveAction.setWrapStyleWord(true);
        addFormField("Required Corrective Action:", new JScrollPane(txtCorrectiveAction));
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
        cmbWorker.addItem(new WorkerItem(null, "-- None / General Hazard --"));
        try {
            List<Worker> list = workforceService.getAllWorkers();
            for (Worker w : list) {
                if ("ACTIVE".equalsIgnoreCase(w.getStatus())) {
                    cmbWorker.addItem(new WorkerItem(w.getWorkerId(), w.getFullName()));
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
        if (txtDescription.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Violation description is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        SafetyViolation sv = new SafetyViolation();
        ProjectItem pi = (ProjectItem) cmbProject.getSelectedItem();
        WorkerItem wi = (WorkerItem) cmbWorker.getSelectedItem();

        sv.setProjectId(pi.id);
        sv.setWorkerId(wi != null ? wi.id : null);
        sv.setSeverity((String) cmbSeverity.getSelectedItem());
        sv.setDescription(txtDescription.getText().trim());
        sv.setCorrectiveAction(txtCorrectiveAction.getText().trim());
        sv.setStatus("OPEN");

        safetyService.createViolation(sv);
    }
}
