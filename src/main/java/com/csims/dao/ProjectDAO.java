package com.csims.dao;

import com.csims.model.Project;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProjectDAO {
    Optional<Project> findById(int projectId);
    List<Project> findAll();
    List<Project> search(String keyword, String status);
    int create(Project project);
    boolean update(Project project);
    boolean updateProgress(int projectId, int progressPercent);
    boolean updateStatus(int projectId, String status);
    boolean delete(int projectId);
    Map<String, Integer> countByStatus();
    int countTotal();
}
