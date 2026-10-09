package com.csims.service;

import com.csims.model.Contractor;
import com.csims.model.ProjectWorker;
import com.csims.model.Worker;

import java.time.LocalDate;
import java.util.List;

public interface WorkforceService {
    List<Worker> getAllWorkers();
    List<Worker> searchWorkers(String keyword, Integer contractorId, String status);
    Worker getWorkerById(int workerId);
    List<Worker> getWorkersByProject(int projectId);
    int createWorker(Worker worker);
    boolean updateWorker(Worker worker);
    boolean deleteWorker(int workerId);

    List<Contractor> getAllContractors();
    List<Contractor> searchContractors(String keyword);
    Contractor getContractorById(int contractorId);
    int createContractor(Contractor contractor);
    boolean updateContractor(Contractor contractor);
    boolean deleteContractor(int contractorId);

    boolean assignWorkerToProject(int projectId, int workerId, String roleInProject, LocalDate assignedDate);
    boolean removeWorkerFromProject(int projectId, int workerId);
    List<ProjectWorker> getProjectWorkers(int projectId);
}
