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
  private final WorkspaceService workspaceService;
  private final WorkspaceMemberService workspaceMemberService;
  private final UserService userService;
  private final JwtUtil jwtUtil;

  public TaskController(TaskService taskService, ProjectService projectService,
      WorkspaceService workspaceService, WorkspaceMemberService workspaceMemberService,
      UserService userService, JwtUtil jwtUtil) {
    this.taskService = taskService;
    this.projectService = projectService;
    this.workspaceService = workspaceService;
    this.workspaceMemberService = workspaceMemberService;
    this.userService = userService;
    this.jwtUtil = jwtUtil;
  }

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

  private Workspace validateProjectAccess(Long projectId, User currentUser) throws Exception {
    Project project = projectService.getProjectById(projectId);
    if (project == null) {
      throw new IllegalArgumentException("Project not found");
    }

    Workspace workspace = project.getWorkspace();
    WorkspaceMember member = workspaceMemberService.getMember(currentUser.getId(), workspace.getId());
    if (member == null) {
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
      Workspace workspace = validateProjectAccess(projectId, currentUser);

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
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
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
      Workspace workspace = validateProjectAccess(projectId, currentUser);

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
      Workspace workspace = validateProjectAccess(task.getProject().getId(), currentUser);

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
      Workspace workspace = validateProjectAccess(existingTask.getProject().getId(), currentUser);

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
      Workspace workspace = validateProjectAccess(task.getProject().getId(), currentUser);

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
      Workspace workspace = validateProjectAccess(task.getProject().getId(), currentUser);

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

