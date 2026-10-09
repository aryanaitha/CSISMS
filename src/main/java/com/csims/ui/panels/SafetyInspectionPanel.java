package com.csims.ui.panels;

import com.csims.model.SafetyInspection;
import com.csims.security.SessionContext;
import com.csims.service.SafetyService;
import com.csims.service.impl.SafetyServiceImpl;
import com.csims.ui.common.BasePanel;
import com.csims.ui.common.BaseTableModel;
import com.csims.ui.dialogs.InspectionFormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.util.List;

public class SafetyInspectionPanel extends BasePanel {

    private final JLabel lblAvgScore = new JLabel("Site Safety Avg: --%");
    private final InspectionTableModel tableModel = new InspectionTableModel();
    private final SafetyService safetyService = new SafetyServiceImpl();

    private static class InspectionTableModel extends BaseTableModel<SafetyInspection> {
        public InspectionTableModel() {
            super(new String[]{"ID", "Project", "Inspection Date", "Overall Score", "PPE", "Worker", "Equipment", "Work Area", "Emergency", "Inspector"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SafetyInspection si = getItemAt(rowIndex);
            if (si == null) return null;
            return switch (columnIndex) {
                case 0 -> si.getInspectionId();
                case 1 -> si.getProjectName() != null ? si.getProjectName() : "Project #" + si.getProjectId();
                case 2 -> DateUtil.formatDisplayDate(si.getInspectionDate());
                case 3 -> si.getOverallScore() + "%";
                case 4 -> si.getPpeScore();
                case 5 -> si.getWorkerSafetyScore();
                case 6 -> si.getEquipmentScore();
                case 7 -> si.getWorkAreaScore();
                case 8 -> si.getEmergencyScore();
                case 9 -> si.getInspectorName() != null ? si.getInspectorName() : "User #" + si.getInspectorId();
                default -> "";
            };
        }
    }

    public SafetyInspectionPanel() {
        table.setModel(tableModel);

        topFilterPanel.add(Box.createHorizontalStrut(15));
        topFilterPanel.add(lblAvgScore);

        btnAdd.setText("Conduct Audit Inspection");
        btnEdit.setVisible(false);
        btnDelete.setVisible(false);

        // RBAC Permissions
        SessionContext session = SessionContext.getInstance();
        btnAdd.setEnabled(session.hasPermission("SAFETY_INSPECTION_CREATE"));

        loadData();
    }

    @Override
    public void loadData() {
        try {
            List<SafetyInspection> list = safetyService.getAllInspections();
            tableModel.setItems(list);

            double avg = safetyService.getAverageSafetyScore();
            lblAvgScore.setText(String.format("Safety Index: %.1f%%", avg));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading inspections: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onSearch() {
        loadData();
    }

    @Override
    protected void onResetFilter() {
        txtSearch.setText("");
        loadData();
    }

    @Override
    protected void onAdd() {
        InspectionFormDialog dlg = new InspectionFormDialog(SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onEdit() {}

    @Override
    protected void onDelete() {}
}
