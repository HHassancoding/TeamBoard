package com.teamboard.controller;

import com.teamboard.DTO.BoardColumnResponseDTO;
import com.teamboard.entity.BoardColumn;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class BoardColumnController {

  private final BoardColumnService boardColumnService;
  private final ProjectService projectService;
  private final WorkspaceService workspaceService;
  private final WorkspaceMemberService workspaceMemberService;
  private final UserService userService;
  private final JwtUtil jwtUtil;

  public BoardColumnController(
      BoardColumnService boardColumnService,
      ProjectService projectService,
      WorkspaceService workspaceService,
      WorkspaceMemberService workspaceMemberService,
      UserService userService,
      JwtUtil jwtUtil) {
    this.boardColumnService = boardColumnService;
    this.projectService = projectService;
    this.workspaceService = workspaceService;
    this.workspaceMemberService = workspaceMemberService;
    this.userService = userService;
    this.jwtUtil = jwtUtil;
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
   * Validates that user has access to project's workspace
   * @param projectId ID of the project
   * @param currentUser Current authenticated user
   * @return Project object if user is a member of the workspace
   * @throws IllegalArgumentException if project doesn't exist or user isn't a workspace member
   */
  private Project validateProjectAccess(Long projectId, User currentUser) throws Exception {
    Project project = projectService.getProjectById(projectId);
    if (project == null) {
      throw new IllegalArgumentException("Project not found");
    }

    Workspace workspace = project.getWorkspace();
    WorkspaceMember member = workspaceMemberService.getMember(currentUser.getId(), workspace.getId());
    if (member == null) {
      throw new IllegalArgumentException("You are not a member of this workspace");
    }

    return project;
  }

  /**
   * Get all columns for a project
   * @param projectId ID of the project
   * @param bearerToken JWT token
   * @return List of columns ordered by position
   */
  @GetMapping("/{projectId}/columns")
  public ResponseEntity<?> getColumns(
      @PathVariable Long projectId,
      @RequestHeader("Authorization") String bearerToken) {
    try {
      User currentUser = validateAndGetUser(bearerToken);
      Project project = validateProjectAccess(projectId, currentUser);

      List<BoardColumn> columns = boardColumnService.getColumnsByProjectId(projectId);
      List<BoardColumnResponseDTO> responseDTOs = columns.stream()
          .map(this::convertToResponseDTO)
          .toList();

      return ResponseEntity.ok(responseDTOs);

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while fetching columns: " + e.getMessage());
    }
  }

  /**
   * Convert BoardColumn entity to DTO
   */
  private BoardColumnResponseDTO convertToResponseDTO(BoardColumn column) {
    return BoardColumnResponseDTO.builder()
        .id(column.getId())
        .name(column.getName())
        .position(column.getPosition())
        .projectId(column.getProject().getId())
        .createdAt(column.getCreatedAt())
        .build();
  }
}

