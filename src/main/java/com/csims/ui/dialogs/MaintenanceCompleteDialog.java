package com.csims.ui.dialogs;

import com.csims.model.Maintenance;
import com.csims.service.MaintenanceService;
import com.csims.service.impl.MaintenanceServiceImpl;
import com.csims.ui.common.FormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MaintenanceCompleteDialog extends FormDialog {

    private final Maintenance maintenance;
    private final JTextField txtCompletedDate = new JTextField(12);
    private final JTextField txtCost = new JTextField(12);
    private final JTextField txtTechnician = new JTextField(18);
    private final JTextField txtNextServiceDue = new JTextField(12);
    private final JComboBox<String> cmbReturnStatus = new JComboBox<>(new String[]{"Available", "In Use", "Assigned"});

    private final MaintenanceService maintenanceService = new MaintenanceServiceImpl();

    public MaintenanceCompleteDialog(Window owner, Maintenance maintenance) {
        super(owner, "Complete Maintenance Service Order", 440, 320);
        this.maintenance = maintenance;

        JTextField txtEquip = new JTextField(maintenance.getEquipmentCode() + " - " + maintenance.getMaintenanceType());
        txtEquip.setEditable(false);
        addFormField("Order:", txtEquip);

        txtCompletedDate.setText(DateUtil.formatDate(LocalDate.now()));
        addFormField("Completion Date:", txtCompletedDate);

        txtCost.setText(maintenance.getCost() != null ? maintenance.getCost().toString() : "0.00");
        addFormField("Actual Cost ($):", txtCost);

        txtTechnician.setText(maintenance.getTechnician() != null ? maintenance.getTechnician() : "");
        addFormField("Technician:", txtTechnician);

        txtNextServiceDue.setText(DateUtil.formatDate(LocalDate.now().plusMonths(6)));
        addFormField("Next Service Due:", txtNextServiceDue);

        addFormField("Equipment Return Status:", cmbReturnStatus);
    }

    @Override
    protected boolean validateForm() {
        try {
            BigDecimal c = new BigDecimal(txtCost.getText().trim());
            if (c.compareTo(BigDecimal.ZERO) < 0) {
                JOptionPane.showMessageDialog(this, "Cost cannot be negative.", "Validation", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid cost value.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        LocalDate compDate = DateUtil.parseDate(txtCompletedDate.getText().trim());
        BigDecimal cost = new BigDecimal(txtCost.getText().trim());
        String tech = txtTechnician.getText().trim();
        LocalDate nextDue = DateUtil.parseDate(txtNextServiceDue.getText().trim());
        String retStatus = (String) cmbReturnStatus.getSelectedItem();

        maintenanceService.completeMaintenance(maintenance.getMaintenanceId(), compDate, cost, tech, nextDue, retStatus);
    }
}
