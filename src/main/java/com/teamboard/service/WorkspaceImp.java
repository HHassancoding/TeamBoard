package com.teamboard.service;

import com.teamboard.entity.Workspace;
import com.teamboard.entity.MemberRole;
import com.teamboard.repository.WorkspaceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceImp implements WorkspaceService {
  private final WorkspaceRepository workspaceRepository;
  private final ObjectProvider<WorkspaceMemberService> workspaceMemberServiceProvider;

  public WorkspaceImp(
      WorkspaceRepository workspaceRepository,
      ObjectProvider<WorkspaceMemberService> workspaceMemberServiceProvider) {
    this.workspaceRepository = workspaceRepository;
    this.workspaceMemberServiceProvider = workspaceMemberServiceProvider;
  }

  @Override
  public List<Workspace> getAllWorkspaces() {
    return workspaceRepository.findAll();
  }

  @Override
  public Workspace getWorkspace(Long id) {
    return workspaceRepository.findById(id).orElse(null);
  }

  @Override
  public Workspace createWorkspace(Workspace workspace) {
    Workspace savedWorkspace = workspaceRepository.save(workspace);

    // Auto-add owner as ADMIN member
    try {
      WorkspaceMemberService memberService = workspaceMemberServiceProvider.getObject();
      memberService.addMember(
          workspace.getOwner().getId(),
          savedWorkspace.getId(),
          MemberRole.ADMIN);
    } catch (IllegalArgumentException e) {
      // Member already exists, continue
    }

    return savedWorkspace;
  }

  @Override
  public Workspace updateWorkspace(Workspace workspace) {
    Optional<Workspace> workspaceToUpdate = workspaceRepository.findById(workspace.getId());
    if (workspaceToUpdate.isPresent()) {
      return workspaceRepository.save(workspace);
    }
    return null;
  }

  @Override
  public void deleteWorkspace(Long id) {
    workspaceRepository.deleteById(id);
  }

  @Override
  public List<Workspace> getWorkspacesByOwner(Long ownerId) {
    return workspaceRepository.findByOwnerId(ownerId);
  }

  @Override
  public Workspace findByOwnerIdAndName(Long ownerId, String name) {
    return workspaceRepository.findByOwnerIdAndName(ownerId, name).orElse(null);
  }

  @Override
  public List<Workspace> searchWorkspacesByName(String name) {
    return workspaceRepository.findByNameContainingIgnoreCase(name);
  }

  // New: workspaces owned by or shared with the user
  @Override
  public List<Workspace> getWorkspacesForUser(Long userId) {
    return workspaceRepository.findAllAccessibleByUser(userId);
  }
}
