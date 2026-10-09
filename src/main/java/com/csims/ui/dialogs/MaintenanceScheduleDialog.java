package com.csims.ui.dialogs;

import com.csims.model.Equipment;
import com.csims.model.Maintenance;
import com.csims.service.EquipmentService;
import com.csims.service.MaintenanceService;
import com.csims.service.impl.EquipmentServiceImpl;
import com.csims.service.impl.MaintenanceServiceImpl;
import com.csims.ui.common.FormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class MaintenanceScheduleDialog extends FormDialog {

    private final JComboBox<EquipmentItem> cmbEquipment = new JComboBox<>();
    private final JTextField txtType = new JTextField(18);
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Due", "Scheduled"});
    private final JTextField txtScheduledDate = new JTextField(12);
    private final JTextField txtTechnician = new JTextField(18);
    private final JTextArea txtNotes = new JTextArea(3, 18);

    private final EquipmentService equipmentService = new EquipmentServiceImpl();
    private final MaintenanceService maintenanceService = new MaintenanceServiceImpl();

    private static class EquipmentItem {
        final int id;
        final String name;

        EquipmentItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public MaintenanceScheduleDialog(Window owner) {
        super(owner, "Schedule Equipment Maintenance", 460, 380);

        loadEquipment();

        addFormField("Equipment:", cmbEquipment);
        addFormField("Maintenance Type:", txtType);
        addFormField("Initial Status:", cmbStatus);
        txtScheduledDate.setText(DateUtil.formatDate(LocalDate.now()));
        addFormField("Scheduled Date (YYYY-MM-DD):", txtScheduledDate);
        addFormField("Technician / Workshop:", txtTechnician);

        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        addFormField("Work Order Notes:", new JScrollPane(txtNotes));
    }

    private void loadEquipment() {
        try {
            List<Equipment> list = equipmentService.getAllEquipment();
            for (Equipment eq : list) {
                cmbEquipment.addItem(new EquipmentItem(eq.getEquipmentId(), eq.getName() + " (" + eq.getEquipmentCode() + ")"));
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected boolean validateForm() {
        if (cmbEquipment.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select equipment.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtType.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Maintenance type is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        Maintenance m = new Maintenance();
        EquipmentItem ei = (EquipmentItem) cmbEquipment.getSelectedItem();
        m.setEquipmentId(ei.id);
        m.setMaintenanceType(txtType.getText().trim());
        m.setStatus((String) cmbStatus.getSelectedItem());
        m.setScheduledDate(DateUtil.parseDate(txtScheduledDate.getText().trim()));
        m.setTechnician(txtTechnician.getText().trim());
        m.setNotes(txtNotes.getText().trim());

        maintenanceService.scheduleMaintenance(m);
    }
}
