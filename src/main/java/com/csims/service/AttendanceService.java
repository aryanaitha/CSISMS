package com.csims.service;

import com.csims.model.Attendance;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    List<Attendance> searchAttendance(Integer projectId, Integer workerId, LocalDate startDate, LocalDate endDate, String status);
    Attendance getAttendanceById(int attendanceId);
    int recordAttendance(Attendance attendance);
    boolean correctAttendance(int attendanceId, String newStatus, String newRemarks);
    boolean deleteAttendance(int attendanceId);
    double getAttendanceRate(Integer projectId, LocalDate startDate, LocalDate endDate);
}
