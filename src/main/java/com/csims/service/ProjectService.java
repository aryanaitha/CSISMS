package com.csims.service;

import com.csims.model.Project;
import java.util.List;
import java.util.Map;

public interface ProjectService {
    List<Project> getAllProjects();
    List<Project> searchProjects(String keyword, String status);
    Project getProjectById(int projectId);
    int createProject(Project project);
    boolean updateProject(Project project);
    boolean updateProgress(int projectId, int progressPercent);
    boolean archiveProject(int projectId);
    boolean deleteProject(int projectId);
    Map<String, Integer> getProjectStats();
}
