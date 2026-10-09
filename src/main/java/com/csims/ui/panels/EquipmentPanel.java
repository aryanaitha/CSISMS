package com.csims.ui.panels;

import com.csims.model.Equipment;
import com.csims.security.SessionContext;
import com.csims.service.EquipmentService;
import com.csims.service.impl.EquipmentServiceImpl;
import com.csims.ui.common.BasePanel;
import com.csims.ui.common.BaseTableModel;
import com.csims.ui.dialogs.AssignEquipmentDialog;
import com.csims.ui.dialogs.EquipmentFormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class EquipmentPanel extends BasePanel {

    private final JComboBox<String> cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "Available", "Assigned", "In Use", "Under Maintenance", "Out of Service"});
    private final JButton btnAssign = new JButton("Assign to Project");
    private final JButton btnStatus = new JButton("Change Status");

    private final EquipmentTableModel tableModel = new EquipmentTableModel();
    private final EquipmentService equipmentService = new EquipmentServiceImpl();

    private static class EquipmentTableModel extends BaseTableModel<Equipment> {
        public EquipmentTableModel() {
            super(new String[]{"ID", "Code", "Machine Name", "Category", "Status", "Assigned Project", "Last Service", "Next Due", "Overdue?"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Equipment e = getItemAt(rowIndex);
            if (e == null) return null;
            return switch (columnIndex) {
                case 0 -> e.getEquipmentId();
                case 1 -> e.getEquipmentCode();
                case 2 -> e.getName();
                case 3 -> e.getCategory();
                case 4 -> e.getStatus();
                case 5 -> e.getProjectName() != null ? e.getProjectName() : "Yard (Unassigned)";
                case 6 -> DateUtil.formatDisplayDate(e.getLastServiceDate());
                case 7 -> DateUtil.formatDisplayDate(e.getNextServiceDue());
                case 8 -> e.isServiceOverdue() ? "⚠️ OVERDUE" : "OK";
                default -> "";
            };
        }
    }

    public EquipmentPanel() {
        table.setModel(tableModel);

        topFilterPanel.add(new JLabel("Status:"));
        topFilterPanel.add(cmbStatusFilter);

        bottomButtonPanel.add(btnAssign);
        bottomButtonPanel.add(btnStatus);

        cmbStatusFilter.addActionListener(e -> onSearch());
        btnAssign.addActionListener(this::onAssign);
        btnStatus.addActionListener(this::onChangeStatus);

        // RBAC Permissions
        SessionContext session = SessionContext.getInstance();
        btnAdd.setEnabled(session.hasPermission("EQUIPMENT_CREATE"));
        btnEdit.setEnabled(session.hasPermission("EQUIPMENT_UPDATE"));
        btnDelete.setEnabled(session.hasPermission("EQUIPMENT_UPDATE"));
        btnAssign.setEnabled(session.hasPermission("EQUIPMENT_UPDATE"));
        btnStatus.setEnabled(session.hasPermission("EQUIPMENT_UPDATE"));

        loadData();
    }

    @Override
    public void loadData() {
        String status = (String) cmbStatusFilter.getSelectedItem();
        String kw = txtSearch.getText().trim();
        try {
            List<Equipment> list = equipmentService.searchEquipment(kw.isEmpty() ? null : kw, null, status, null);
            tableModel.setItems(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading equipment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        EquipmentFormDialog dlg = new EquipmentFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onEdit() {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Equipment e = tableModel.getItemAt(idx);
        EquipmentFormDialog dlg = new EquipmentFormDialog(SwingUtilities.getWindowAncestor(this), e);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onDelete() {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Equipment e = tableModel.getItemAt(idx);

        int ans = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete equipment '" + e.getName() + "'?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                equipmentService.deleteEquipment(e.getEquipmentId());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAssign(ActionEvent e) {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Equipment eq = tableModel.getItemAt(idx);
        AssignEquipmentDialog dlg = new AssignEquipmentDialog(SwingUtilities.getWindowAncestor(this), eq);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    private void onChangeStatus(ActionEvent e) {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Equipment eq = tableModel.getItemAt(idx);

        String[] options = {"Available", "Assigned", "In Use", "Under Maintenance", "Out of Service"};
        String chosen = (String) JOptionPane.showInputDialog(this, "Select new status for " + eq.getName() + ":",
            "Update Status", JOptionPane.QUESTION_MESSAGE, null, options, eq.getStatus());

        if (chosen != null && !chosen.equals(eq.getStatus())) {
            try {
                equipmentService.updateStatus(eq.getEquipmentId(), chosen);
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
