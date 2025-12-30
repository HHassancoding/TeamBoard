package com.teamboard.service;

import com.teamboard.entity.Project;
import com.teamboard.entity.Workspace;
import com.teamboard.repository.ProjectRepo;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectImp implements ProjectService{

  private final ProjectRepo projectRepo;
  private final WorkspaceService workspaceService ;



  public ProjectImp(ProjectRepo projectRepo, WorkspaceService workspaceService) {
    this.projectRepo = projectRepo;
    this.workspaceService = workspaceService;
  }


  @Override
  public Project createProject(Project project) {
    Workspace workspace = workspaceService.getWorkspace(project.getWorkspace().getId());
    if(workspace == null) {
      throw new IllegalArgumentException("Workspace not found");
    }

    return projectRepo.save(project);
  }

  @Override
  public Project getProjectById(Long id) {
    return projectRepo.findById(id).orElseThrow(
        () -> new IllegalArgumentException("Project not found" + id)
    );
  }

  @Override
  public List<Project> getProjectsByWorkspaceId(Long workspaceId) {
    Workspace workspace = workspaceService.getWorkspace(workspaceId);
    if(workspace == null) {
      throw new IllegalArgumentException("Workspace not found");
    }
    return projectRepo.findByWorkspaceId(workspaceId);
  }

  @Override
  public Project updateProject(Project project) {
    projectRepo.findById(project.getId()).orElseThrow(
        () -> new IllegalArgumentException("Project not found" + project.getId())
    );
    return projectRepo.save(project);
  }

  @Override
  public void deleteProject(Long id) {
    Project project = projectRepo.findById(id).orElseThrow(
        () -> new IllegalArgumentException("Project not found" + id)
    );
    projectRepo.deleteById(id);
  }
}
