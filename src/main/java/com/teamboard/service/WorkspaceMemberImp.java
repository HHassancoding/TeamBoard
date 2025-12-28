package com.teamboard.service;

import com.teamboard.entity.Workspace;
import com.teamboard.entity.WorkspaceMember;
import com.teamboard.entity.User;
import com.teamboard.entity.MemberRole;
import com.teamboard.repository.WorkspaceMemberRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceMemberImp implements WorkspaceMemberService {
  private final WorkspaceMemberRepository workspaceMemberRepository;
  private final WorkspaceService workspaceService;
  private final UserService userService;

  public WorkspaceMemberImp(
      WorkspaceMemberRepository workspaceMemberRepository,
      WorkspaceService workspaceService,
      UserService userService) {
    this.workspaceMemberRepository = workspaceMemberRepository;
    this.workspaceService = workspaceService;
    this.userService = userService;
  }

  @Override
  public WorkspaceMember addMember(Long userId, Long workspaceId, MemberRole role) {
    // Validate user exists
    User user = userService.getUser(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found with id: " + userId);
    }

    // Validate workspace exists
    Workspace workspace = workspaceService.getWorkspace(workspaceId);
    if (workspace == null) {
      throw new IllegalArgumentException("Workspace not found with id: " + workspaceId);
    }

    // Check if member already exists
    Optional<WorkspaceMember> existing =
        workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, workspaceId);
    if (existing.isPresent()) {
      throw new IllegalArgumentException("User is already a member of this workspace");
    }

    // Create and save member
    WorkspaceMember member =
        WorkspaceMember.builder()
            .user(user)
            .workspace(workspace)
            .role(role != null ? role : MemberRole.MEMBER)
            .build();

    return workspaceMemberRepository.save(member);
  }

  @Override
  public void removeMember(Long userId, Long workspaceId) {
    Optional<WorkspaceMember> member =
        workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, workspaceId);

    if (member.isEmpty()) {
      throw new IllegalArgumentException(
          "Member not found with userId: " + userId + " and workspaceId: " + workspaceId);
    }

    // Prevent removing workspace owner
    Workspace workspace = workspaceService.getWorkspace(workspaceId);
    if (workspace != null && workspace.getOwner().getId().equals(userId)) {
      throw new IllegalArgumentException("Cannot remove workspace owner");
    }

    workspaceMemberRepository.deleteById(member.get().getId());
  }

  @Override
  public List<WorkspaceMember> getMembersOfWorkspace(Long workspaceId) {
    // Validate workspace exists
    Workspace workspace = workspaceService.getWorkspace(workspaceId);
    if (workspace == null) {
      throw new IllegalArgumentException("Workspace not found with id: " + workspaceId);
    }

    return workspaceMemberRepository.findByWorkspaceId(workspaceId);
  }

  @Override
  public List<WorkspaceMember> getUserWorkspaces(Long userId) {
    // Validate user exists
    User user = userService.getUser(userId);
    if (user == null) {
      throw new IllegalArgumentException("User not found with id: " + userId);
    }

    return workspaceMemberRepository.findByUserId(userId);
  }

  @Override
  public WorkspaceMember getMember(Long userId, Long workspaceId) {
    return workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, workspaceId).orElse(null);
  }

  @Override
  public WorkspaceMember updateMemberRole(
      Long userId, Long workspaceId, MemberRole newRole) {
    Optional<WorkspaceMember> member =
        workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, workspaceId);

    if (member.isEmpty()) {
      throw new IllegalArgumentException(
          "Member not found with userId: " + userId + " and workspaceId: " + workspaceId);
    }

    WorkspaceMember existingMember = member.get();
    existingMember.setRole(newRole);
    return workspaceMemberRepository.save(existingMember);
  }
}

