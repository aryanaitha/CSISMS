package com.csims.service;

import com.csims.model.Attendance;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {
    private final ProjectService projectService;

    public AttendanceService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public List<Attendance> getAttendance(Integer projectId, LocalDate date, Integer workerId) {
        return projectService.getAttendance(projectId, date, workerId);
    }

    public Attendance getAttendanceById(Integer id) {
        return projectService.getAttendanceById(id);
    }

    public void recordAttendance(Attendance a, Long operatorId, String operatorUsername) {
        projectService.recordAttendance(a, operatorId, operatorUsername);
    }

    public void correctAttendance(Integer id, String status, String remarks, Long operatorId, String operatorUsername) {
        projectService.correctAttendance(id, status, remarks, operatorId, operatorUsername);
    }
}
