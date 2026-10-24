package com.csims.repository;

import com.csims.model.Incident;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class IncidentRepository {
    private final SafetyRepository safetyRepository;

    public IncidentRepository(SafetyRepository safetyRepository) {
        this.safetyRepository = safetyRepository;
    }

    public List<Incident> findAll(Integer projectId, String severity, String status) {
        return safetyRepository.findAllIncidents(projectId, severity, status);
    }

    public Optional<Incident> findById(Integer id) {
        return safetyRepository.findIncidentById(id);
    }

    public Integer create(Incident inc) {
        return safetyRepository.createIncident(inc);
    }

    public void update(Incident inc) {
        safetyRepository.updateIncident(inc);
    }
}
