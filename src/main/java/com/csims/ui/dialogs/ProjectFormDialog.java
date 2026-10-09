package com.csims.ui.dialogs;

import com.csims.model.Project;
import com.csims.service.ProjectService;
import com.csims.service.impl.ProjectServiceImpl;
import com.csims.ui.common.FormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjectFormDialog extends FormDialog {

    private final JTextField txtName = new JTextField(20);
    private final JTextField txtLocation = new JTextField(20);
    private final JTextField txtStartDate = new JTextField(12);
    private final JTextField txtEndDate = new JTextField(12);
    private final JTextField txtBudget = new JTextField(12);
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Planning", "Active", "On Hold", "Completed", "Archived"});
    private final JSpinner spnProgress = new JSpinner(new SpinnerNumberModel(0, 0, 100, 5));
    private final JTextArea txtDescription = new JTextArea(3, 20);

    private final Project projectToEdit;
    private final ProjectService projectService = new ProjectServiceImpl();

    public ProjectFormDialog(Window owner, Project projectToEdit) {
        super(owner, projectToEdit == null ? "Create New Project" : "Edit Project Details", 480, 440);
        this.projectToEdit = projectToEdit;

        addFormField("Project Name:", txtName);
        addFormField("Location:", txtLocation);
        addFormField("Start Date (YYYY-MM-DD):", txtStartDate);
        addFormField("End Date (YYYY-MM-DD):", txtEndDate);
        addFormField("Budget ($):", txtBudget);
        addFormField("Status:", cmbStatus);
        addFormField("Progress (%):", spnProgress);

        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        addFormField("Description:", new JScrollPane(txtDescription));

        if (projectToEdit != null) {
            txtName.setText(projectToEdit.getProjectName());
            txtLocation.setText(projectToEdit.getLocation());
            txtStartDate.setText(DateUtil.formatDate(projectToEdit.getStartDate()));
            txtEndDate.setText(DateUtil.formatDate(projectToEdit.getEndDate()));
            txtBudget.setText(projectToEdit.getBudget() != null ? projectToEdit.getBudget().toString() : "0.00");
            cmbStatus.setSelectedItem(projectToEdit.getStatus());
            spnProgress.setValue(projectToEdit.getProgressPercent());
            txtDescription.setText(projectToEdit.getDescription());
        } else {
            txtStartDate.setText(DateUtil.formatDate(LocalDate.now()));
            txtBudget.setText("0.00");
        }
    }

    @Override
    protected boolean validateForm() {
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Project name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtLocation.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Location is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            LocalDate sd = DateUtil.parseDate(txtStartDate.getText());
            if (sd == null) {
                JOptionPane.showMessageDialog(this, "Please enter a valid start date (YYYY-MM-DD).", "Validation", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid start date format (use YYYY-MM-DD).", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        Project p = projectToEdit != null ? projectToEdit : new Project();
        p.setProjectName(txtName.getText().trim());
        p.setLocation(txtLocation.getText().trim());
        p.setStartDate(DateUtil.parseDate(txtStartDate.getText()));
        p.setEndDate(DateUtil.parseDate(txtEndDate.getText()));
        try {
            p.setBudget(new BigDecimal(txtBudget.getText().trim()));
        } catch (Exception e) {
            p.setBudget(BigDecimal.ZERO);
        }
        p.setStatus((String) cmbStatus.getSelectedItem());
        p.setProgressPercent((Integer) spnProgress.getValue());
        p.setDescription(txtDescription.getText().trim());

        if (projectToEdit == null) {
            projectService.createProject(p);
        } else {
            projectService.updateProject(p);
        }
    }
}
