package com.csims.dao;

import com.csims.model.ProjectWorker;
import java.time.LocalDate;
import java.util.List;

public interface ProjectWorkerDAO {
    boolean assign(int projectId, int workerId, String roleInProject, LocalDate assignedDate);
    boolean unassign(int projectId, int workerId);
    List<ProjectWorker> findByProjectId(int projectId);
    List<ProjectWorker> findByWorkerId(int workerId);
    boolean isAssigned(int projectId, int workerId);
}
