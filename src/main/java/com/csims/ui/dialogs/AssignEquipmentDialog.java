package com.csims.ui.dialogs;

import com.csims.model.Equipment;
import com.csims.model.Project;
import com.csims.service.EquipmentService;
import com.csims.service.ProjectService;
import com.csims.service.impl.EquipmentServiceImpl;
import com.csims.service.impl.ProjectServiceImpl;
import com.csims.ui.common.FormDialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AssignEquipmentDialog extends FormDialog {

    private final Equipment equipment;
    private final JComboBox<ProjectItem> cmbProject = new JComboBox<>();
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Assigned", "In Use", "Available"});

    private final EquipmentService equipmentService = new EquipmentServiceImpl();
    private final ProjectService projectService = new ProjectServiceImpl();

    private static class ProjectItem {
        final Integer id;
        final String name;

        ProjectItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public AssignEquipmentDialog(Window owner, Equipment equipment) {
        super(owner, "Assign Equipment to Site", 420, 240);
        this.equipment = equipment;

        loadProjects();

        JTextField txtName = new JTextField(equipment.getName() + " (" + equipment.getEquipmentCode() + ")");
        txtName.setEditable(false);
        addFormField("Equipment:", txtName);

        addFormField("Assign to Project:", cmbProject);
        addFormField("Operating Status:", cmbStatus);

        if (equipment.getProjectId() != null) {
            for (int i = 0; i < cmbProject.getItemCount(); i++) {
                if (equipment.getProjectId().equals(cmbProject.getItemAt(i).id)) {
                    cmbProject.setSelectedIndex(i);
                    break;
                }
            }
        }
        cmbStatus.setSelectedItem(equipment.getStatus());
    }

    private void loadProjects() {
        cmbProject.addItem(new ProjectItem(null, "-- Unassign / Return to Yard --"));
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
        return true;
    }

    @Override
    protected void applyChanges() {
        ProjectItem pi = (ProjectItem) cmbProject.getSelectedItem();
        Integer pId = pi != null ? pi.id : null;
        String status = (String) cmbStatus.getSelectedItem();
        equipmentService.assignToProject(equipment.getEquipmentId(), pId, status);
    }
}
