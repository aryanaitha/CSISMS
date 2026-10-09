package com.csims.service;

import com.csims.exception.ApiException;
import com.csims.model.Contractor;
import com.csims.model.Worker;
import com.csims.repository.WorkforceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkforceService {
    private final WorkforceRepository workforceRepository;
    private final AuditService auditService;

    public WorkforceService(WorkforceRepository workforceRepository, AuditService auditService) {
        this.workforceRepository = workforceRepository;
        this.auditService = auditService;
    }

    public List<Worker> getAllWorkers(String status, Integer contractorId) {
        return workforceRepository.findAllWorkers(status, contractorId);
    }

    public Worker getWorkerById(Integer id) {
        return workforceRepository.findWorkerById(id)
                .orElseThrow(() -> new ApiException("Worker not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer createWorker(Worker w, Long operatorId, String operatorUsername) {
        Integer id = workforceRepository.createWorker(w);
        auditService.log(operatorId, operatorUsername, null, "WORKER_CREATE", "WORKFORCE", "WORKER", String.valueOf(id), null, w.getFullName(), "SUCCESS", "Added new worker " + w.getFullName() + " (" + w.getTrade() + ")");
        return id;
    }

    @Transactional
    public void updateWorker(Integer id, Worker w, Long operatorId, String operatorUsername) {
        Worker existing = getWorkerById(id);
        w.setId(id);
        workforceRepository.updateWorker(w);
        auditService.log(operatorId, operatorUsername, null, "WORKER_UPDATE", "WORKFORCE", "WORKER", String.valueOf(id), existing.getStatus(), w.getStatus(), "SUCCESS", "Updated worker " + w.getFullName());
    }

    public List<Contractor> getAllContractors() {
        return workforceRepository.findAllContractors();
    }

    public Contractor getContractorById(Integer id) {
        return workforceRepository.findContractorById(id)
                .orElseThrow(() -> new ApiException("Contractor not found with ID " + id, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Integer createContractor(Contractor c, Long operatorId, String operatorUsername) {
        Integer id = workforceRepository.createContractor(c);
        auditService.log(operatorId, operatorUsername, null, "CONTRACTOR_CREATE", "WORKFORCE", "CONTRACTOR", String.valueOf(id), null, c.getCompanyName(), "SUCCESS", "Registered contractor " + c.getCompanyName());
        return id;
    }

    @Transactional
    public void updateContractor(Integer id, Contractor c, Long operatorId, String operatorUsername) {
        Contractor existing = getContractorById(id);
        c.setId(id);
        workforceRepository.updateContractor(c);
        auditService.log(operatorId, operatorUsername, null, "CONTRACTOR_UPDATE", "WORKFORCE", "CONTRACTOR", String.valueOf(id), existing.getCompanyName(), c.getCompanyName(), "SUCCESS", "Updated contractor " + c.getCompanyName());
    }
}
