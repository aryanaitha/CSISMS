package com.csims.repository;

import com.csims.model.Contractor;
import com.csims.model.ProjectWorker;
import com.csims.model.Worker;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class WorkforceRepository {
    private final ProjectRepository projectRepository;

    public WorkforceRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Contractor> findAllContractors() {
        return projectRepository.findAllContractors();
    }

    public Optional<Contractor> findContractorById(Integer id) {
        return projectRepository.findContractorById(id);
    }

    public Integer createContractor(Contractor c) {
        return projectRepository.createContractor(c);
    }

    public void updateContractor(Contractor c) {
        projectRepository.updateContractor(c);
    }

    public List<Worker> findAllWorkers(String status, Integer contractorId) {
        return projectRepository.findAllWorkers(status, contractorId);
    }

    public Optional<Worker> findWorkerById(Integer id) {
        return projectRepository.findWorkerById(id);
    }

    public Integer createWorker(Worker w) {
        return projectRepository.createWorker(w);
    }

    public void updateWorker(Worker w) {
        projectRepository.updateWorker(w);
    }

    public List<ProjectWorker> findWorkersByProjectId(Integer projectId) {
        return projectRepository.findWorkersByProjectId(projectId);
    }

    public void assignWorkerToProject(Integer projectId, Integer workerId, String roleInProject) {
        projectRepository.assignWorkerToProject(projectId, workerId, roleInProject);
    }

    public void removeWorkerFromProject(Integer projectId, Integer workerId) {
        projectRepository.removeWorkerFromProject(projectId, workerId);
    }
}
