package com.teamboard.service;

import com.teamboard.entity.Workspace;
import java.util.List;

public interface WorkspaceService {
  List<Workspace> getAllWorkspaces();

  Workspace getWorkspace(Long id);

  Workspace createWorkspace(Workspace workspace);

  Workspace updateWorkspace(Workspace workspace);

  void deleteWorkspace(Long id);

  List<Workspace> getWorkspacesByOwner(Long ownerId);

  Workspace findByOwnerIdAndName(Long ownerId, String name);

  List<Workspace> searchWorkspacesByName(String name);
}

