package com.csims.ui.dialogs;

import com.csims.model.Equipment;
import com.csims.model.Project;
import com.csims.service.EquipmentService;
import com.csims.service.ProjectService;
import com.csims.service.impl.EquipmentServiceImpl;
import com.csims.service.impl.ProjectServiceImpl;
import com.csims.ui.common.FormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class EquipmentFormDialog extends FormDialog {

    private final JTextField txtCode = new JTextField(18);
    private final JTextField txtName = new JTextField(18);
    private final JTextField txtCategory = new JTextField(18);
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Available", "Assigned", "In Use", "Under Maintenance", "Out of Service"});
    private final JComboBox<ProjectItem> cmbProject = new JComboBox<>();
    private final JTextField txtPurchaseDate = new JTextField(12);
    private final JTextField txtLastService = new JTextField(12);
    private final JTextField txtNextService = new JTextField(12);

    private final Equipment equipmentToEdit;
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

    public EquipmentFormDialog(Window owner, Equipment equipmentToEdit) {
        super(owner, equipmentToEdit == null ? "Register Equipment" : "Edit Equipment Details", 460, 420);
        this.equipmentToEdit = equipmentToEdit;

        loadProjects();

        addFormField("Equipment Code:", txtCode);
        addFormField("Equipment Name:", txtName);
        addFormField("Category (e.g. Crane):", txtCategory);
        addFormField("Status:", cmbStatus);
        addFormField("Assigned Project:", cmbProject);
        addFormField("Purchase Date (YYYY-MM-DD):", txtPurchaseDate);
        addFormField("Last Service Date:", txtLastService);
        addFormField("Next Service Due:", txtNextService);

        if (equipmentToEdit != null) {
            txtCode.setText(equipmentToEdit.getEquipmentCode());
            txtCode.setEditable(false);
            txtName.setText(equipmentToEdit.getName());
            txtCategory.setText(equipmentToEdit.getCategory());
            cmbStatus.setSelectedItem(equipmentToEdit.getStatus());
            txtPurchaseDate.setText(DateUtil.formatDate(equipmentToEdit.getPurchaseDate()));
            txtLastService.setText(DateUtil.formatDate(equipmentToEdit.getLastServiceDate()));
            txtNextService.setText(DateUtil.formatDate(equipmentToEdit.getNextServiceDue()));

            if (equipmentToEdit.getProjectId() != null) {
                for (int i = 0; i < cmbProject.getItemCount(); i++) {
                    if (equipmentToEdit.getProjectId().equals(cmbProject.getItemAt(i).id)) {
                        cmbProject.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } else {
            txtPurchaseDate.setText(DateUtil.formatDate(LocalDate.now()));
            txtLastService.setText(DateUtil.formatDate(LocalDate.now()));
            txtNextService.setText(DateUtil.formatDate(LocalDate.now().plusMonths(3)));
        }
    }

    private void loadProjects() {
        cmbProject.addItem(new ProjectItem(null, "-- None (Unassigned) --"));
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
        if (txtCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Equipment code is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Equipment name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        Equipment e = equipmentToEdit != null ? equipmentToEdit : new Equipment();
        e.setEquipmentCode(txtCode.getText().trim());
        e.setName(txtName.getText().trim());
        e.setCategory(txtCategory.getText().trim());
        e.setStatus((String) cmbStatus.getSelectedItem());

        ProjectItem pi = (ProjectItem) cmbProject.getSelectedItem();
        e.setProjectId(pi != null ? pi.id : null);

        e.setPurchaseDate(DateUtil.parseDate(txtPurchaseDate.getText().trim()));
        e.setLastServiceDate(DateUtil.parseDate(txtLastService.getText().trim()));
        e.setNextServiceDue(DateUtil.parseDate(txtNextService.getText().trim()));

        if (equipmentToEdit == null) {
            equipmentService.createEquipment(e);
        } else {
            equipmentService.updateEquipment(e);
        }
    }
}
