package com.teamboard;

import com.teamboard.entity.Project;
import com.teamboard.entity.Workspace;
import com.teamboard.repository.ProjectRepo;
import com.teamboard.service.ProjectImp;
import com.teamboard.service.WorkspaceService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectImp Service Tests")
class ProjectImpTest {

  @Mock
  private ProjectRepo projectRepo;

  @Mock
  private WorkspaceService workspaceService;

  @InjectMocks
  private ProjectImp projectService;

  private Project testProject;
  private Workspace testWorkspace;

  @BeforeEach
  void setUp() {
    testWorkspace = new Workspace();
    testWorkspace.setId(1L);
    testWorkspace.setName("Test Workspace");

    testProject = new Project();
    testProject.setId(1L);
    testProject.setName("Test Project");
    testProject.setWorkspace(testWorkspace);
  }

  @Nested
  @DisplayName("createProject Tests")
  class CreateProjectTests {

    @Test
    @DisplayName("Should successfully create a project when workspace exists")
    void shouldCreateProjectWhenWorkspaceExists() {
      // Arrange
      when(workspaceService.getWorkspace(1L)).thenReturn(testWorkspace);
      when(projectRepo.save(testProject)).thenReturn(testProject);

      // Act
      Project result = projectService.createProject(testProject);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getName()).isEqualTo("Test Project");
      verify(workspaceService, times(1)).getWorkspace(1L);
      verify(projectRepo, times(1)).save(testProject);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when workspace is null")
    void shouldThrowExceptionWhenWorkspaceIsNull() {
      // Arrange
      when(workspaceService.getWorkspace(1L)).thenReturn(null);

      // Act & Assert
      assertThatThrownBy(() -> projectService.createProject(testProject))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Workspace not found");

      verify(projectRepo, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when workspace does not exist")
    void shouldThrowExceptionWhenWorkspaceDoesNotExist() {
      // Arrange
      Long nonExistentWorkspaceId = 999L;
      testProject.getWorkspace().setId(nonExistentWorkspaceId);
      when(workspaceService.getWorkspace(nonExistentWorkspaceId)).thenReturn(null);

      // Act & Assert
      assertThatThrownBy(() -> projectService.createProject(testProject))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Workspace not found");

      verify(projectRepo, never()).save(any());
    }

    @Test
    @DisplayName("Should pass the project to repository with correct workspace association")
    void shouldAssociateProjectWithWorkspaceCorrectly() {
      // Arrange
      when(workspaceService.getWorkspace(1L)).thenReturn(testWorkspace);
      when(projectRepo.save(testProject)).thenReturn(testProject);

      // Act
      projectService.createProject(testProject);

      // Assert
      verify(projectRepo).save(argThat(project ->
          project.getWorkspace().getId().equals(1L)
      ));
    }
  }

  @Nested
  @DisplayName("getProjectById Tests")
  class GetProjectByIdTests {

    @Test
    @DisplayName("Should return project when it exists")
    void shouldReturnProjectWhenExists() {
      // Arrange
      when(projectRepo.findById(1L)).thenReturn(Optional.of(testProject));

      // Act
      Project result = projectService.getProjectById(1L);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getName()).isEqualTo("Test Project");
      verify(projectRepo, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when project not found")
    void shouldThrowExceptionWhenProjectNotFound() {
      // Arrange
      when(projectRepo.findById(999L)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> projectService.getProjectById(999L))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Project not found");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException with project ID in message")
    void shouldIncludeProjectIdInErrorMessage() {
      // Arrange
      Long projectId = 123L;
      when(projectRepo.findById(projectId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> projectService.getProjectById(projectId))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("123");
    }
  }

  @Nested
  @DisplayName("getProjectsByWorkspaceId Tests")
  class GetProjectsByWorkspaceIdTests {

    @Test
    @DisplayName("Should return list of projects for valid workspace")
    void shouldReturnProjectsForValidWorkspace() {
      // Arrange
      Project project2 = new Project();
      project2.setId(2L);
      project2.setName("Project 2");
      project2.setWorkspace(testWorkspace);

      List<Project> projectList = List.of(testProject, project2);
      when(workspaceService.getWorkspace(1L)).thenReturn(testWorkspace);
      when(projectRepo.findByWorkspaceId(1L)).thenReturn(projectList);

      // Act
      List<Project> result = projectService.getProjectsByWorkspaceId(1L);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result).hasSize(2);
      assertThat(result).contains(testProject, project2);
      verify(workspaceService, times(1)).getWorkspace(1L);
      verify(projectRepo, times(1)).findByWorkspaceId(1L);
    }

    @Test
    @DisplayName("Should return empty list when workspace has no projects")
    void shouldReturnEmptyListWhenNoProjects() {
      // Arrange
      when(workspaceService.getWorkspace(1L)).thenReturn(testWorkspace);
      when(projectRepo.findByWorkspaceId(1L)).thenReturn(List.of());

      // Act
      List<Project> result = projectService.getProjectsByWorkspaceId(1L);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when workspace does not exist")
    void shouldThrowExceptionWhenWorkspaceNotFound() {
      // Arrange
      when(workspaceService.getWorkspace(999L)).thenReturn(null);

      // Act & Assert
      assertThatThrownBy(() -> projectService.getProjectsByWorkspaceId(999L))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Workspace not found");

      verify(projectRepo, never()).findByWorkspaceId(anyLong());
    }

    @Test
    @DisplayName("Should validate workspace before querying projects")
    void shouldValidateWorkspaceBeforeQuery() {
      // Arrange
      when(workspaceService.getWorkspace(1L)).thenReturn(null);

      // Act & Assert
      assertThatThrownBy(() -> projectService.getProjectsByWorkspaceId(1L))
          .isInstanceOf(IllegalArgumentException.class);

      verify(projectRepo, never()).findByWorkspaceId(1L);
    }
  }

  @Nested
  @DisplayName("updateProject Tests")
  class UpdateProjectTests {

    @Test
    @DisplayName("Should successfully update project when it exists")
    void shouldUpdateProjectWhenExists() {
      // Arrange
      testProject.setName("Updated Project");
      when(projectRepo.findById(1L)).thenReturn(Optional.of(testProject));
      when(projectRepo.save(testProject)).thenReturn(testProject);

      // Act
      Project result = projectService.updateProject(testProject);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getName()).isEqualTo("Updated Project");
      verify(projectRepo, times(1)).findById(1L);
      verify(projectRepo, times(1)).save(testProject);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when project not found")
    void shouldThrowExceptionWhenProjectNotFound() {
      // Arrange
      when(projectRepo.findById(999L)).thenReturn(Optional.empty());
      testProject.setId(999L);

      // Act & Assert
      assertThatThrownBy(() -> projectService.updateProject(testProject))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Project not found");
    }

    @Test
    @DisplayName("Should verify project exists before saving")
    void shouldVerifyProjectExistsBeforeSaving() {
      // Arrange
      when(projectRepo.findById(1L)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> projectService.updateProject(testProject))
          .isInstanceOf(IllegalArgumentException.class);

      verify(projectRepo, never()).save(any());
    }

    @Test
    @DisplayName("Should include project ID in error message")
    void shouldIncludeProjectIdInErrorMessage() {
      // Arrange
      Long projectId = 456L;
      testProject.setId(projectId);
      when(projectRepo.findById(projectId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> projectService.updateProject(testProject))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("456");
    }
  }

  @Nested
  @DisplayName("deleteProject Tests")
  class DeleteProjectTests {

    @Test
    @DisplayName("Should successfully delete project when it exists")
    void shouldDeleteProjectWhenExists() {
      // Arrange
      when(projectRepo.findById(1L)).thenReturn(Optional.of(testProject));

      // Act
      projectService.deleteProject(1L);

      // Assert
      verify(projectRepo, times(1)).findById(1L);
      verify(projectRepo, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when project not found")
    void shouldThrowExceptionWhenProjectNotFound() {
      // Arrange
      when(projectRepo.findById(999L)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> projectService.deleteProject(999L))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Project not found");
    }

    @Test
    @DisplayName("Should not call deleteById if project does not exist")
    void shouldNotDeleteIfProjectNotFound() {
      // Arrange
      when(projectRepo.findById(999L)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> projectService.deleteProject(999L))
          .isInstanceOf(IllegalArgumentException.class);

      verify(projectRepo, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should include project ID in error message")
    void shouldIncludeProjectIdInErrorMessage() {
      // Arrange
      Long projectId = 789L;
      when(projectRepo.findById(projectId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThatThrownBy(() -> projectService.deleteProject(projectId))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("789");
    }

    @Test
    @DisplayName("Should verify project before deletion")
    void shouldVerifyProjectBeforeDeletion() {
      // Arrange
      when(projectRepo.findById(1L)).thenReturn(Optional.of(testProject));

      // Act
      projectService.deleteProject(1L);

      // Assert
      verify(projectRepo).findById(1L);
      verify(projectRepo).deleteById(1L);
    }
  }
}