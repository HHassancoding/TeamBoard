package com.teamboard.controller;

import com.teamboard.DTO.ProjectCreateRequestDTO;
import com.teamboard.DTO.ProjectResponseDTO;
import com.teamboard.entity.Project;
import com.teamboard.entity.User;
import com.teamboard.entity.Workspace;
import com.teamboard.entity.WorkspaceMember;
import com.teamboard.service.BoardColumnService;
import com.teamboard.service.ProjectService;
import com.teamboard.service.UserService;
import com.teamboard.service.WorkspaceMemberService;
import com.teamboard.service.WorkspaceService;
import com.teamboard.util.JwtUtil;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces")
@CrossOrigin(origins = "*")
public class ProjectController {

  private final ProjectService projectService;
  private final WorkspaceService workspaceService;
  private final JwtUtil jwtUtil;
  private final WorkspaceMemberService workspaceMemberService;
  private final UserService userService;
  private final BoardColumnService boardColumnService;

  public ProjectController(WorkspaceService workspaceService, JwtUtil jwtUtil,
                           WorkspaceMemberService workspaceMemberService, UserService userService,
                           ProjectService projectService, BoardColumnService boardColumnService) {
    this.workspaceService = workspaceService;
    this.jwtUtil = jwtUtil;
    this.workspaceMemberService = workspaceMemberService;
    this.userService = userService;
    this.projectService = projectService;
    this.boardColumnService = boardColumnService;
  }

  /**
   * Validates JWT token and returns the current user
   * @param bearerToken Authorization header with format "Bearer {token}"
   * @return User object if valid
   * @throws IllegalArgumentException if token is invalid or user not found
   */
  private User validateAndGetUser(String bearerToken) throws Exception {
    if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
      throw new IllegalArgumentException("Invalid authorization header");
    }

    String token = bearerToken.substring(7);
    String email = jwtUtil.extractUsername(token);
    User currentUser = userService.findByEmail(email);

    if (currentUser == null) {
      throw new IllegalArgumentException("Invalid user");
    }

    return currentUser;
  }

  /**
   * Validates that user has access to workspace
   * @param workspaceId ID of the workspace
   * @param currentUser Current authenticated user
   * @return Workspace object if user is a member
   * @throws IllegalArgumentException if workspace doesn't exist or user isn't a member
   */
  private Workspace validateWorkspaceAccess(Long workspaceId, User currentUser) throws Exception {
    Workspace workspace = workspaceService.getWorkspace(workspaceId);
    if (workspace == null) {
      throw new IllegalArgumentException("Workspace not found");
    }

    WorkspaceMember member = workspaceMemberService.getMember(currentUser.getId(), workspace.getId());
    if (member == null) {
      throw new IllegalArgumentException("You are not a member of this workspace");
    }

    return workspace;
  }

  /**
   * Validates that user is the creator of the project
   * @param projectId ID of the project
   * @param currentUser Current authenticated user
   * @return Project object if user is the creator
   * @throws IllegalArgumentException if project doesn't exist or user isn't creator
   */
  private Project validateProjectOwnership(Long projectId, User currentUser) throws Exception {
    Project project = projectService.getProjectById(projectId);
    if (project == null) {
      throw new IllegalArgumentException("Project not found");
    }

    if (!project.getCreatedBy().getId().equals(currentUser.getId())) {
      throw new IllegalArgumentException("Only the project creator can perform this action");
    }

    return project;
  }

  @PostMapping("/{workspaceId}/projects")
  public ResponseEntity<?> createProject(
      @PathVariable Long workspaceId,
      @RequestHeader("Authorization") String bearerToken,
      @RequestBody ProjectCreateRequestDTO projectRequestDTO) throws Exception {
    try {
      User currentUser = validateAndGetUser(bearerToken);
      Workspace workspace = validateWorkspaceAccess(workspaceId, currentUser);

      Project project = new Project();
      project.setName(projectRequestDTO.getName());
      project.setDescription(projectRequestDTO.getDescription());
      project.setWorkspace(workspace);
      project.setCreatedBy(currentUser);

      Project createdProject = projectService.createProject(project);

      // Auto-create 4 default columns for the project
      boardColumnService.createDefaultColumns(createdProject.getId());

      ProjectResponseDTO responseDTO = convertToResponseDTO(createdProject);
      return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while creating the project: " + e.getMessage());
    }
  }

  @GetMapping("/{workspaceId}/projects")
  public ResponseEntity<?> getProjects(
      @PathVariable Long workspaceId,
      @RequestHeader("Authorization") String bearerToken) throws Exception {
    try {
      User currentUser = validateAndGetUser(bearerToken);
      Workspace workspace = validateWorkspaceAccess(workspaceId, currentUser);

      List<Project> projects = projectService.getProjectsByWorkspaceId(workspaceId);
      List<ProjectResponseDTO> responseDTOs = projects.stream()
          .map(this::convertToResponseDTO)
          .toList();
      return ResponseEntity.ok(responseDTOs);

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while retrieving projects: " + e.getMessage());
    }
  }

  @GetMapping("/{workspaceId}/projects/{projectId}")
  public ResponseEntity<?> getProject
      (
          @PathVariable Long projectId,
          @RequestHeader("Authorization") String bearerToken,
          @PathVariable Long workspaceId
      )throws Exception
  {
    try {
      User currentUser = validateAndGetUser(bearerToken);
      Workspace workspace = validateWorkspaceAccess(workspaceId, currentUser);
      Project project = validateProjectOwnership(projectId, currentUser);

      ProjectResponseDTO responseDTO = convertToResponseDTO(project);
      return ResponseEntity.ok(responseDTO);

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while retrieving the project: " + e.getMessage());
    }
  }

  @DeleteMapping("/{workspaceId}/projects/{projectId}")
  public ResponseEntity<?> deleteProject
      (
      @PathVariable Long projectId,
      @RequestHeader("Authorization") String bearerToken,
      @PathVariable Long workspaceId
      ) throws Exception {
    try {
      User currentUser = validateAndGetUser(bearerToken);
      Workspace workspace = validateWorkspaceAccess(workspaceId, currentUser);
      Project project = validateProjectOwnership(projectId, currentUser);
      projectService.deleteProject(projectId);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while deleting the project: " + e.getMessage());
    }
  }

  @PutMapping("/{workspaceId}/projects/{projectId}")
  public ResponseEntity<?> updateProject
      (
      @PathVariable Long projectId,
      @RequestHeader("Authorization") String bearerToken,
      @RequestBody ProjectCreateRequestDTO projectRequestDTO,
      @PathVariable Long workspaceId
      ) throws Exception {
    try {
      User currentUser = validateAndGetUser(bearerToken);
      Project existingProject = validateProjectOwnership(projectId, currentUser);
      existingProject.setName(projectRequestDTO.getName());
      existingProject.setDescription(projectRequestDTO.getDescription());
      Project updatedProject = projectService.updateProject(existingProject);
      ProjectResponseDTO responseDTO = convertToResponseDTO(updatedProject);
      return ResponseEntity.ok(responseDTO);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while updating the project: " + e.getMessage());
    }
  }

  private ProjectResponseDTO convertToResponseDTO(Project project) {
    return new ProjectResponseDTO(
        project.getId(),
        project.getName(),
        project.getDescription(),
        project.getWorkspace().getId(),
        project.getCreatedBy().getId(),
        project.getCreatedBy().getName(),
        project.getCreatedAt(),
        project.getUpdatedAt());
  }


}
