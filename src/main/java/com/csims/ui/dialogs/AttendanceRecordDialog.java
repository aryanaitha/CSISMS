package com.csims.ui.dialogs;

import com.csims.model.Attendance;
import com.csims.model.Project;
import com.csims.model.Worker;
import com.csims.service.AttendanceService;
import com.csims.service.ProjectService;
import com.csims.service.WorkforceService;
import com.csims.service.impl.AttendanceServiceImpl;
import com.csims.service.impl.ProjectServiceImpl;
import com.csims.service.impl.WorkforceServiceImpl;
import com.csims.ui.common.FormDialog;
import com.csims.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AttendanceRecordDialog extends FormDialog {

    private final JComboBox<ProjectItem> cmbProject = new JComboBox<>();
    private final JComboBox<WorkerItem> cmbWorker = new JComboBox<>();
    private final JTextField txtDate = new JTextField(12);
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Present", "Absent", "Leave"});
    private final JTextField txtRemarks = new JTextField(18);

    private final AttendanceService attendanceService = new AttendanceServiceImpl();
    private final ProjectService projectService = new ProjectServiceImpl();
    private final WorkforceService workforceService = new WorkforceServiceImpl();

    private static class ProjectItem {
        final int id;
        final String name;

        ProjectItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class WorkerItem {
        final int id;
        final String name;

        WorkerItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public AttendanceRecordDialog(Window owner) {
        super(owner, "Record Daily Attendance", 440, 320);

        loadProjects();
        loadWorkers();

        addFormField("Project:", cmbProject);
        addFormField("Worker:", cmbWorker);
        txtDate.setText(DateUtil.formatDate(LocalDate.now()));
        addFormField("Date (YYYY-MM-DD):", txtDate);
        addFormField("Attendance Status:", cmbStatus);
        addFormField("Remarks / Shift:", txtRemarks);
    }

    private void loadProjects() {
        try {
            List<Project> list = projectService.getAllProjects();
            for (Project p : list) {
                if (!"Archived".equalsIgnoreCase(p.getStatus())) {
                    cmbProject.addItem(new ProjectItem(p.getProjectId(), p.getProjectName()));
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadWorkers() {
        try {
            List<Worker> list = workforceService.getAllWorkers();
            for (Worker w : list) {
                if ("ACTIVE".equalsIgnoreCase(w.getStatus())) {
                    cmbWorker.addItem(new WorkerItem(w.getWorkerId(), w.getFullName() + " (" + w.getTrade() + ")"));
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected boolean validateForm() {
        if (cmbProject.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a project.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (cmbWorker.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a worker.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            LocalDate d = DateUtil.parseDate(txtDate.getText().trim());
            if (d == null) {
                JOptionPane.showMessageDialog(this, "Please enter a valid date (YYYY-MM-DD).", "Validation", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format.", "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    @Override
    protected void applyChanges() {
        Attendance a = new Attendance();
        ProjectItem pi = (ProjectItem) cmbProject.getSelectedItem();
        WorkerItem wi = (WorkerItem) cmbWorker.getSelectedItem();
        a.setProjectId(pi.id);
        a.setWorkerId(wi.id);
        a.setAttendanceDate(DateUtil.parseDate(txtDate.getText().trim()));
        a.setStatus((String) cmbStatus.getSelectedItem());
        a.setRemarks(txtRemarks.getText().trim());

        attendanceService.recordAttendance(a);
    }
}
