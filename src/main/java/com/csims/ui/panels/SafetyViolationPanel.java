package com.csims.ui.panels;

import com.csims.model.SafetyViolation;
import com.csims.security.SessionContext;
import com.csims.service.SafetyService;
import com.csims.service.impl.SafetyServiceImpl;
import com.csims.ui.common.BasePanel;
import com.csims.ui.common.BaseTableModel;
import com.csims.ui.dialogs.ViolationFormDialog;
import com.csims.ui.dialogs.ViolationResolveDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class SafetyViolationPanel extends BasePanel {

    private final JComboBox<String> cmbSeverityFilter = new JComboBox<>(new String[]{"ALL", "Low", "Medium", "High", "Critical"});
    private final JComboBox<String> cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"});
    private final JButton btnResolve = new JButton("Update / Resolve");

    private final ViolationTableModel tableModel = new ViolationTableModel();
    private final SafetyService safetyService = new SafetyServiceImpl();

    private static class ViolationTableModel extends BaseTableModel<SafetyViolation> {
        public ViolationTableModel() {
            super(new String[]{"ID", "Project", "Severity", "Description", "Worker Involved", "Status", "Resolution Date", "Corrective Action", "Reported By"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SafetyViolation sv = getItemAt(rowIndex);
            if (sv == null) return null;
            return switch (columnIndex) {
                case 0 -> sv.getViolationId();
                case 1 -> sv.getProjectName() != null ? sv.getProjectName() : "Project #" + sv.getProjectId();
                case 2 -> sv.getSeverity();
                case 3 -> sv.getDescription();
                case 4 -> sv.getWorkerName() != null ? sv.getWorkerName() : "General";
                case 5 -> sv.getStatus();
                case 6 -> DateUtil.formatDisplayDate(sv.getResolutionDate());
                case 7 -> sv.getCorrectiveAction() != null ? sv.getCorrectiveAction() : "";
                case 8 -> sv.getReporterName() != null ? sv.getReporterName() : "Officer";
                default -> "";
            };
        }
    }

    public SafetyViolationPanel() {
        table.setModel(tableModel);

        topFilterPanel.add(new JLabel("Severity:"));
        topFilterPanel.add(cmbSeverityFilter);
        topFilterPanel.add(new JLabel("Status:"));
        topFilterPanel.add(cmbStatusFilter);

        btnAdd.setText("Log Violation");
        bottomButtonPanel.add(btnResolve);

        cmbSeverityFilter.addActionListener(e -> onSearch());
        cmbStatusFilter.addActionListener(e -> onSearch());
        btnResolve.addActionListener(this::onResolve);

        // RBAC Permissions
        SessionContext session = SessionContext.getInstance();
        btnAdd.setEnabled(session.hasPermission("SAFETY_VIOLATION_CREATE"));
        btnEdit.setEnabled(session.hasPermission("SAFETY_VIOLATION_UPDATE"));
        btnDelete.setEnabled(session.hasPermission("SAFETY_VIOLATION_UPDATE"));
        btnResolve.setEnabled(session.hasPermission("SAFETY_VIOLATION_UPDATE"));

        loadData();
    }

    @Override
    public void loadData() {
        String sev = (String) cmbSeverityFilter.getSelectedItem();
        String status = (String) cmbStatusFilter.getSelectedItem();
        try {
            List<SafetyViolation> list = safetyService.searchViolations(null, null, sev, status);
            tableModel.setItems(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading violations: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onSearch() {
        loadData();
    }

    @Override
    protected void onResetFilter() {
        txtSearch.setText("");
        cmbSeverityFilter.setSelectedIndex(0);
        cmbStatusFilter.setSelectedIndex(0);
        loadData();
    }

    @Override
    protected void onAdd() {
        ViolationFormDialog dlg = new ViolationFormDialog(SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onEdit() {
        onResolve(null);
    }

    @Override
    protected void onDelete() {}

    private void onResolve(ActionEvent e) {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        SafetyViolation sv = tableModel.getItemAt(idx);

        ViolationResolveDialog dlg = new ViolationResolveDialog(SwingUtilities.getWindowAncestor(this), sv);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            JOptionPane.showMessageDialog(this, "Violation status updated successfully!", "Updated", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        }
    }
}
