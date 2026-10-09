package com.csims.service.impl;

import com.csims.dao.ContractorDAO;
import com.csims.dao.ProjectWorkerDAO;
import com.csims.dao.WorkerDAO;
import com.csims.dao.impl.ContractorDAOImpl;
import com.csims.dao.impl.ProjectWorkerDAOImpl;
import com.csims.dao.impl.WorkerDAOImpl;
import com.csims.model.Contractor;
import com.csims.model.ProjectWorker;
import com.csims.model.Worker;
import com.csims.security.PermissionChecker;
import com.csims.service.AuditService;
import com.csims.service.WorkforceService;
import com.csims.util.ValidationException;
import com.csims.util.Validator;

import java.time.LocalDate;
import java.util.List;

public class WorkforceServiceImpl implements WorkforceService {

    private final WorkerDAO workerDAO = new WorkerDAOImpl();
    private final ContractorDAO contractorDAO = new ContractorDAOImpl();
    private final ProjectWorkerDAO projectWorkerDAO = new ProjectWorkerDAOImpl();
    private final AuditService auditService = new AuditServiceImpl();

    @Override
    public List<Worker> getAllWorkers() {
        PermissionChecker.require("WORKFORCE_VIEW");
        return workerDAO.findAll();
    }

    @Override
    public List<Worker> searchWorkers(String keyword, Integer contractorId, String status) {
        PermissionChecker.require("WORKFORCE_VIEW");
        return workerDAO.search(keyword, contractorId, status);
    }

    @Override
    public Worker getWorkerById(int workerId) {
        PermissionChecker.require("WORKFORCE_VIEW");
        return workerDAO.findById(workerId)
            .orElseThrow(() -> new ValidationException("Worker #" + workerId + " not found."));
    }

    @Override
    public List<Worker> getWorkersByProject(int projectId) {
        PermissionChecker.require("WORKFORCE_VIEW");
        return workerDAO.findByProjectId(projectId);
    }

    @Override
    public int createWorker(Worker worker) {
        PermissionChecker.require("WORKFORCE_CREATE");
        Validator.requireNonEmpty(worker.getFullName(), "Full Name");
        Validator.requireNonEmpty(worker.getTrade(), "Trade / Skill");
        if (worker.getJoinedDate() == null) {
            worker.setJoinedDate(LocalDate.now());
        }

        int id = workerDAO.create(worker);
        auditService.logCurrent("WORKER_CREATE", "WORKFORCE", "WORKER", String.valueOf(id),
            null, worker.getFullName(), "SUCCESS", "Registered worker: " + worker.getFullName());
        return id;
    }

    @Override
    public boolean updateWorker(Worker worker) {
        PermissionChecker.require("WORKFORCE_UPDATE");
        Validator.requireNonEmpty(worker.getFullName(), "Full Name");
        Validator.requireNonEmpty(worker.getTrade(), "Trade / Skill");

        Worker old = getWorkerById(worker.getWorkerId());
        String oldVal = old.getFullName() + ", trade: " + old.getTrade() + ", status: " + old.getStatus();
        String newVal = worker.getFullName() + ", trade: " + worker.getTrade() + ", status: " + worker.getStatus();

        boolean updated = workerDAO.update(worker);
        if (updated) {
            auditService.logCurrent("WORKER_UPDATE", "WORKFORCE", "WORKER", String.valueOf(worker.getWorkerId()),
                oldVal, newVal, "SUCCESS", "Updated worker details.");
        }
        return updated;
    }

    @Override
    public boolean deleteWorker(int workerId) {
        PermissionChecker.require("WORKFORCE_UPDATE");
        Worker w = getWorkerById(workerId);
        boolean deleted = workerDAO.delete(workerId);
        if (deleted) {
            auditService.logCurrent("WORKER_DELETE", "WORKFORCE", "WORKER", String.valueOf(workerId),
                w.getFullName(), null, "SUCCESS", "Removed worker record #" + workerId);
        }
        return deleted;
    }

