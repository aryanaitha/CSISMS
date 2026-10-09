package com.csims.dao;

import com.csims.model.Worker;
import java.util.List;
import java.util.Optional;

public interface WorkerDAO {
    Optional<Worker> findById(int workerId);
    List<Worker> findAll();
    List<Worker> search(String keyword, Integer contractorId, String status);
    List<Worker> findByProjectId(int projectId);
    int create(Worker worker);
    boolean update(Worker worker);
    boolean delete(int workerId);
    int countTotal();
}
