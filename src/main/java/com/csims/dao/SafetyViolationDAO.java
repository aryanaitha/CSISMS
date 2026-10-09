package com.csims.dao;

import com.csims.model.SafetyViolation;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SafetyViolationDAO {
    Optional<SafetyViolation> findById(int violationId);
    List<SafetyViolation> findAll();
    List<SafetyViolation> search(Integer projectId, Integer workerId, String severity, String status);
    int create(SafetyViolation violation);
    boolean update(SafetyViolation violation);
    boolean updateResolution(int violationId, String status, String correctiveAction, LocalDate resolutionDate);
    boolean delete(int violationId);
    Map<String, Integer> countBySeverity();
    int countOpen();
    int countTotal();
}