    @Override
    public List<Contractor> getAllContractors() {
        PermissionChecker.require("WORKFORCE_VIEW");
        return contractorDAO.findAll();
    }

    @Override
    public List<Contractor> searchContractors(String keyword) {
        PermissionChecker.require("WORKFORCE_VIEW");
        return contractorDAO.search(keyword);
    }

    @Override
    public Contractor getContractorById(int contractorId) {
        PermissionChecker.require("WORKFORCE_VIEW");
        return contractorDAO.findById(contractorId)
            .orElseThrow(() -> new ValidationException("Contractor #" + contractorId + " not found."));
    }

    @Override
    public int createContractor(Contractor contractor) {
        PermissionChecker.require("WORKFORCE_CREATE");
        Validator.requireNonEmpty(contractor.getCompanyName(), "Company Name");

        int id = contractorDAO.create(contractor);
        auditService.logCurrent("CONTRACTOR_CREATE", "WORKFORCE", "CONTRACTOR", String.valueOf(id),
            null, contractor.getCompanyName(), "SUCCESS", "Added contractor company: " + contractor.getCompanyName());
        return id;
    }

    @Override
    public boolean updateContractor(Contractor contractor) {
        PermissionChecker.require("WORKFORCE_UPDATE");
        Validator.requireNonEmpty(contractor.getCompanyName(), "Company Name");

        Contractor old = getContractorById(contractor.getContractorId());
        boolean updated = contractorDAO.update(contractor);
        if (updated) {
            auditService.logCurrent("CONTRACTOR_UPDATE", "WORKFORCE", "CONTRACTOR", String.valueOf(contractor.getContractorId()),
                old.getCompanyName(), contractor.getCompanyName(), "SUCCESS", "Updated contractor details.");
        }
        return updated;
    }

    @Override
    public boolean deleteContractor(int contractorId) {
        PermissionChecker.require("WORKFORCE_UPDATE");
        Contractor c = getContractorById(contractorId);
        boolean deleted = contractorDAO.delete(contractorId);
        if (deleted) {
            auditService.logCurrent("CONTRACTOR_DELETE", "WORKFORCE", "CONTRACTOR", String.valueOf(contractorId),
                c.getCompanyName(), null, "SUCCESS", "Removed contractor record #" + contractorId);
        }
        return deleted;
    }

    @Override
    public boolean assignWorkerToProject(int projectId, int workerId, String roleInProject, LocalDate assignedDate) {
        PermissionChecker.require("WORKFORCE_ASSIGN");
        Validator.requireNonEmpty(roleInProject, "Role in Project");
        if (assignedDate == null) {
            assignedDate = LocalDate.now();
        }

        boolean assigned = projectWorkerDAO.assign(projectId, workerId, roleInProject, assignedDate);
        if (assigned) {
            auditService.logCurrent("WORKER_ASSIGN_PROJECT", "WORKFORCE", "PROJECT_WORKER",
                projectId + ":" + workerId, null, roleInProject, "SUCCESS", "Assigned worker #" + workerId + " to project #" + projectId);
        }
        return assigned;
    }

    @Override
    public boolean removeWorkerFromProject(int projectId, int workerId) {
        PermissionChecker.require("WORKFORCE_ASSIGN");
        boolean removed = projectWorkerDAO.unassign(projectId, workerId);
        if (removed) {
            auditService.logCurrent("WORKER_UNASSIGN_PROJECT", "WORKFORCE", "PROJECT_WORKER",
                projectId + ":" + workerId, null, null, "SUCCESS", "Unassigned worker #" + workerId + " from project #" + projectId);
        }
        return removed;
    }

    @Override
    public List<ProjectWorker> getProjectWorkers(int projectId) {
        PermissionChecker.require("WORKFORCE_VIEW");
        return projectWorkerDAO.findByProjectId(projectId);
    }
}
