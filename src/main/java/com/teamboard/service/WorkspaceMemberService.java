package com.teamboard.service;

import com.teamboard.entity.WorkspaceMember;
import com.teamboard.entity.MemberRole;
import java.util.List;

public interface WorkspaceMemberService {
  WorkspaceMember addMember(Long userId, Long workspaceId, MemberRole role);

  void removeMember(Long userId, Long workspaceId);

  List<WorkspaceMember> getMembersOfWorkspace(Long workspaceId);

  List<WorkspaceMember> getUserWorkspaces(Long userId);

  WorkspaceMember getMember(Long userId, Long workspaceId);

  WorkspaceMember updateMemberRole(Long userId, Long workspaceId, MemberRole newRole);
}

