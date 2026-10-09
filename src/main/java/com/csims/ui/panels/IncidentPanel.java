package com.csims.ui.panels;

import com.csims.model.Incident;
import com.csims.security.SessionContext;
import com.csims.service.SafetyService;
import com.csims.service.impl.SafetyServiceImpl;
import com.csims.ui.common.BasePanel;
import com.csims.ui.common.BaseTableModel;
import com.csims.ui.dialogs.IncidentInvestigateDialog;
import com.csims.ui.dialogs.IncidentReportDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class IncidentPanel extends BasePanel {

    private final JComboBox<String> cmbSeverityFilter = new JComboBox<>(new String[]{"ALL", "Minor", "Moderate", "Major", "Critical"});
    private final JComboBox<String> cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "Reported", "Under Investigation", "Action Taken", "Resolved"});
    private final JButton btnInvestigate = new JButton("Investigate / Update");

    private final IncidentTableModel tableModel = new IncidentTableModel();
    private final SafetyService safetyService = new SafetyServiceImpl();

    private static class IncidentTableModel extends BaseTableModel<Incident> {
        public IncidentTableModel() {
            super(new String[]{"ID", "Project", "Date & Time", "Severity", "Title", "Investigator", "Status", "Actions Taken", "Reported By"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Incident inc = getItemAt(rowIndex);
            if (inc == null) return null;
            return switch (columnIndex) {
                case 0 -> inc.getIncidentId();
                case 1 -> inc.getProjectName() != null ? inc.getProjectName() : "Project #" + inc.getProjectId();
                case 2 -> DateUtil.formatDateTime(inc.getIncidentDate());
                case 3 -> inc.getSeverity();
                case 4 -> inc.getTitle();
                case 5 -> inc.getInvestigatorName() != null ? inc.getInvestigatorName() : "Unassigned";
                case 6 -> inc.getStatus();
                case 7 -> inc.getActionsTaken() != null ? inc.getActionsTaken() : "";
                case 8 -> inc.getReporterName() != null ? inc.getReporterName() : "User #" + inc.getReportedBy();
                default -> "";
            };
        }
    }

    public IncidentPanel() {
        table.setModel(tableModel);

        topFilterPanel.add(new JLabel("Severity:"));
        topFilterPanel.add(cmbSeverityFilter);
        topFilterPanel.add(new JLabel("Status:"));
        topFilterPanel.add(cmbStatusFilter);

        btnAdd.setText("Report Incident");
        btnEdit.setText("Investigate");
        btnDelete.setVisible(false); // Incident records are immutable/permanent for compliance
        bottomButtonPanel.add(btnInvestigate);

        cmbSeverityFilter.addActionListener(e -> onSearch());
        cmbStatusFilter.addActionListener(e -> onSearch());
        btnInvestigate.addActionListener(this::onInvestigate);

        // RBAC Permissions
        SessionContext session = SessionContext.getInstance();
        btnAdd.setEnabled(session.hasPermission("INCIDENT_CREATE"));
        btnEdit.setEnabled(session.hasPermission("INCIDENT_UPDATE"));
        btnInvestigate.setEnabled(session.hasPermission("INCIDENT_UPDATE"));

        loadData();
    }

    @Override
    public void loadData() {
        String sev = (String) cmbSeverityFilter.getSelectedItem();
        String status = (String) cmbStatusFilter.getSelectedItem();
        String searchTxt = txtSearch.getText().trim().toLowerCase();

        try {
            List<Incident> list = safetyService.searchIncidents(
                null,
                "ALL".equalsIgnoreCase(sev) ? null : sev,
                "ALL".equalsIgnoreCase(status) ? null : status,
                null,
                null
            );

            if (!searchTxt.isEmpty()) {
                list = list.stream().filter(inc ->
                    (inc.getTitle() != null && inc.getTitle().toLowerCase().contains(searchTxt)) ||
                    (inc.getDescription() != null && inc.getDescription().toLowerCase().contains(searchTxt)) ||
                    (inc.getProjectName() != null && inc.getProjectName().toLowerCase().contains(searchTxt))
                ).toList();
            }

            tableModel.setItems(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading incidents: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        IncidentReportDialog dlg = new IncidentReportDialog(SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onEdit() {
        onInvestigate(null);
    }

    @Override
    protected void onDelete() {
        // Not permitted for compliance records
    }

    private void onInvestigate(ActionEvent e) {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Incident inc = tableModel.getItemAt(idx);

        IncidentInvestigateDialog dlg = new IncidentInvestigateDialog(SwingUtilities.getWindowAncestor(this), inc);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            JOptionPane.showMessageDialog(this, "Incident status updated successfully!", "Updated", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        }
    }
}
