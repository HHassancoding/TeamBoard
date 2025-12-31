package com.teamboard.service;

import com.teamboard.entity.Project;
import java.util.List;

public interface ProjectService {
  Project createProject(Project project);
  Project getProjectById(Long id);
  List<Project> getProjectsByWorkspaceId(Long workspaceId);
  Project updateProject(Project project);
  void deleteProject(Long id);
}
