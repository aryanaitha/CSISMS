package com.csims.ui.panels;

import com.csims.model.Attendance;
import com.csims.model.Project;
import com.csims.security.SessionContext;
import com.csims.service.AttendanceService;
import com.csims.service.ProjectService;
import com.csims.service.impl.AttendanceServiceImpl;
import com.csims.service.impl.ProjectServiceImpl;
import com.csims.ui.common.BasePanel;
import com.csims.ui.common.BaseTableModel;
import com.csims.ui.dialogs.AttendanceCorrectDialog;
import com.csims.ui.dialogs.AttendanceRecordDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.List;

public class AttendancePanel extends BasePanel {

    private final JComboBox<ProjectFilterItem> cmbProjectFilter = new JComboBox<>();
    private final JComboBox<String> cmbStatusFilter = new JComboBox<>(new String[]{"ALL", "Present", "Absent", "Leave"});
    private final JTextField txtDateFilter = new JTextField(10);
    private final JLabel lblStats = new JLabel("Attendance Rate: --%");

    private final JButton btnCorrect = new JButton("Correct Record");

    private final AttendanceTableModel tableModel = new AttendanceTableModel();
    private final AttendanceService attendanceService = new AttendanceServiceImpl();
    private final ProjectService projectService = new ProjectServiceImpl();

    private static class ProjectFilterItem {
        final Integer id;
        final String name;

        ProjectFilterItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class AttendanceTableModel extends BaseTableModel<Attendance> {
        public AttendanceTableModel() {
            super(new String[]{"ID", "Date", "Worker Name", "Trade", "Project", "Status", "Remarks / Shift", "Recorded By"});
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Attendance a = getItemAt(rowIndex);
            if (a == null) return null;
            return switch (columnIndex) {
                case 0 -> a.getAttendanceId();
                case 1 -> DateUtil.formatDisplayDate(a.getAttendanceDate());
                case 2 -> a.getWorkerName() != null ? a.getWorkerName() : "Worker #" + a.getWorkerId();
                case 3 -> a.getTrade() != null ? a.getTrade() : "";
                case 4 -> a.getProjectName() != null ? a.getProjectName() : "Project #" + a.getProjectId();
                case 5 -> a.getStatus();
                case 6 -> a.getRemarks() != null ? a.getRemarks() : "";
                case 7 -> a.getRecorderName() != null ? a.getRecorderName() : "System";
                default -> "";
            };
        }
    }

    public AttendancePanel() {
        table.setModel(tableModel);

        topFilterPanel.add(new JLabel("Project:"));
        loadProjectFilter();
        topFilterPanel.add(cmbProjectFilter);

        topFilterPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        topFilterPanel.add(txtDateFilter);

        topFilterPanel.add(new JLabel("Status:"));
        topFilterPanel.add(cmbStatusFilter);

        topFilterPanel.add(Box.createHorizontalStrut(15));
        topFilterPanel.add(lblStats);

        btnAdd.setText("Record Attendance");
        bottomButtonPanel.add(btnCorrect);

        cmbProjectFilter.addActionListener(e -> onSearch());
        cmbStatusFilter.addActionListener(e -> onSearch());
        txtDateFilter.addActionListener(e -> onSearch());
        btnCorrect.addActionListener(this::onCorrect);

        // RBAC Permissions
        SessionContext session = SessionContext.getInstance();
        btnAdd.setEnabled(session.hasPermission("ATTENDANCE_RECORD"));
        btnCorrect.setEnabled(session.hasPermission("ATTENDANCE_CORRECT"));
        btnEdit.setVisible(false); // BasePanel edit button hidden in favor of Correct Record
        btnDelete.setEnabled(session.hasPermission("ATTENDANCE_CORRECT"));

        loadData();
    }

    private void loadProjectFilter() {
        cmbProjectFilter.removeAllItems();
        cmbProjectFilter.addItem(new ProjectFilterItem(null, "All Projects"));
        try {
            List<Project> list = projectService.getAllProjects();
            for (Project p : list) {
                cmbProjectFilter.addItem(new ProjectFilterItem(p.getProjectId(), p.getProjectName()));
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void loadData() {
        ProjectFilterItem pItem = (ProjectFilterItem) cmbProjectFilter.getSelectedItem();
        Integer pId = pItem != null ? pItem.id : null;
        String status = (String) cmbStatusFilter.getSelectedItem();

        LocalDate d = null;
        String dateText = txtDateFilter.getText().trim();
        if (!dateText.isEmpty()) {
            try {
                d = DateUtil.parseDate(dateText);
            } catch (Exception ignored) {}
        }

        try {
            List<Attendance> list = attendanceService.searchAttendance(pId, null, d, d, status);
            tableModel.setItems(list);

            double rate = attendanceService.getAttendanceRate(pId, d, d);
            lblStats.setText(String.format("Attendance Rate: %.1f%%", rate));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading attendance: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void onSearch() {
        loadData();
    }

    @Override
    protected void onResetFilter() {
        txtSearch.setText("");
        txtDateFilter.setText("");
        if (cmbProjectFilter.getItemCount() > 0) cmbProjectFilter.setSelectedIndex(0);
        cmbStatusFilter.setSelectedIndex(0);
        loadData();
    }

    @Override
    protected void onAdd() {
        AttendanceRecordDialog dlg = new AttendanceRecordDialog(SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }

    @Override
    protected void onEdit() {
        onCorrect(null);
    }

    @Override
    protected void onDelete() {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Attendance a = tableModel.getItemAt(idx);

        int ans = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this attendance record?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            try {
                attendanceService.deleteAttendance(a.getAttendanceId());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onCorrect(ActionEvent e) {
        int idx = getSelectedRowIndex();
        if (idx == -1) return;
        Attendance a = tableModel.getItemAt(idx);

        AttendanceCorrectDialog dlg = new AttendanceCorrectDialog(SwingUtilities.getWindowAncestor(this), a);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            loadData();
        }
    }
}
