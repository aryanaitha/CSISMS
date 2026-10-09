package com.csims.dao;

import com.csims.model.Attendance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceDAO {
    Optional<Attendance> findById(int attendanceId);
    Optional<Attendance> findByWorkerProjectDate(int workerId, int projectId, LocalDate date);
    List<Attendance> search(Integer projectId, Integer workerId, LocalDate startDate, LocalDate endDate, String status);
    int record(Attendance attendance);
    boolean update(Attendance attendance);
    boolean delete(int attendanceId);
    double calculateAttendanceRate(Integer projectId, LocalDate startDate, LocalDate endDate);
}
