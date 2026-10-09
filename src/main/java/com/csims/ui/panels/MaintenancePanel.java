package com.csims.ui.panels;

import com.csims.model.Maintenance;
import com.csims.security.SessionContext;
import com.csims.service.MaintenanceService;
import com.csims.service.impl.MaintenanceServiceImpl;
import com.csims.ui.common.BasePanel;
import com.csims.ui.common.BaseTableModel;
import com.csims.ui.dialogs.MaintenanceCompleteDialog;
import com.csims.ui.dialogs.MaintenanceScheduleDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class MaintenancePanel extends BasePanel {

    private final JComboBox<String> cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "Due", "Scheduled", "Serviced", "Completed"});
    private final JButton btnComplete = new JButton("Complete Service");

    private final MaintenanceTableModel tableModel = new MaintenanceTableModel();
    private final MaintenanceService maintenanceService = new MaintenanceServiceImpl();

    private static class MaintenanceTableModel extends BaseTableModel<Maintenance> {
        public MaintenanceTableModel() {
            super(new String[]{"ID", "Code", "Machine Name", "Service Type", "Status", "Scheduled Date", "Completed Date", "Cost ($)", "Technician"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Maintenance m = getItemAt(rowIndex);
            if (m == null) return null;
            return switch (columnIndex) {
                case 0 -> m.getMaintenanceId();
                case 1 -> m.getEquipmentCode() != null ? m.getEquipmentCode() : "EQ #" + m.getEquipmentId();
                case 2 -> m.getEquipmentName() != null ? m.getEquipmentName() : "";
                case 3 -> m.getMaintenanceType();
                case 4 -> m.getStatus();
                case 5 -> DateUtil.formatDisplayDate(m.getScheduledDate());
                case 6 -> DateUtil.formatDisplayDate(m.getCompletedDate());
                case 7 -> m.getCost() != null ? "$" + String.format("%.2f", m.getCost()) : "$0.00";
                case 8 -> m.getTechnician() != null ? m.getTechnician() : "";
                default -> "";
            };
        }
    }

    public MaintenancePanel() {
        table.setModel(tableModel);

        topFilterPanel.add(new JLabel("Status:"));
        topFilterPanel.add(cmbStatusFilter);

        btnAdd.setText("Schedule Maintenance");
        bottomButtonPanel.add(btnComplete);

        cmbStatusFilter.addActionListener(e -> onSearch());
        btnComplete.addActionListener(this::onComplete);

        // RBAC Permissions
        SessionContext session = SessionContext.getInstance();
        btnAdd.setEnabled(session.hasPermission("MAINTENANCE_SCHEDULE"));
        btnEdit.setEnabled(session.hasPermission("MAINTENANCE_UPDATE"));
        btnDelete.setEnabled(session.hasPermission("MAINTENANCE_UPDATE"));
        btnComplete.setEnabled(session.hasPermission("MAINTENANCE_UPDATE"));

        loadData();
    }

    @Override
    public void loadData() {
        String status = (String) cmbStatusFilter.getSelectedItem();
        String kw = txtSearch.getText().trim();
        try {
            List<Maintenance> list = maintenanceService.searchMaintenance(null, status, kw.isEmpty() ? null : kw);
            tableModel.setItems(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading maintenance: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onSearch() {
        loadData();
    }

    @Override
    protected void onResetFilter() {
        txtSearch.setText("");
        cmbStatusFilter.setSelectedIndex(0);
        loadData();
    }

    @Override
    protected void onAdd() {
        MaintenanceScheduleDialog dlg = new MaintenanceScheduleDialog(SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onEdit() {
        onComplete(null);
    }

    @Override
    protected void onDelete() {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Maintenance m = tableModel.getItemAt(idx);

        int ans = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel/delete maintenance order #" + m.getMaintenanceId() + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                maintenanceService.deleteMaintenance(m.getMaintenanceId());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onComplete(ActionEvent e) {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Maintenance m = tableModel.getItemAt(idx);

        if ("Completed".equalsIgnoreCase(m.getStatus())) {
            JOptionPane.showMessageDialog(this, "This maintenance order is already marked Completed.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        MaintenanceCompleteDialog dlg = new MaintenanceCompleteDialog(SwingUtilities.getWindowAncestor(this), m);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            JOptionPane.showMessageDialog(this, "Maintenance service completed successfully!", "Completed", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        }
    }
}
