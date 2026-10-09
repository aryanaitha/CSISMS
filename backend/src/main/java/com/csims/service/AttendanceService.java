package com.csims.service;

import com.csims.exception.ApiException;
import com.csims.model.Attendance;
import com.csims.repository.AttendanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final AuditService auditService;

    public AttendanceService(AttendanceRepository attendanceRepository, AuditService auditService) {
        this.attendanceRepository = attendanceRepository;
        this.auditService = auditService;
    }

    public List<Attendance> getAttendance(Integer projectId, LocalDate date, Integer workerId) {
        return attendanceRepository.findAll(projectId, date, workerId);
    }

    public Attendance getAttendanceById(Integer id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ApiException("Attendance record not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void recordAttendance(Attendance a, Long operatorId, String operatorUsername) {
        a.setRecordedBy(operatorId);
        attendanceRepository.record(a);
        auditService.log(operatorId, operatorUsername, null, "ATTENDANCE_RECORD", "ATTENDANCE", "ATTENDANCE", String.valueOf(a.getWorkerId()), null, a.getStatus(), "SUCCESS", "Recorded attendance for worker #" + a.getWorkerId() + " on " + a.getAttendanceDate() + ": " + a.getStatus());
    }

    @Transactional
    public void correctAttendance(Integer id, String status, String remarks, Long operatorId, String operatorUsername) {
        Attendance existing = getAttendanceById(id);
        attendanceRepository.correct(id, status, remarks, operatorId);
        auditService.log(operatorId, operatorUsername, null, "ATTENDANCE_CORRECT", "ATTENDANCE", "ATTENDANCE", String.valueOf(id), existing.getStatus(), status, "SUCCESS", "Corrected attendance record #" + id + " from " + existing.getStatus() + " to " + status + ". Reason: " + remarks);
    }
}
