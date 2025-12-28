package com.teamboard;

import com.teamboard.entity.User;
import com.teamboard.entity.Workspace;
import com.teamboard.entity.WorkspaceMember;
import com.teamboard.entity.MemberRole;
import com.teamboard.repository.WorkspaceMemberRepository;
import com.teamboard.service.WorkspaceMemberImp;
import com.teamboard.service.WorkspaceService;
import com.teamboard.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkspaceMemberImpTests {

  @Mock
  private WorkspaceMemberRepository workspaceMemberRepository;

  @Mock
  private WorkspaceService workspaceService;

  @Mock
  private UserService userService;

  @InjectMocks
  private WorkspaceMemberImp workspaceMemberService;

  private User user;
  private User owner;
  private Workspace workspace;
  private WorkspaceMember member;

  @BeforeEach
  void setUp() {
    // Setup user
    user = new User();
    user.setId(1L);
    user.setEmail("member@example.com");
    user.setName("Member User");

    // Setup owner
    owner = new User();
    owner.setId(2L);
    owner.setEmail("owner@example.com");
    owner.setName("Owner User");

    // Setup workspace
    workspace = new Workspace();
    workspace.setId(1L);
    workspace.setName("Test Workspace");
    workspace.setOwner(owner);

    // Setup member
    member = new WorkspaceMember();
    member.setId(1L);
    member.setUser(user);
    member.setWorkspace(workspace);
    member.setRole(MemberRole.MEMBER);
  }

  @Test
  void testAddMemberSuccess() {
    when(userService.getUser(1L)).thenReturn(user);
    when(workspaceService.getWorkspace(1L)).thenReturn(workspace);
    when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1L, 1L))
        .thenReturn(Optional.empty());
    when(workspaceMemberRepository.save(any(WorkspaceMember.class))).thenReturn(member);

    WorkspaceMember result = workspaceMemberService.addMember(1L, 1L, MemberRole.MEMBER);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals(user.getId(), result.getUser().getId());
    assertEquals(workspace.getId(), result.getWorkspace().getId());
    assertEquals(MemberRole.MEMBER, result.getRole());
    verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
  }

  @Test
  void testAddMemberWithAdminRole() {
    WorkspaceMember adminMember = new WorkspaceMember();
    adminMember.setId(1L);
    adminMember.setUser(user);
    adminMember.setWorkspace(workspace);
    adminMember.setRole(MemberRole.ADMIN);

    when(userService.getUser(1L)).thenReturn(user);
    when(workspaceService.getWorkspace(1L)).thenReturn(workspace);
    when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1L, 1L))
        .thenReturn(Optional.empty());
    when(workspaceMemberRepository.save(any(WorkspaceMember.class))).thenReturn(adminMember);

    WorkspaceMember result = workspaceMemberService.addMember(1L, 1L, MemberRole.ADMIN);

    assertNotNull(result);
    assertEquals(MemberRole.ADMIN, result.getRole());
    verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
  }

  @Test
  void testAddMemberUserNotFound() {
    when(userService.getUser(999L)).thenReturn(null);

    assertThrows(
        IllegalArgumentException.class,
        () -> workspaceMemberService.addMember(999L, 1L, MemberRole.MEMBER));

    verify(workspaceMemberRepository, never()).save(any());
  }

  @Test
  void testAddMemberWorkspaceNotFound() {
    when(userService.getUser(1L)).thenReturn(user);
    when(workspaceService.getWorkspace(999L)).thenReturn(null);

    assertThrows(
        IllegalArgumentException.class,
        () -> workspaceMemberService.addMember(1L, 999L, MemberRole.MEMBER));

    verify(workspaceMemberRepository, never()).save(any());
  }

  @Test
  void testAddMemberAlreadyExists() {
    when(userService.getUser(1L)).thenReturn(user);
    when(workspaceService.getWorkspace(1L)).thenReturn(workspace);
    when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1L, 1L))
        .thenReturn(Optional.of(member));

    assertThrows(
        IllegalArgumentException.class,
        () -> workspaceMemberService.addMember(1L, 1L, MemberRole.MEMBER));

    verify(workspaceMemberRepository, never()).save(any());
  }

  @Test
  void testRemoveMemberSuccess() {
    when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1L, 1L))
        .thenReturn(Optional.of(member));
    when(workspaceService.getWorkspace(1L)).thenReturn(workspace);

    workspaceMemberService.removeMember(1L, 1L);

    verify(workspaceMemberRepository).deleteById(1L);
  }

  @Test
  void testRemoveMemberNotFound() {
    when(workspaceMemberRepository.findByUserIdAndWorkspaceId(999L, 1L))
        .thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> workspaceMemberService.removeMember(999L, 1L));

    verify(workspaceMemberRepository, never()).deleteById(any());
  }

  @Test
  void testRemoveOwnerFails() {
    // Member is owner
    WorkspaceMember ownerMember = new WorkspaceMember();
    ownerMember.setId(1L);
    ownerMember.setUser(owner);
    ownerMember.setWorkspace(workspace);
    ownerMember.setRole(MemberRole.ADMIN);

    when(workspaceMemberRepository.findByUserIdAndWorkspaceId(2L, 1L))
        .thenReturn(Optional.of(ownerMember));
    when(workspaceService.getWorkspace(1L)).thenReturn(workspace);

    assertThrows(
        IllegalArgumentException.class,
        () -> workspaceMemberService.removeMember(2L, 1L));

    verify(workspaceMemberRepository, never()).deleteById(any());
  }

  @Test
  void testGetMembersOfWorkspace() {
    List<WorkspaceMember> members = List.of(member);

    when(workspaceService.getWorkspace(1L)).thenReturn(workspace);
    when(workspaceMemberRepository.findByWorkspaceId(1L)).thenReturn(members);

    List<WorkspaceMember> result = workspaceMemberService.getMembersOfWorkspace(1L);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(member.getId(), result.get(0).getId());
    verify(workspaceMemberRepository).findByWorkspaceId(1L);
  }

  @Test
  void testGetMembersOfWorkspaceNotFound() {
    when(workspaceService.getWorkspace(999L)).thenReturn(null);

    assertThrows(
        IllegalArgumentException.class,
        () -> workspaceMemberService.getMembersOfWorkspace(999L));

    verify(workspaceMemberRepository, never()).findByWorkspaceId(any());
  }

  @Test
  void testGetUserWorkspaces() {
    List<WorkspaceMember> memberships = List.of(member);

    when(userService.getUser(1L)).thenReturn(user);
    when(workspaceMemberRepository.findByUserId(1L)).thenReturn(memberships);

    List<WorkspaceMember> result = workspaceMemberService.getUserWorkspaces(1L);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(member.getId(), result.get(0).getId());
    verify(workspaceMemberRepository).findByUserId(1L);
  }

  @Test
  void testGetUserWorkspacesNotFound() {
    when(userService.getUser(999L)).thenReturn(null);

    assertThrows(
        IllegalArgumentException.class,
        () -> workspaceMemberService.getUserWorkspaces(999L));

    verify(workspaceMemberRepository, never()).findByUserId(any());
  }

  @Test
  void testGetMember() {
    when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1L, 1L))
        .thenReturn(Optional.of(member));

    WorkspaceMember result = workspaceMemberService.getMember(1L, 1L);

    assertNotNull(result);
    assertEquals(member.getId(), result.getId());
    verify(workspaceMemberRepository).findByUserIdAndWorkspaceId(1L, 1L);
  }

  @Test
  void testGetMemberNotFound() {
    when(workspaceMemberRepository.findByUserIdAndWorkspaceId(999L, 1L))
        .thenReturn(Optional.empty());

    WorkspaceMember result = workspaceMemberService.getMember(999L, 1L);

    assertNull(result);
  }

  @Test
  void testUpdateMemberRoleSuccess() {
    WorkspaceMember updatedMember = new WorkspaceMember();
    updatedMember.setId(1L);
    updatedMember.setUser(user);
    updatedMember.setWorkspace(workspace);
    updatedMember.setRole(MemberRole.ADMIN);

    when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1L, 1L))
        .thenReturn(Optional.of(member));
    when(workspaceMemberRepository.save(any(WorkspaceMember.class))).thenReturn(updatedMember);

    WorkspaceMember result = workspaceMemberService.updateMemberRole(1L, 1L, MemberRole.ADMIN);

    assertNotNull(result);
    assertEquals(MemberRole.ADMIN, result.getRole());
    verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
  }

  @Test
  void testUpdateMemberRoleNotFound() {
    when(workspaceMemberRepository.findByUserIdAndWorkspaceId(999L, 1L))
        .thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> workspaceMemberService.updateMemberRole(999L, 1L, MemberRole.ADMIN));

    verify(workspaceMemberRepository, never()).save(any());
  }
}

