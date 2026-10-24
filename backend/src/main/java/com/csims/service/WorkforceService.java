package com.csims.service;

import com.csims.model.Contractor;
import com.csims.model.Worker;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkforceService {
    private final ProjectService projectService;

    public WorkforceService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public List<Contractor> getAllContractors() {
        return projectService.getAllContractors();
    }

    public Contractor getContractorById(Integer id) {
        return projectService.getContractorById(id);
    }

    public Integer createContractor(Contractor c, Long operatorId, String operatorUsername) {
        return projectService.createContractor(c, operatorId, operatorUsername);
    }

    public void updateContractor(Integer id, Contractor c, Long operatorId, String operatorUsername) {
        projectService.updateContractor(id, c, operatorId, operatorUsername);
    }

    public List<Worker> getAllWorkers(String status, Integer contractorId) {
        return projectService.getAllWorkers(status, contractorId);
    }

    public Worker getWorkerById(Integer id) {
        return projectService.getWorkerById(id);
    }

    public Integer createWorker(Worker w, Long operatorId, String operatorUsername) {
        return projectService.createWorker(w, operatorId, operatorUsername);
    }

    public void updateWorker(Integer id, Worker w, Long operatorId, String operatorUsername) {
        projectService.updateWorker(id, w, operatorId, operatorUsername);
    }
}
