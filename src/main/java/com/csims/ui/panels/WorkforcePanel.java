package com.csims.ui.panels;

import com.csims.model.Contractor;
import com.csims.model.Worker;
import com.csims.security.SessionContext;
import com.csims.service.WorkforceService;
import com.csims.service.impl.WorkforceServiceImpl;
import com.csims.ui.common.BasePanel;
import com.csims.ui.common.BaseTableModel;
import com.csims.ui.dialogs.AssignWorkerDialog;
import com.csims.ui.dialogs.ContractorFormDialog;
import com.csims.ui.dialogs.WorkerFormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class WorkforcePanel extends BasePanel {

    private final JComboBox<ContractorFilterItem> cmbContractorFilter = new JComboBox<>();
    private final JComboBox<String> cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "ACTIVE", "INACTIVE", "ON_LEAVE"});

    private final JButton btnAssignProject = new JButton("Assign to Project");
    private final JButton btnAddContractor = new JButton("Add Subcontractor");

    private final WorkerTableModel tableModel = new WorkerTableModel();
    private final WorkforceService workforceService = new WorkforceServiceImpl();

    private static class ContractorFilterItem {
        final Integer id;
        final String name;

        ContractorFilterItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class WorkerTableModel extends BaseTableModel<Worker> {
        public WorkerTableModel() {
            super(new String[]{"ID", "Full Name", "Trade / Skill", "Phone", "Email", "Subcontractor", "Status", "Joined Date"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Worker w = getItemAt(rowIndex);
            if (w == null) return null;
            return switch (columnIndex) {
                case 0 -> w.getWorkerId();
                case 1 -> w.getFullName();
                case 2 -> w.getTrade();
                case 3 -> w.getPhone() != null ? w.getPhone() : "";
                case 4 -> w.getEmail() != null ? w.getEmail() : "";
                case 5 -> w.getContractorName() != null ? w.getContractorName() : "Direct Hire";
                case 6 -> w.getStatus();
                case 7 -> DateUtil.formatDisplayDate(w.getJoinedDate());
                default -> "";
            };
        }
    }

    public WorkforcePanel() {
        table.setModel(tableModel);

        topFilterPanel.add(new JLabel("Contractor:"));
        loadContractorFilter();
        topFilterPanel.add(cmbContractorFilter);

        topFilterPanel.add(new JLabel("Status:"));
        topFilterPanel.add(cmbStatusFilter);

        bottomButtonPanel.add(btnAssignProject);
        bottomButtonPanel.add(btnAddContractor);

        cmbContractorFilter.addActionListener(e -> onSearch());
        cmbStatusFilter.addActionListener(e -> onSearch());
        btnAssignProject.addActionListener(this::onAssignProject);
        btnAddContractor.addActionListener(this::onAddContractor);

        // RBAC Permissions
        SessionContext session = SessionContext.getInstance();
        btnAdd.setEnabled(session.hasPermission("WORKFORCE_CREATE"));
        btnEdit.setEnabled(session.hasPermission("WORKFORCE_UPDATE"));
        btnDelete.setEnabled(session.hasPermission("WORKFORCE_UPDATE"));
        btnAssignProject.setEnabled(session.hasPermission("WORKFORCE_ASSIGN"));
        btnAddContractor.setEnabled(session.hasPermission("WORKFORCE_CREATE"));

        loadData();
    }

    private void loadContractorFilter() {
        cmbContractorFilter.removeAllItems();
        cmbContractorFilter.addItem(new ContractorFilterItem(null, "All Contractors"));
        try {
            List<Contractor> list = workforceService.getAllContractors();
            for (Contractor c : list) {
                cmbContractorFilter.addItem(new ContractorFilterItem(c.getContractorId(), c.getCompanyName()));
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void loadData() {
        ContractorFilterItem cItem = (ContractorFilterItem) cmbContractorFilter.getSelectedItem();
        Integer cId = cItem != null ? cItem.id : null;
        String status = (String) cmbStatusFilter.getSelectedItem();
        String kw = txtSearch.getText().trim();

        try {
            List<Worker> list = workforceService.searchWorkers(kw.isEmpty() ? null : kw, cId, status);
            tableModel.setItems(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading workforce: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onSearch() {
        loadData();
    }

    @Override
    protected void onResetFilter() {
        txtSearch.setText("");
        if (cmbContractorFilter.getItemCount() > 0) cmbContractorFilter.setSelectedIndex(0);
        cmbStatusFilter.setSelectedIndex(0);
        loadData();
    }

    @Override
    protected void onAdd() {
        WorkerFormDialog dlg = new WorkerFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onEdit() {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Worker w = tableModel.getItemAt(idx);
        WorkerFormDialog dlg = new WorkerFormDialog(SwingUtilities.getWindowAncestor(this), w);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onDelete() {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Worker w = tableModel.getItemAt(idx);

        int ans = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to remove worker '" + w.getFullName() + "'?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                workforceService.deleteWorker(w.getWorkerId());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAssignProject(ActionEvent e) {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Worker w = tableModel.getItemAt(idx);
        AssignWorkerDialog dlg = new AssignWorkerDialog(SwingUtilities.getWindowAncestor(this), w);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            JOptionPane.showMessageDialog(this, "Worker assigned to project successfully!", "Assigned", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onAddContractor(ActionEvent e) {
        ContractorFormDialog dlg = new ContractorFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadContractorFilter();
        }
    }
}
