package com.csims.repository;

import com.csims.model.Attendance;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class AttendanceRepository {
    private final ProjectRepository projectRepository;

    public AttendanceRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Attendance> findAll(Integer projectId, LocalDate date, Integer workerId) {
        return projectRepository.findAllAttendance(projectId, date, workerId);
    }

    public Optional<Attendance> findById(Integer id) {
        return projectRepository.findAttendanceById(id);
    }

    public void record(Attendance a) {
        projectRepository.recordAttendance(a);
    }

    public void correct(Integer id, String status, String remarks, Long recordedBy) {
        projectRepository.correctAttendance(id, status, remarks, recordedBy);
    }
}
