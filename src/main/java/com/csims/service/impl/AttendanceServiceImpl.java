package com.csims.service.impl;

import com.csims.dao.AttendanceDAO;
import com.csims.dao.impl.AttendanceDAOImpl;
import com.csims.model.Attendance;
import com.csims.model.User;
import com.csims.security.PermissionChecker;
import com.csims.security.SessionContext;
import com.csims.service.AttendanceService;
import com.csims.service.AuditService;
import com.csims.util.ValidationException;

import java.time.LocalDate;
import java.util.List;

public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceDAO attendanceDAO = new AttendanceDAOImpl();
    private final AuditService auditService = new AuditServiceImpl();

    @Override
    public List<Attendance> searchAttendance(Integer projectId, Integer workerId, LocalDate startDate, LocalDate endDate, String status) {
        PermissionChecker.require("ATTENDANCE_VIEW");
        return attendanceDAO.search(projectId, workerId, startDate, endDate, status);
    }

    @Override
    public Attendance getAttendanceById(int attendanceId) {
        PermissionChecker.require("ATTENDANCE_VIEW");
        return attendanceDAO.findById(attendanceId)
            .orElseThrow(() -> new ValidationException("Attendance record #" + attendanceId + " not found."));
    }

    @Override
    public int recordAttendance(Attendance attendance) {
        PermissionChecker.require("ATTENDANCE_RECORD");
        if (attendance.getWorkerId() <= 0) {
            throw new ValidationException("Worker must be selected.");
        }
        if (attendance.getProjectId() <= 0) {
            throw new ValidationException("Project must be selected.");
        }
        if (attendance.getAttendanceDate() == null) {
            attendance.setAttendanceDate(LocalDate.now());
        }

        User recorder = SessionContext.getInstance().getCurrentUser();
        if (recorder != null) {
            attendance.setRecordedBy(recorder.getUserId());
        }

        int id = attendanceDAO.record(attendance);
        auditService.logCurrent("ATTENDANCE_RECORDED", "ATTENDANCE", "ATTENDANCE", String.valueOf(id),
            null, attendance.getStatus() + " on " + attendance.getAttendanceDate(),
            "SUCCESS", "Recorded attendance for worker #" + attendance.getWorkerId());
        return id;
    }

    @Override
    public boolean correctAttendance(int attendanceId, String newStatus, String newRemarks) {
        // Enforce specific permission for past corrections
        PermissionChecker.require("ATTENDANCE_CORRECT");

        Attendance current = getAttendanceById(attendanceId);
        String oldVal = current.getStatus() + " (Remarks: " + current.getRemarks() + ")";
        String newVal = newStatus + " (Remarks: " + newRemarks + ")";

        current.setStatus(newStatus);
        current.setRemarks(newRemarks);
        User recorder = SessionContext.getInstance().getCurrentUser();
        if (recorder != null) {
            current.setRecordedBy(recorder.getUserId());
        }

        boolean updated = attendanceDAO.update(current);
        if (updated) {
            auditService.logCurrent("ATTENDANCE_CORRECTED", "ATTENDANCE", "ATTENDANCE", String.valueOf(attendanceId),
                oldVal, newVal, "SUCCESS", "Corrected attendance record #" + attendanceId);
        }
        return updated;
    }

    @Override
    public boolean deleteAttendance(int attendanceId) {
        PermissionChecker.require("ATTENDANCE_CORRECT");
        Attendance a = getAttendanceById(attendanceId);
        boolean deleted = attendanceDAO.delete(attendanceId);
        if (deleted) {
            auditService.logCurrent("ATTENDANCE_DELETED", "ATTENDANCE", "ATTENDANCE", String.valueOf(attendanceId),
                a.getStatus(), null, "SUCCESS", "Deleted attendance record #" + attendanceId);
        }
        return deleted;
    }

    @Override
    public double getAttendanceRate(Integer projectId, LocalDate startDate, LocalDate endDate) {
        PermissionChecker.require("ATTENDANCE_VIEW");
        return attendanceDAO.calculateAttendanceRate(projectId, startDate, endDate);
    }
}
