package com.teamboard;

import com.teamboard.entity.User;
import com.teamboard.entity.Workspace;
import com.teamboard.repository.WorkspaceRepository;
import com.teamboard.service.WorkspaceImp;
import com.teamboard.service.WorkspaceMemberService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkspaceImpTests {

  @Mock
  private WorkspaceRepository workspaceRepository;

  @Mock
  private ObjectProvider<WorkspaceMemberService> workspaceMemberServiceProvider;

  @InjectMocks
  private WorkspaceImp workspaceImp;

  private User owner;
  private Workspace workspace1;
  private Workspace workspace2;
  private List<Workspace> workspaceList;
  private Optional<Workspace> optionalWorkspace1;
  private Optional<Workspace> emptyOptional;

  @BeforeEach
  void setUp() {
    // Create test user (owner)
    owner = new User();
    owner.setId(1L);
    owner.setEmail("owner@test.com");
    owner.setName("Owner User");
    owner.setPasswordHash("hashed_password");

    // Create test workspaces
    workspace1 = new Workspace();
    workspace1.setId(1L);
    workspace1.setName("Team A");
    workspace1.setDescription("First team workspace");
    workspace1.setOwner(owner);
    workspace1.setCreatedAt(LocalDateTime.now());
    workspace1.setUpdatedAt(LocalDateTime.now());

    workspace2 = new Workspace();
    workspace2.setId(2L);
    workspace2.setName("Team B");
    workspace2.setDescription("Second team workspace");
    workspace2.setOwner(owner);
    workspace2.setCreatedAt(LocalDateTime.now());
    workspace2.setUpdatedAt(LocalDateTime.now());

    // Create test collections
    workspaceList = Arrays.asList(workspace1, workspace2);
    optionalWorkspace1 = Optional.of(workspace1);
    emptyOptional = Optional.empty();
  }

  // ==================== getAllWorkspaces Tests ====================

  @Test
  public void getAllWorkspacesTest() {
    // Arrange
    when(workspaceRepository.findAll()).thenReturn(workspaceList);

    // Act
    List<Workspace> result = workspaceImp.getAllWorkspaces();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Team A", result.get(0).getName());
    assertEquals("Team B", result.get(1).getName());
    verify(workspaceRepository).findAll();
  }

  @Test
  public void getAllWorkspacesEmptyTest() {
    // Arrange
    when(workspaceRepository.findAll()).thenReturn(Collections.emptyList());

    // Act
    List<Workspace> result = workspaceImp.getAllWorkspaces();

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(workspaceRepository).findAll();
  }

  // ==================== getWorkspace Tests ====================

  @Test
  public void getWorkspaceByIdTest() {
    // Arrange
    when(workspaceRepository.findById(1L)).thenReturn(optionalWorkspace1);

    // Act
    Workspace result = workspaceImp.getWorkspace(1L);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Team A", result.getName());
    assertEquals("First team workspace", result.getDescription());
    verify(workspaceRepository).findById(1L);
  }

  @Test
  public void getWorkspaceNotFoundTest() {
    // Arrange
    when(workspaceRepository.findById(99L)).thenReturn(emptyOptional);

    // Act
    Workspace result = workspaceImp.getWorkspace(99L);

    // Assert
    assertNull(result);
    verify(workspaceRepository).findById(99L);
  }

  // ==================== createWorkspace Tests ====================

  @Test
  public void createWorkspaceTest() {
    // Arrange
    Workspace newWorkspace = new Workspace();
    newWorkspace.setName("New Team");
    newWorkspace.setDescription("Brand new workspace");
    newWorkspace.setOwner(owner);

    Workspace savedWorkspace = new Workspace();
    savedWorkspace.setId(3L);
    savedWorkspace.setName("New Team");
    savedWorkspace.setDescription("Brand new workspace");
    savedWorkspace.setOwner(owner);
    savedWorkspace.setCreatedAt(LocalDateTime.now());
    savedWorkspace.setUpdatedAt(LocalDateTime.now());

    when(workspaceRepository.save(newWorkspace)).thenReturn(savedWorkspace);
    // Mock the ObjectProvider to return a WorkspaceMemberService that does nothing
    WorkspaceMemberService mockMemberService = mock(WorkspaceMemberService.class);
    when(workspaceMemberServiceProvider.getObject()).thenReturn(mockMemberService);

    // Act
    Workspace result = workspaceImp.createWorkspace(newWorkspace);

    // Assert
    assertNotNull(result);
    assertEquals(3L, result.getId());
    assertEquals("New Team", result.getName());
    assertEquals(owner.getId(), result.getOwner().getId());
    verify(workspaceRepository).save(newWorkspace);
  }

  // ==================== updateWorkspace Tests ====================

  @Test
  public void updateWorkspaceSuccessTest() {
    // Arrange
    Workspace updatedWorkspace = new Workspace();
    updatedWorkspace.setId(1L);
    updatedWorkspace.setName("Team A - Updated");
    updatedWorkspace.setDescription("Updated description");
    updatedWorkspace.setOwner(owner);

    when(workspaceRepository.findById(1L)).thenReturn(optionalWorkspace1);
    when(workspaceRepository.save(updatedWorkspace)).thenReturn(updatedWorkspace);

    // Act
    Workspace result = workspaceImp.updateWorkspace(updatedWorkspace);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Team A - Updated", result.getName());
    assertEquals("Updated description", result.getDescription());
    verify(workspaceRepository).findById(1L);
    verify(workspaceRepository).save(updatedWorkspace);
  }

  @Test
  public void updateWorkspaceNotFoundTest() {
    // Arrange
    Workspace nonExistentWorkspace = new Workspace();
    nonExistentWorkspace.setId(99L);
    nonExistentWorkspace.setName("Non-existent");

    when(workspaceRepository.findById(99L)).thenReturn(emptyOptional);

    // Act
    Workspace result = workspaceImp.updateWorkspace(nonExistentWorkspace);

    // Assert
    assertNull(result);
    verify(workspaceRepository).findById(99L);
    verify(workspaceRepository, never()).save(nonExistentWorkspace);
  }

  // ==================== deleteWorkspace Tests ====================

  @Test
  public void deleteWorkspaceTest() {
    // Act
    workspaceImp.deleteWorkspace(1L);

    // Assert
    verify(workspaceRepository, times(1)).deleteById(1L);
  }

  // ==================== getWorkspacesByOwner Tests ====================

  @Test
  public void getWorkspacesByOwnerTest() {
    // Arrange
    when(workspaceRepository.findByOwnerId(1L)).thenReturn(workspaceList);

    // Act
    List<Workspace> result = workspaceImp.getWorkspacesByOwner(1L);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(1L, result.get(0).getOwner().getId());
    assertEquals(1L, result.get(1).getOwner().getId());
    verify(workspaceRepository).findByOwnerId(1L);
  }

  @Test
  public void getWorkspacesByOwnerEmptyTest() {
    // Arrange
    when(workspaceRepository.findByOwnerId(2L)).thenReturn(Collections.emptyList());

    // Act
    List<Workspace> result = workspaceImp.getWorkspacesByOwner(2L);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(workspaceRepository).findByOwnerId(2L);
  }

  // ==================== findByOwnerIdAndName Tests ====================

  @Test
  public void findByOwnerIdAndNameTest() {
    // Arrange
    when(workspaceRepository.findByOwnerIdAndName(1L, "Team A"))
        .thenReturn(optionalWorkspace1);

    // Act
    Workspace result = workspaceImp.findByOwnerIdAndName(1L, "Team A");

    // Assert
    assertNotNull(result);
    assertEquals("Team A", result.getName());
    assertEquals(1L, result.getId());
    verify(workspaceRepository).findByOwnerIdAndName(1L, "Team A");
  }

  @Test
  public void findByOwnerIdAndNameNotFoundTest() {
    // Arrange
    when(workspaceRepository.findByOwnerIdAndName(1L, "Non-existent"))
        .thenReturn(emptyOptional);

    // Act
    Workspace result = workspaceImp.findByOwnerIdAndName(1L, "Non-existent");

    // Assert
    assertNull(result);
    verify(workspaceRepository).findByOwnerIdAndName(1L, "Non-existent");
  }

  // ==================== searchWorkspacesByName Tests ====================

  @Test
  public void searchWorkspacesByNameTest() {
    // Arrange
    List<Workspace> searchResults = Arrays.asList(workspace1);
    when(workspaceRepository.findByNameContainingIgnoreCase("Team"))
        .thenReturn(workspaceList);

    // Act
    List<Workspace> result = workspaceImp.searchWorkspacesByName("Team");

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(w -> w.getName().contains("Team")));
    verify(workspaceRepository).findByNameContainingIgnoreCase("Team");
  }

  @Test
  public void searchWorkspacesByNameEmptyTest() {
    // Arrange
    when(workspaceRepository.findByNameContainingIgnoreCase("NonExistent"))
        .thenReturn(Collections.emptyList());

    // Act
    List<Workspace> result = workspaceImp.searchWorkspacesByName("NonExistent");

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(workspaceRepository).findByNameContainingIgnoreCase("NonExistent");
  }

  // ==================== getWorkspacesForUser Tests ====================

  @Test
  public void getWorkspacesForUserTest() {
    // Arrange
    when(workspaceRepository.findAllAccessibleByUser(1L)).thenReturn(workspaceList);

    // Act
    List<Workspace> result = workspaceImp.getWorkspacesForUser(1L);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(workspaceRepository).findAllAccessibleByUser(1L);
  }

  @Test
  public void getWorkspacesForUserEmptyTest() {
    // Arrange
    when(workspaceRepository.findAllAccessibleByUser(2L)).thenReturn(Collections.emptyList());

    // Act
    List<Workspace> result = workspaceImp.getWorkspacesForUser(2L);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(workspaceRepository).findAllAccessibleByUser(2L);
  }
}
