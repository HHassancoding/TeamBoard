package com.teamboard.controller;

import com.teamboard.DTO.WorkspaceCreateRequestDTO;
import com.teamboard.DTO.WorkspaceMemberRequestDTO;
import com.teamboard.DTO.WorkspaceMemberResponseDTO;
import com.teamboard.DTO.WorkspaceResponseDTO;
import com.teamboard.entity.MemberRole;
import com.teamboard.entity.User;
import com.teamboard.entity.Workspace;
import com.teamboard.entity.WorkspaceMember;
import com.teamboard.service.WorkspaceService;
import com.teamboard.service.WorkspaceMemberService;
import com.teamboard.service.UserImp;
import com.teamboard.util.JwtUtil;
import java.util.List;
import java.util.stream.Collectors;
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
public class WorkspaceController {
  private final WorkspaceService workspaceService;
  private final WorkspaceMemberService workspaceMemberService;
  private final JwtUtil jwtUtil;
  private final UserImp userImp;

  public WorkspaceController(
      WorkspaceService workspaceService,
      WorkspaceMemberService workspaceMemberService,
      JwtUtil jwtUtil,
      UserImp userImp) {
    this.workspaceService = workspaceService;
    this.workspaceMemberService = workspaceMemberService;
    this.jwtUtil = jwtUtil;
    this.userImp = userImp;
  }

  /**
   * Create a new workspace. Current user is auto-set as owner.
   * POST /api/workspaces
   */
  @PostMapping
  public ResponseEntity<?> createWorkspace(
      @RequestHeader("Authorization") String bearerToken,
      @RequestBody WorkspaceCreateRequestDTO requestDTO) {
    try {
      // Extract JWT token
      String token = bearerToken.substring(7);
      String email = jwtUtil.extractUsername(token);
      User owner = userImp.findByEmail(email);

      if (owner == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
      }

      // Validate request
      if (requestDTO.getName() == null || requestDTO.getName().trim().isEmpty()) {
        return ResponseEntity.badRequest().body("Workspace name is required");
      }

      // Check for duplicate workspace name for this owner
      Workspace existing = workspaceService.findByOwnerIdAndName(owner.getId(), requestDTO.getName());
      if (existing != null) {
        return ResponseEntity.badRequest().body("Workspace with this name already exists");
      }

      // Create workspace with owner
      Workspace workspace = new Workspace();
      workspace.setName(requestDTO.getName());
      workspace.setDescription(requestDTO.getDescription());
      workspace.setOwner(owner);

      Workspace savedWorkspace = workspaceService.createWorkspace(workspace);
      WorkspaceResponseDTO responseDTO = convertToResponseDTO(savedWorkspace);

      return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to create workspace: " + e.getMessage());
    }
  }

  /**
   * Get all workspaces (basic list; later will filter by membership).
   * GET /api/workspaces
   */
  @GetMapping
  public ResponseEntity<?> getAllWorkspaces() {
    try {
      List<Workspace> workspaces = workspaceService.getAllWorkspaces();
      List<WorkspaceResponseDTO> responseDTOs =
          workspaces.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
      return ResponseEntity.ok(responseDTOs);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to fetch workspaces: " + e.getMessage());
    }
  }

  /**
   * Get single workspace by ID.
   * GET /api/workspaces/{id}
   */
  @GetMapping("/{id}")
  public ResponseEntity<?> getWorkspace(@PathVariable Long id) {
    try {
      Workspace workspace = workspaceService.getWorkspace(id);
      if (workspace == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workspace not found");
      }
      WorkspaceResponseDTO responseDTO = convertToResponseDTO(workspace);
      return ResponseEntity.ok(responseDTO);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to fetch workspace: " + e.getMessage());
    }
  }

