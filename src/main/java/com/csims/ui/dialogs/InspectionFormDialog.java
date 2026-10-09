package com.csims.ui.dialogs;

import com.csims.model.Project;
import com.csims.model.SafetyInspection;
import com.csims.service.ProjectService;
import com.csims.service.SafetyService;
import com.csims.service.impl.ProjectServiceImpl;
import com.csims.service.impl.SafetyServiceImpl;
import com.csims.ui.common.FormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class InspectionFormDialog extends FormDialog {

    private final JComboBox<ProjectItem> cmbProject = new JComboBox<>();
    private final JTextField txtDate = new JTextField(12);

    private final JSpinner spnPpe = new JSpinner(new SpinnerNumberModel(90, 0, 100, 5));
    private final JSpinner spnWorker = new JSpinner(new SpinnerNumberModel(90, 0, 100, 5));
    private final JSpinner spnEquip = new JSpinner(new SpinnerNumberModel(90, 0, 100, 5));
    private final JSpinner spnWorkArea = new JSpinner(new SpinnerNumberModel(90, 0, 100, 5));
    private final JSpinner spnEmergency = new JSpinner(new SpinnerNumberModel(90, 0, 100, 5));
    private final JLabel lblOverallScore = new JLabel("90.00 %", SwingConstants.LEFT);

    private final JTextArea txtRemarks = new JTextArea(3, 18);

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

    public InspectionFormDialog(Window owner) {
        super(owner, "Conduct Site Safety Audit", 460, 440);

        loadProjects();

        addFormField("Project:", cmbProject);
        txtDate.setText(DateUtil.formatDate(LocalDate.now()));
        addFormField("Date (YYYY-MM-DD):", txtDate);

        addFormField("1. PPE Compliance (0-100):", spnPpe);
        addFormField("2. Worker Safe Practices (0-100):", spnWorker);
        addFormField("3. Equipment Condition (0-100):", spnEquip);
        addFormField("4. Work Area Housekeeping (0-100):", spnWorkArea);
        addFormField("5. Emergency Arrangements (0-100):", spnEmergency);

        lblOverallScore.setFont(lblOverallScore.getFont().deriveFont(Font.BOLD, 14f));
        addFormField("Calculated Safety Score:", lblOverallScore);

        txtRemarks.setLineWrap(true);
        txtRemarks.setWrapStyleWord(true);
        addFormField("Audit Notes & Observations:", new JScrollPane(txtRemarks));

        javax.swing.event.ChangeListener cl = e -> updateScore();
        spnPpe.addChangeListener(cl);
        spnWorker.addChangeListener(cl);
        spnEquip.addChangeListener(cl);
        spnWorkArea.addChangeListener(cl);
        spnEmergency.addChangeListener(cl);

        updateScore();
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

    private void updateScore() {
        int s1 = (Integer) spnPpe.getValue();
        int s2 = (Integer) spnWorker.getValue();
        int s3 = (Integer) spnEquip.getValue();
        int s4 = (Integer) spnWorkArea.getValue();
        int s5 = (Integer) spnEmergency.getValue();
        double avg = (s1 + s2 + s3 + s4 + s5) / 5.0;
        lblOverallScore.setText(String.format("%.2f %%", avg));
        if (avg >= 85) {
            lblOverallScore.setForeground(new Color(0, 130, 40));
        } else if (avg >= 70) {
            lblOverallScore.setForeground(new Color(180, 110, 0));
        } else {
            lblOverallScore.setForeground(new Color(180, 0, 0));
        }
    }

    @Override
    protected boolean validateForm() {
        if (cmbProject.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a project.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        SafetyInspection si = new SafetyInspection();
        ProjectItem pi = (ProjectItem) cmbProject.getSelectedItem();
        si.setProjectId(pi.id);
        si.setInspectionDate(DateUtil.parseDate(txtDate.getText().trim()));
        si.setPpeScore((Integer) spnPpe.getValue());
        si.setWorkerSafetyScore((Integer) spnWorker.getValue());
        si.setEquipmentScore((Integer) spnEquip.getValue());
        si.setWorkAreaScore((Integer) spnWorkArea.getValue());
        si.setEmergencyScore((Integer) spnEmergency.getValue());
        si.setRemarks(txtRemarks.getText().trim());

        safetyService.createInspection(si);
    }
}
