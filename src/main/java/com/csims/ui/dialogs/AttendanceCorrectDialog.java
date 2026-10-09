package com.csims.ui.dialogs;

import com.csims.model.Attendance;
import com.csims.service.AttendanceService;
import com.csims.service.impl.AttendanceServiceImpl;
import com.csims.ui.common.FormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;

public class AttendanceCorrectDialog extends FormDialog {

    private final Attendance attendance;
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Present", "Absent", "Leave"});
    private final JTextField txtRemarks = new JTextField(18);

    private final AttendanceService attendanceService = new AttendanceServiceImpl();

    public AttendanceCorrectDialog(Window owner, Attendance attendance) {
        super(owner, "Correct Attendance Record", 430, 280);
        this.attendance = attendance;

        JTextField txtWorker = new JTextField(attendance.getWorkerName() != null ? attendance.getWorkerName() : "Worker #" + attendance.getWorkerId());
        txtWorker.setEditable(false);
        addFormField("Worker:", txtWorker);

        JTextField txtDate = new JTextField(DateUtil.formatDisplayDate(attendance.getAttendanceDate()));
        txtDate.setEditable(false);
        addFormField("Date:", txtDate);

        JTextField txtCurrent = new JTextField(attendance.getStatus());
        txtCurrent.setEditable(false);
        addFormField("Current Status:", txtCurrent);

        cmbStatus.setSelectedItem(attendance.getStatus());
        addFormField("Corrected Status:", cmbStatus);

        txtRemarks.setText(attendance.getRemarks() != null ? attendance.getRemarks() : "Correction requested");
        addFormField("Correction Reason:", txtRemarks);
    }

    @Override
    protected boolean validateForm() {
        if (txtRemarks.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "A reason/remark is required for correcting attendance records.", "Audit Requirement", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        String newStatus = (String) cmbStatus.getSelectedItem();
        String newRemarks = txtRemarks.getText().trim();
        attendanceService.correctAttendance(attendance.getAttendanceId(), newStatus, newRemarks);
    }
}
