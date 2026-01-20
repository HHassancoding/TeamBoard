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
import com.teamboard.util.JwtUtil;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Alias controller to support workspace-scoped routes that many frontends use when navigating
 * from a workspace -> project -> board flow. Delegates to the same services as BoardColumnController.
 */
@RestController
@RequestMapping("/api/workspaces")
public class BoardColumnAliasController {

  private final BoardColumnService boardColumnService;
  private final ProjectService projectService;
  private final WorkspaceMemberService workspaceMemberService;
  private final UserService userService;
  private final JwtUtil jwtUtil;

  public BoardColumnAliasController(
      BoardColumnService boardColumnService,
      ProjectService projectService,
      WorkspaceMemberService workspaceMemberService,
      UserService userService,
      JwtUtil jwtUtil
  ) {
    this.boardColumnService = boardColumnService;
    this.projectService = projectService;
    this.workspaceMemberService = workspaceMemberService;
    this.userService = userService;
    this.jwtUtil = jwtUtil;
  }

  private User validateAndGetUser(String bearerToken) {
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

  @GetMapping("/{workspaceId}/projects/{projectId}/columns")
  public ResponseEntity<?> getColumnsAlias(
      @PathVariable Long workspaceId,
      @PathVariable Long projectId,
      @RequestHeader("Authorization") String bearerToken
  ) {
    try {
      User currentUser = validateAndGetUser(bearerToken);

      Project project = projectService.getProjectById(projectId);
      if (project == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
      }
      if (!project.getWorkspace().getId().equals(workspaceId)) {
        // Path workspaceId doesn't match the project's workspace — treat as 404 to avoid leaking info
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found in workspace");
      }

      // Check if user is workspace owner OR workspace member
      Workspace workspace = project.getWorkspace();
      boolean isOwner = workspace.getOwner().getId().equals(currentUser.getId());
      WorkspaceMember member = workspaceMemberService.getMember(currentUser.getId(), workspaceId);
      boolean isMember = member != null;

      if (!isOwner && !isMember) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not a member of this workspace");
      }

      List<BoardColumn> columns = boardColumnService.getColumnsByProjectId(projectId);
      List<BoardColumnResponseDTO> responseDTOs = columns.stream()
          .map(this::convertToResponseDTO)
          .toList();

      return ResponseEntity.ok(responseDTOs);
    } catch (IllegalArgumentException e) {
      String msg = e.getMessage() != null ? e.getMessage() : "Invalid request";
      HttpStatus status = msg.toLowerCase().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.FORBIDDEN;
      return ResponseEntity.status(status).body(msg);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while fetching columns: " + e.getMessage());
    }
  }

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
