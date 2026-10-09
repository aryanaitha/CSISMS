package com.csims.ui.dialogs;

import com.csims.model.Incident;
import com.csims.model.Project;
import com.csims.service.ProjectService;
import com.csims.service.SafetyService;
import com.csims.service.impl.ProjectServiceImpl;
import com.csims.service.impl.SafetyServiceImpl;
import com.csims.ui.common.FormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class IncidentReportDialog extends FormDialog {

    private final JComboBox<ProjectItem> cmbProject = new JComboBox<>();
    private final JComboBox<String> cmbSeverity = new JComboBox<>(new String[]{"Minor", "Moderate", "Major", "Critical"});
    private final JTextField txtTitle = new JTextField(20);
    private final JTextField txtDateTime = new JTextField(16);
    private final JTextArea txtDescription = new JTextArea(4, 20);

    private final SafetyService safetyService = new SafetyServiceImpl();
    private final ProjectService projectService = new ProjectServiceImpl();

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

    public IncidentReportDialog(Window owner) {
        super(owner, "Report Site Incident / Accident", 480, 380);

        loadProjects();

        addFormField("Project:", cmbProject);
        addFormField("Severity Level:", cmbSeverity);
        addFormField("Incident Title:", txtTitle);
        txtDateTime.setText(DateUtil.formatDateTime(LocalDateTime.now()));
        addFormField("Date & Time (YYYY-MM-DD HH:MM:SS):", txtDateTime);

        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        addFormField("Incident Description:", new JScrollPane(txtDescription));
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

    @Override
    protected boolean validateForm() {
        if (cmbProject.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a project.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtTitle.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Incident title is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtDescription.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Description is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        Incident inc = new Incident();
        ProjectItem pi = (ProjectItem) cmbProject.getSelectedItem();
        inc.setProjectId(pi.id);
        inc.setSeverity((String) cmbSeverity.getSelectedItem());
        inc.setTitle(txtTitle.getText().trim());
        inc.setDescription(txtDescription.getText().trim());

        try {
            inc.setIncidentDate(DateUtil.parseDateTime(txtDateTime.getText().trim()));
        } catch (Exception e) {
            inc.setIncidentDate(LocalDateTime.now());
        }

        safetyService.reportIncident(inc);
    }
}
