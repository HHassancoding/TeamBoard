package com.teamboard.controller;

import com.teamboard.DTO.TaskCreateRequestDTO;
import com.teamboard.DTO.TaskResponseDTO;
import com.teamboard.entity.Project;
import com.teamboard.entity.Task;
import com.teamboard.entity.User;
import com.teamboard.entity.Workspace;
import com.teamboard.entity.WorkspaceMember;
import com.teamboard.service.ProjectService;
import com.teamboard.service.TaskService;
import com.teamboard.service.UserService;
import com.teamboard.service.WorkspaceMemberService;
import com.teamboard.service.WorkspaceService;
import com.teamboard.util.JwtUtil;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TaskController {

  private final TaskService taskService;
  private final ProjectService projectService;
  private final WorkspaceMemberService workspaceMemberService;
  private final UserService userService;
  private final JwtUtil jwtUtil;
  private final com.teamboard.repository.BoardColumnRepository boardColumnRepository;

  public TaskController(TaskService taskService, ProjectService projectService,
      WorkspaceService workspaceService, WorkspaceMemberService workspaceMemberService,
      UserService userService, JwtUtil jwtUtil, com.teamboard.repository.BoardColumnRepository boardColumnRepository) {
    this.taskService = taskService;
    this.projectService = projectService;
    this.workspaceMemberService = workspaceMemberService;
    this.userService = userService;
    this.jwtUtil = jwtUtil;
    this.boardColumnRepository = boardColumnRepository;
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

  private Workspace validateProjectAccess(Long projectId, User currentUser) {
    Project project = projectService.getProjectById(projectId);
    if (project == null) {
      throw new IllegalArgumentException("Project not found");
    }

    Workspace workspace = project.getWorkspace();

    // ✅ CHECK IF USER IS WORKSPACE OWNER
    boolean isOwner = workspace.getOwner().getId().equals(currentUser.getId());

    // ✅ CHECK IF USER IS WORKSPACE MEMBER
    WorkspaceMember member = workspaceMemberService.getMember(currentUser.getId(), workspace.getId());
    boolean isMember = member != null;

    // ✅ ALLOW ACCESS IF OWNER OR MEMBER
    if (!isOwner && !isMember) {
      throw new IllegalArgumentException("You are not a member of this workspace");
    }

    return workspace;
  }

  @PostMapping("/projects/{projectId}/tasks")
  public ResponseEntity<?> createTask(
      @PathVariable Long projectId,
      @RequestHeader("Authorization") String bearerToken,
      @RequestBody TaskCreateRequestDTO taskRequestDTO) {
    try {
      User currentUser = validateAndGetUser(bearerToken);
      validateProjectAccess(projectId, currentUser);

      Project project = projectService.getProjectById(projectId);
      Task task = new Task();
      task.setTitle(taskRequestDTO.getTitle());
      task.setDescription(taskRequestDTO.getDescription());
      task.setProject(project);
      task.setPriority(taskRequestDTO.getPriority());
      task.setDueDate(taskRequestDTO.getDueDate());
      task.setCreatedBy(currentUser);

      if (taskRequestDTO.getAssignedToId() != null) {
        User assignedUser = userService.getUser(taskRequestDTO.getAssignedToId());
        if (assignedUser != null) {
          task.setAssignedTo(assignedUser);
        }
      }

      Task createdTask = taskService.createTask(task);
      TaskResponseDTO responseDTO = convertToResponseDTO(createdTask);
      return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

    } catch (IllegalArgumentException e) {
      String errorMsg = e.getMessage();

      // Authorization-related errors
      if (errorMsg != null && (errorMsg.contains("not a member") ||
          errorMsg.contains("don't have access") ||
          errorMsg.contains("Only the") ||
          errorMsg.contains("Cannot remove"))) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMsg);
      }

      // Resource not found errors
      if (errorMsg != null && (errorMsg.contains("not found") ||
          errorMsg.contains("Backlog column not found"))) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMsg);
      }

      // All other IllegalArgumentExceptions are validation errors
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(errorMsg != null ? errorMsg : "Invalid request");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while creating the task: " + e.getMessage());
    }
  }

  @GetMapping("/projects/{projectId}/tasks")
  public ResponseEntity<?> getTasksByProject(
      @PathVariable Long projectId,
      @RequestHeader("Authorization") String bearerToken) {
    try {
      User currentUser = validateAndGetUser(bearerToken);
      validateProjectAccess(projectId, currentUser);

      List<Task> tasks = taskService.getTasksByProject(projectId);
      List<TaskResponseDTO> responseDTOs = tasks.stream()
          .map(this::convertToResponseDTO)
          .toList();

      return ResponseEntity.ok(responseDTOs);

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while fetching tasks: " + e.getMessage());
    }
  }

  @GetMapping("/tasks/{taskId}")
  public ResponseEntity<?> getTask(
      @PathVariable Long taskId,
      @RequestHeader("Authorization") String bearerToken) {
    try {
      User currentUser = validateAndGetUser(bearerToken);
      Task task = taskService.getTaskById(taskId);
      validateProjectAccess(task.getProject().getId(), currentUser);

      TaskResponseDTO responseDTO = convertToResponseDTO(task);
      return ResponseEntity.ok(responseDTO);

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while fetching the task: " + e.getMessage());
    }
  }

  @PutMapping("/tasks/{taskId}")
  public ResponseEntity<?> updateTask(
      @PathVariable Long taskId,
      @RequestHeader("Authorization") String bearerToken,
      @RequestBody TaskCreateRequestDTO taskRequestDTO) {
    try {
      User currentUser = validateAndGetUser(bearerToken);
      Task existingTask = taskService.getTaskById(taskId);
      validateProjectAccess(existingTask.getProject().getId(), currentUser);

      existingTask.setTitle(taskRequestDTO.getTitle());
      existingTask.setDescription(taskRequestDTO.getDescription());
      existingTask.setPriority(taskRequestDTO.getPriority());
      existingTask.setDueDate(taskRequestDTO.getDueDate());

      if (taskRequestDTO.getAssignedToId() != null) {
        User assignedUser = userService.getUser(taskRequestDTO.getAssignedToId());
        existingTask.setAssignedTo(assignedUser);
      } else {
        existingTask.setAssignedTo(null);
      }

      Task updatedTask = taskService.updateTask(existingTask);
      TaskResponseDTO responseDTO = convertToResponseDTO(updatedTask);
      return ResponseEntity.ok(responseDTO);

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while updating the task: " + e.getMessage());
    }
  }

  @DeleteMapping("/tasks/{taskId}")
  public ResponseEntity<?> deleteTask(
      @PathVariable Long taskId,
      @RequestHeader("Authorization") String bearerToken) {
    try {
      User currentUser = validateAndGetUser(bearerToken);
      Task task = taskService.getTaskById(taskId);
      validateProjectAccess(task.getProject().getId(), currentUser);

      taskService.deleteTask(taskId);
      return ResponseEntity.ok("Task deleted successfully");

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while deleting the task: " + e.getMessage());
    }
  }

  @PatchMapping("/tasks/{taskId}/column/{columnId}")
  public ResponseEntity<?> moveTaskToColumn(
      @PathVariable Long taskId,
      @PathVariable Long columnId,
      @RequestHeader("Authorization") String bearerToken) {
    try {
      User currentUser = validateAndGetUser(bearerToken);
      Task task = taskService.getTaskById(taskId);
      validateProjectAccess(task.getProject().getId(), currentUser);

      Task movedTask = taskService.moveTaskToColumn(taskId, columnId);
      TaskResponseDTO responseDTO = convertToResponseDTO(movedTask);
      return ResponseEntity.ok(responseDTO);

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while moving the task: " + e.getMessage());
    }
  }

  @PostMapping("/workspaces/{workspaceId}/projects/{projectId}/tasks")
  public ResponseEntity<?> createTaskAlias(
      @PathVariable Long workspaceId,
      @PathVariable Long projectId,
      @RequestHeader("Authorization") String bearerToken,
      @RequestBody TaskCreateRequestDTO taskRequestDTO) {
    System.out.println("========================================");
    System.out.println("TASK CREATION REQUEST START");
    System.out.println("========================================");
    System.out.println("Endpoint: POST /api/workspaces/" + workspaceId + "/projects/" + projectId + "/tasks");
    System.out.println("Request DTO: " + taskRequestDTO);

    try {
      System.out.println("Step 1: Validating JWT token...");
      User currentUser = validateAndGetUser(bearerToken);
      System.out.println("✓ JWT Valid - User ID: " + currentUser.getId() + ", Email: " + currentUser.getEmail());

      System.out.println("Step 2: Fetching project with ID: " + projectId);
      Project project = projectService.getProjectById(projectId);
      if (project == null) {
        System.out.println("✗ ERROR: Project not found - ID: " + projectId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
      }
      System.out.println("✓ Project found - ID: " + project.getId() + ", Name: " + project.getName());

      System.out.println("Step 3: Validating project belongs to workspace...");
      System.out.println("  - URL workspaceId: " + workspaceId);
      System.out.println("  - Project's workspaceId: " + project.getWorkspace().getId());
      if (!project.getWorkspace().getId().equals(workspaceId)) {
        System.out.println("✗ ERROR: Project does not belong to workspace");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found in workspace");
      }
      System.out.println("✓ Project belongs to workspace");

      // Check if user is workspace owner OR workspace member
      Workspace workspace = project.getWorkspace();
      System.out.println("Step 4: Checking workspace access...");
      System.out.println("  - Workspace ID: " + workspace.getId());
      System.out.println("  - Workspace Name: " + workspace.getName());
      System.out.println("  - Workspace Owner ID: " + workspace.getOwner().getId());
      System.out.println("  - Current User ID: " + currentUser.getId());

      boolean isOwner = workspace.getOwner().getId().equals(currentUser.getId());
      System.out.println("  - Is Owner? " + isOwner);

      WorkspaceMember member = workspaceMemberService.getMember(currentUser.getId(), workspaceId);
      boolean isMember = member != null;
      System.out.println("  - Is Member? " + isMember);
      if (isMember && member != null) {
        System.out.println("  - Member Role: " + member.getRole());
      }

      if (!isOwner && !isMember) {
        System.out.println("✗ AUTHORIZATION FAILED: User is neither owner nor member");
        System.out.println("========================================");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not a member of this workspace");
      }
      System.out.println("✓ Authorization passed - User has access");

      System.out.println("Step 5: Fetching Backlog column for project...");
      java.util.List<com.teamboard.entity.BoardColumn> columns = boardColumnRepository.findByProjectIdOrderByPosition(projectId);
      if (columns == null || columns.isEmpty()) {
        System.out.println("✗ ERROR: Project has no columns");
        System.out.println("========================================");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Project has no columns. Please create columns first.");
      }

      // Get first column (Backlog or first column in order)
      com.teamboard.entity.BoardColumn backlogColumn = columns.get(0);
      System.out.println("✓ Column found - ID: " + backlogColumn.getId() + ", Name: " + backlogColumn.getName());

      System.out.println("Step 6: Creating task object...");
      Task task = new Task();
      task.setTitle(taskRequestDTO.getTitle());
      task.setDescription(taskRequestDTO.getDescription());
      task.setProject(project);
      task.setColumn(backlogColumn);
      task.setPriority(taskRequestDTO.getPriority());
      task.setDueDate(taskRequestDTO.getDueDate());
      task.setCreatedBy(currentUser);

      System.out.println("  - Title: " + taskRequestDTO.getTitle());
      System.out.println("  - Description: " + taskRequestDTO.getDescription());
      System.out.println("  - Priority: " + taskRequestDTO.getPriority());
      System.out.println("  - DueDate: " + taskRequestDTO.getDueDate());
      System.out.println("  - AssignedToId: " + taskRequestDTO.getAssignedToId());
      System.out.println("  - Column: " + backlogColumn.getName());

      if (taskRequestDTO.getAssignedToId() != null) {
        System.out.println("Step 7: Fetching assigned user...");
        User assignedUser = userService.getUser(taskRequestDTO.getAssignedToId());
        if (assignedUser != null) {
          task.setAssignedTo(assignedUser);
          System.out.println("✓ Assigned to user: " + assignedUser.getName() + " (ID: " + assignedUser.getId() + ")");
        } else {
          System.out.println("⚠ Warning: Assigned user not found - ID: " + taskRequestDTO.getAssignedToId());
        }
      }

      System.out.println("Step 8: Calling taskService.createTask()...");
      Task createdTask = taskService.createTask(task);
      System.out.println("✓ Task created successfully - ID: " + createdTask.getId());

      System.out.println("Step 9: Converting to response DTO...");
      TaskResponseDTO responseDTO = convertToResponseDTO(createdTask);
      System.out.println("✓ Response DTO created");
      System.out.println("========================================");
      System.out.println("TASK CREATION SUCCESS - Returning 201");
      System.out.println("========================================");
      return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);

    } catch (IllegalArgumentException e) {
      System.out.println("✗ EXCEPTION CAUGHT: IllegalArgumentException");
      System.out.println("  - Message: " + e.getMessage());
      System.out.println("  - Stack trace:");
      e.printStackTrace();
      System.out.println("========================================");
      String errorMsg = e.getMessage();

      // Authorization-related errors
      if (errorMsg != null && (errorMsg.contains("not a member") ||
          errorMsg.contains("don't have access") ||
          errorMsg.contains("Only the") ||
          errorMsg.contains("Cannot remove"))) {
        System.out.println("Returning 403 FORBIDDEN - Authorization error");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMsg);
      }

      // Resource not found errors
      if (errorMsg != null && (errorMsg.contains("not found") ||
          errorMsg.contains("Backlog column not found"))) {
        System.out.println("Returning 404 NOT FOUND - Resource missing");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMsg);
      }

      // All other IllegalArgumentExceptions are validation errors
      System.out.println("Returning 400 BAD REQUEST - Validation error");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(errorMsg != null ? errorMsg : "Invalid request");
    } catch (Exception e) {
      System.out.println("✗ EXCEPTION CAUGHT: " + e.getClass().getName());
      System.out.println("  - Message: " + e.getMessage());
      System.out.println("  - Stack trace:");
      e.printStackTrace();
      System.out.println("========================================");
      System.out.println("Returning 500 INTERNAL SERVER ERROR");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while creating the task: " + e.getMessage());
    }
  }

  @GetMapping("/workspaces/{workspaceId}/projects/{projectId}/tasks")
  public ResponseEntity<?> getTasksByProjectAlias(
      @PathVariable Long workspaceId,
      @PathVariable Long projectId,
      @RequestHeader("Authorization") String bearerToken) {
    try {
      User currentUser = validateAndGetUser(bearerToken);

      Project project = projectService.getProjectById(projectId);
      if (project == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
      }
      if (!project.getWorkspace().getId().equals(workspaceId)) {
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

      List<Task> tasks = taskService.getTasksByProject(projectId);
      List<TaskResponseDTO> responseDTOs = tasks.stream()
          .map(this::convertToResponseDTO)
          .toList();

      return ResponseEntity.ok(responseDTOs);

    } catch (IllegalArgumentException e) {
      String errorMsg = e.getMessage();

      // Authorization-related errors
      if (errorMsg != null && (errorMsg.contains("not a member") ||
          errorMsg.contains("don't have access"))) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMsg);
      }

      // Resource not found errors
      if (errorMsg != null && errorMsg.contains("not found")) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMsg);
      }

      // All other IllegalArgumentExceptions are validation errors
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(errorMsg != null ? errorMsg : "Invalid request");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while fetching tasks: " + e.getMessage());
    }
  }

  private TaskResponseDTO convertToResponseDTO(Task task) {
    return TaskResponseDTO.builder()
        .id(task.getId())
        .title(task.getTitle())
        .description(task.getDescription())
        .projectId(task.getProject().getId())
        .columnId(task.getColumn().getId())
        .assignedToId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null)
        .assignedToName(task.getAssignedTo() != null ? task.getAssignedTo().getName() : null)
        .assignedToInitials(task.getAssignedTo() != null ? task.getAssignedTo().getAvatarInitials() : null)
        .priority(task.getPriority())
        .dueDate(task.getDueDate())
        .createdById(task.getCreatedBy().getId())
        .createdByName(task.getCreatedBy().getName())
        .createdAt(task.getCreatedAt())
        .updatedAt(task.getUpdatedAt())
        .completedAt(task.getCompletedAt())
        .build();
  }
}

