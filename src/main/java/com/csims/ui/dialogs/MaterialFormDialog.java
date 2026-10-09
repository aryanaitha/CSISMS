package com.csims.ui.dialogs;

import com.csims.model.Material;
import com.csims.service.InventoryService;
import com.csims.service.impl.InventoryServiceImpl;
import com.csims.ui.common.FormDialog;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class MaterialFormDialog extends FormDialog {

    private final JTextField txtCode = new JTextField(18);
    private final JTextField txtName = new JTextField(18);
    private final JTextField txtCategory = new JTextField(18);
    private final JTextField txtUnit = new JTextField(18);
    private final JTextField txtQtyOnHand = new JTextField(12);
    private final JTextField txtMinStock = new JTextField(12);
    private final JTextField txtMaxStock = new JTextField(12);
    private final JTextField txtUnitPrice = new JTextField(12);
    private final JTextField txtLocation = new JTextField(18);

    private final Material materialToEdit;
    private final InventoryService inventoryService = new InventoryServiceImpl();

    public MaterialFormDialog(Window owner, Material materialToEdit) {
        super(owner, materialToEdit == null ? "Add Material to Catalog" : "Edit Material Details", 460, 420);
        this.materialToEdit = materialToEdit;

        addFormField("Material Code:", txtCode);
        addFormField("Material Name:", txtName);
        addFormField("Category:", txtCategory);
        addFormField("Unit (e.g. Bags, Tonnes):", txtUnit);
        addFormField("Initial Stock:", txtQtyOnHand);
        addFormField("Minimum Stock Limit:", txtMinStock);
        addFormField("Maximum Stock Limit:", txtMaxStock);
        addFormField("Unit Price ($):", txtUnitPrice);
        addFormField("Storage Location:", txtLocation);

        if (materialToEdit != null) {
            txtCode.setText(materialToEdit.getMaterialCode());
            txtCode.setEditable(false);
            txtName.setText(materialToEdit.getName());
            txtCategory.setText(materialToEdit.getCategory());
            txtUnit.setText(materialToEdit.getUnit());
            txtQtyOnHand.setText(materialToEdit.getQuantityOnHand().toString());
            txtMinStock.setText(materialToEdit.getMinStock().toString());
            txtMaxStock.setText(materialToEdit.getMaxStock().toString());
            txtUnitPrice.setText(materialToEdit.getUnitPrice().toString());
            txtLocation.setText(materialToEdit.getLocation());
        } else {
            txtQtyOnHand.setText("0.00");
            txtMinStock.setText("10.00");
            txtMaxStock.setText("500.00");
            txtUnitPrice.setText("0.00");
        }
    }

    @Override
    protected boolean validateForm() {
        if (txtCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Material code is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Material name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtCategory.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtUnit.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Unit is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        Material m = materialToEdit != null ? materialToEdit : new Material();
        m.setMaterialCode(txtCode.getText().trim());
        m.setName(txtName.getText().trim());
        m.setCategory(txtCategory.getText().trim());
        m.setUnit(txtUnit.getText().trim());
        try { m.setQuantityOnHand(new BigDecimal(txtQtyOnHand.getText().trim())); } catch (Exception e) { m.setQuantityOnHand(BigDecimal.ZERO); }
        try { m.setMinStock(new BigDecimal(txtMinStock.getText().trim())); } catch (Exception e) { m.setMinStock(BigDecimal.ZERO); }
        try { m.setMaxStock(new BigDecimal(txtMaxStock.getText().trim())); } catch (Exception e) { m.setMaxStock(BigDecimal.ZERO); }
        try { m.setUnitPrice(new BigDecimal(txtUnitPrice.getText().trim())); } catch (Exception e) { m.setUnitPrice(BigDecimal.ZERO); }
        m.setLocation(txtLocation.getText().trim());

        if (materialToEdit == null) {
            inventoryService.createMaterial(m);
        } else {
            inventoryService.updateMaterial(m);
        }
    }
}
