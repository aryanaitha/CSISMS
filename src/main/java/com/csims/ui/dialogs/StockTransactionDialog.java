package com.csims.ui.dialogs;

import com.csims.model.Material;
import com.csims.model.Project;
import com.csims.service.InventoryService;
import com.csims.service.ProjectService;
import com.csims.service.impl.InventoryServiceImpl;
import com.csims.service.impl.ProjectServiceImpl;
import com.csims.ui.common.FormDialog;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class StockTransactionDialog extends FormDialog {

    private final JComboBox<String> cmbType = new JComboBox<>(new String[]{"ISSUE", "RECEIPT"});
    private final JComboBox<MaterialItem> cmbMaterial = new JComboBox<>();
    private final JTextField txtQuantity = new JTextField(12);
    private final JComboBox<ProjectItem> cmbProject = new JComboBox<>();
    private final JTextField txtRefNo = new JTextField(18);
    private final JTextField txtRemarks = new JTextField(18);
    private final JLabel lblCurrentStock = new JLabel("Available Stock: --");

    private final InventoryService inventoryService = new InventoryServiceImpl();
    private final ProjectService projectService = new ProjectServiceImpl();

    private static class MaterialItem {
        final int id;
        final String label;
        final BigDecimal onHand;
        final String unit;

        MaterialItem(int id, String label, BigDecimal onHand, String unit) {
            this.id = id;
            this.label = label;
            this.onHand = onHand;
            this.unit = unit;
        }

        @Override
        public String toString() {
            return label;
        }
    }

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

    public StockTransactionDialog(Window owner, Material preSelected) {
        super(owner, "Record Material Movement", 460, 360);

        loadMaterials(preSelected);
        loadProjects();

        addFormField("Movement Type:", cmbType);
        addFormField("Material:", cmbMaterial);
        addFormField("Current Level:", lblCurrentStock);
        addFormField("Quantity:", txtQuantity);
        addFormField("Target Project:", cmbProject);
        addFormField("Reference / PO No:", txtRefNo);
        addFormField("Notes / Purpose:", txtRemarks);

        cmbMaterial.addActionListener(e -> updateStockDisplay());
        cmbType.addActionListener(e -> {
            boolean isIssue = "ISSUE".equals(cmbType.getSelectedItem());
            cmbProject.setEnabled(isIssue);
        });

        updateStockDisplay();
    }

    private void loadMaterials(Material preSelected) {
        try {
            List<Material> list = inventoryService.getAllMaterials();
            MaterialItem toSelect = null;
            for (Material m : list) {
                MaterialItem mi = new MaterialItem(m.getMaterialId(), m.getName() + " (" + m.getMaterialCode() + ")", m.getQuantityOnHand(), m.getUnit());
                cmbMaterial.addItem(mi);
                if (preSelected != null && preSelected.getMaterialId() == m.getMaterialId()) {
                    toSelect = mi;
                }
            }
            if (toSelect != null) {
                cmbMaterial.setSelectedItem(toSelect);
            }
        } catch (Exception ignored) {}
    }

    private void loadProjects() {
        cmbProject.addItem(new ProjectItem(null, "-- None (General Stock) --"));
        try {
            List<Project> list = projectService.getAllProjects();
            for (Project p : list) {
                if (!"Archived".equalsIgnoreCase(p.getStatus())) {
                    cmbProject.addItem(new ProjectItem(p.getProjectId(), p.getProjectName()));
                }
            }
        } catch (Exception ignored) {}
    }

    private void updateStockDisplay() {
        MaterialItem mi = (MaterialItem) cmbMaterial.getSelectedItem();
        if (mi != null) {
            lblCurrentStock.setText(mi.onHand + " " + mi.unit);
        }
    }

    @Override
    protected boolean validateForm() {
        if (cmbMaterial.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a material.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            BigDecimal qty = new BigDecimal(txtQuantity.getText().trim());
            if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.", "Validation", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric quantity.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        String type = (String) cmbType.getSelectedItem();
        MaterialItem mi = (MaterialItem) cmbMaterial.getSelectedItem();
        BigDecimal qty = new BigDecimal(txtQuantity.getText().trim());
        ProjectItem pi = (ProjectItem) cmbProject.getSelectedItem();
        Integer pId = pi != null ? pi.id : null;
        String ref = txtRefNo.getText().trim();
        String rem = txtRemarks.getText().trim();

        inventoryService.recordTransaction(mi.id, type, qty, pId, ref, rem);
    }
}