  /**
   * Update workspace (owner-only). Updates name and description.
   * PUT /api/workspaces/{id}
   */
  @PutMapping("/{id}")
  public ResponseEntity<?> updateWorkspace(
      @PathVariable Long id,
      @RequestHeader("Authorization") String bearerToken,
      @RequestBody WorkspaceCreateRequestDTO requestDTO) {
    try {
      // Extract JWT token and resolve current user
      String token = bearerToken.substring(7);
      String email = jwtUtil.extractUsername(token);
      User currentUser = userImp.findByEmail(email);

      if (currentUser == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
      }

      // Get workspace
      Workspace workspace = workspaceService.getWorkspace(id);
      if (workspace == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workspace not found");
      }

      // Check ownership
      if (!workspace.getOwner().getId().equals(currentUser.getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body("Only workspace owner can update it");
      }

      // Validate request
      if (requestDTO.getName() == null || requestDTO.getName().trim().isEmpty()) {
        return ResponseEntity.badRequest().body("Workspace name is required");
      }

      // Update workspace
      workspace.setName(requestDTO.getName());
      workspace.setDescription(requestDTO.getDescription());

      Workspace updatedWorkspace = workspaceService.updateWorkspace(workspace);
      WorkspaceResponseDTO responseDTO = convertToResponseDTO(updatedWorkspace);

      return ResponseEntity.ok(responseDTO);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to update workspace: " + e.getMessage());
    }
  }

  /**
   * Delete workspace (owner-only).
   * DELETE /api/workspaces/{id}
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteWorkspace(
      @PathVariable Long id, @RequestHeader("Authorization") String bearerToken) {
    try {
      // Extract JWT token and resolve current user
      String token = bearerToken.substring(7);
      String email = jwtUtil.extractUsername(token);
      User currentUser = userImp.findByEmail(email);

      if (currentUser == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
      }

      // Get workspace
      Workspace workspace = workspaceService.getWorkspace(id);
      if (workspace == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workspace not found");
      }

      // Check ownership
      if (!workspace.getOwner().getId().equals(currentUser.getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body("Only workspace owner can delete it");
      }

      // Delete workspace
      workspaceService.deleteWorkspace(id);

      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to delete workspace: " + e.getMessage());
    }
  }

  /**
   * Get workspaces owned by a specific user (for admin/debugging).
   * GET /api/workspaces/owner/{ownerId}
   */
  @GetMapping("/owner/{ownerId}")
  public ResponseEntity<?> getWorkspacesByOwner(@PathVariable Long ownerId) {
    try {
      List<Workspace> workspaces = workspaceService.getWorkspacesByOwner(ownerId);
      List<WorkspaceResponseDTO> responseDTOs =
          workspaces.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
      return ResponseEntity.ok(responseDTOs);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to fetch workspaces: " + e.getMessage());
    }
  }

  /**
   * Add a member to workspace.
   * POST /api/workspaces/{workspaceId}/members
   *
   * @param workspaceId ID of the workspace to add member to
   * @param bearerToken JWT token of the workspace owner
   * @param requestDTO contains userId (ID of user to add) and role (ADMIN, MEMBER, VIEWER)
   * @return WorkspaceMemberResponseDTO with member details
   */
  @PostMapping("/{workspaceId}/members")
  public ResponseEntity<?> addMemberToWorkspace(
      @PathVariable Long workspaceId,
      @RequestHeader("Authorization") String bearerToken,
      @RequestBody WorkspaceMemberRequestDTO requestDTO) {
    try {
      // Extract JWT token and resolve current user
      String token = bearerToken.substring(7);
      String email = jwtUtil.extractUsername(token);
      User currentUser = userImp.findByEmail(email);

      if (currentUser == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
      }

      // Get workspace
      Workspace workspace = workspaceService.getWorkspace(workspaceId);
      if (workspace == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workspace not found");
      }

      // Check if current user is owner
      if (!workspace.getOwner().getId().equals(currentUser.getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body("Only workspace owner can add members");
      }

      // Validate request
      if (requestDTO.getUserId() == null) {
        return ResponseEntity.badRequest().body("User ID is required");
      }

      // Determine role
      MemberRole role = MemberRole.MEMBER;
      if (requestDTO.getRole() != null && !requestDTO.getRole().trim().isEmpty()) {
        try {
          role = MemberRole.valueOf(requestDTO.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
          return ResponseEntity.badRequest()
              .body("Invalid role. Must be one of: ADMIN, MEMBER, VIEWER");
        }
      }

      // Add member
      WorkspaceMember member =
          workspaceMemberService.addMember(requestDTO.getUserId(), workspaceId, role);
      WorkspaceMemberResponseDTO responseDTO = convertToMemberResponseDTO(member);

      return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to add member: " + e.getMessage());
    }
  }

  /**
   * Remove a member from workspace.
   * DELETE /api/workspaces/{workspaceId}/members/{userId}
   *
   * @param workspaceId ID of the workspace to remove member from
   * @param userId ID of the user to remove from workspace
   * @param bearerToken JWT token of the workspace owner
   */
  @DeleteMapping("/{workspaceId}/members/{userId}")
  public ResponseEntity<?> removeMemberFromWorkspace(
      @PathVariable Long workspaceId,
      @PathVariable Long userId,
      @RequestHeader("Authorization") String bearerToken) {
    try {
      // Extract JWT token and resolve current user
      String token = bearerToken.substring(7);
      String email = jwtUtil.extractUsername(token);
      User currentUser = userImp.findByEmail(email);

      if (currentUser == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
      }

      // Get workspace
      Workspace workspace = workspaceService.getWorkspace(workspaceId);
      if (workspace == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workspace not found");
      }

      // Check if current user is owner
      if (!workspace.getOwner().getId().equals(currentUser.getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body("Only workspace owner can remove members");
      }

      // Remove member
      workspaceMemberService.removeMember(userId, workspaceId);

      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to remove member: " + e.getMessage());
    }
  }

  /**
   * List all members of a workspace.
   * GET /api/workspaces/{workspaceId}/members
   *
   * @param workspaceId ID of the workspace to get members from
   * @return List of WorkspaceMemberResponseDTO
   */
  @GetMapping("/{workspaceId}/members")
  public ResponseEntity<?> getWorkspaceMembers(@PathVariable Long workspaceId) {
    try {
      // Validate workspace exists
      Workspace workspace = workspaceService.getWorkspace(workspaceId);
      if (workspace == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workspace not found");
      }

      // Get members
      List<WorkspaceMember> members = workspaceMemberService.getMembersOfWorkspace(workspaceId);
      List<WorkspaceMemberResponseDTO> responseDTOs =
          members.stream()
              .map(this::convertToMemberResponseDTO)
              .collect(Collectors.toList());

      return ResponseEntity.ok(responseDTOs);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to fetch members: " + e.getMessage());
    }
  }

  /**
   * Helper method to convert Workspace entity to WorkspaceResponseDTO.
   */
  private WorkspaceResponseDTO convertToResponseDTO(Workspace workspace) {
    WorkspaceResponseDTO dto = new WorkspaceResponseDTO();
    dto.setId(workspace.getId());
    dto.setName(workspace.getName());
    dto.setDescription(workspace.getDescription());
    dto.setOwnerId(workspace.getOwner().getId());
    dto.setOwnerName(workspace.getOwner().getName());
    dto.setOwnerEmail(workspace.getOwner().getEmail());
    dto.setCreatedAt(workspace.getCreatedAt());
    dto.setUpdatedAt(workspace.getUpdatedAt());
    return dto;
  }

  /**
   * Helper method to convert WorkspaceMember entity to WorkspaceMemberResponseDTO.
   */
  private WorkspaceMemberResponseDTO convertToMemberResponseDTO(WorkspaceMember member) {
    WorkspaceMemberResponseDTO dto = new WorkspaceMemberResponseDTO();
    dto.setId(member.getId());
    dto.setUserId(member.getUser().getId());
    dto.setUserEmail(member.getUser().getEmail());
    dto.setUserName(member.getUser().getName());
    dto.setRole(member.getRole().name());
    dto.setJoinedAt(member.getJoinedAt());
    dto.setUpdatedAt(member.getUpdatedAt());
    return dto;
  }
}

