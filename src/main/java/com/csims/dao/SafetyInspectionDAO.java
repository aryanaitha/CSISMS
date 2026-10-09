package com.csims.dao;

import com.csims.model.SafetyInspection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SafetyInspectionDAO {
    Optional<SafetyInspection> findById(int inspectionId);
    List<SafetyInspection> findAll();
    List<SafetyInspection> search(Integer projectId, Integer inspectorId, LocalDate startDate, LocalDate endDate);
    int create(SafetyInspection inspection);
    boolean delete(int inspectionId);
    double getAverageSafetyScore();
    int countTotal();
}
