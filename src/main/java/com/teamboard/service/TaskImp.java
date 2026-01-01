package com.teamboard.service;

import com.teamboard.entity.BoardColumn;
import com.teamboard.entity.ColumnName;
import com.teamboard.entity.Priority;
import com.teamboard.entity.Project;
import com.teamboard.entity.Task;
import com.teamboard.entity.User;
import com.teamboard.repository.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskImp implements TaskService {

  private final TaskRepository taskRepository;
  private final ProjectService projectService;
  private final BoardColumnService boardColumnService;
  private final UserService userService;

  public TaskImp(TaskRepository taskRepository, ProjectService projectService,
      BoardColumnService boardColumnService, UserService userService) {
    this.taskRepository = taskRepository;
    this.projectService = projectService;
    this.boardColumnService = boardColumnService;
    this.userService = userService;
  }

  @Override
  public Task createTask(Task task) {
    // Validate project exists
    Project project = projectService.getProjectById(task.getProject().getId());
    if (project == null) {
      throw new IllegalArgumentException("Project not found with id: " + task.getProject().getId());
    }

    // Auto-assign to Backlog column
    List<BoardColumn> backlogColumns = boardColumnService.getColumnsByProjectId(project.getId());
    BoardColumn backlogColumn = backlogColumns.stream()
        .filter(col -> col.getName() == ColumnName.BACKLOG)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Backlog column not found for project: " + project.getId()));

    task.setColumn(backlogColumn);

    // Set default priority if not specified
    if (task.getPriority() == null) {
      task.setPriority(Priority.MEDIUM);
    }

    // Validate title is not null or empty
    if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("Task title is required");
    }

    return taskRepository.save(task);
  }

  @Override
  public Task getTaskById(Long taskId) {
    return taskRepository.findById(taskId)
        .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));
  }

  @Override
  public List<Task> getTasksByProject(Long projectId) {
    Project project = projectService.getProjectById(projectId);
    if (project == null) {
      throw new IllegalArgumentException("Project not found with id: " + projectId);
    }
    return taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
  }

  @Override
  public List<Task> getTasksByColumn(Long columnId) {
    BoardColumn column = boardColumnService.getColumnById(columnId);
    if (column == null) {
      throw new IllegalArgumentException("Column not found with id: " + columnId);
    }
    return taskRepository.findByColumnIdOrderByCreatedAtDesc(columnId);
  }

  @Override
  public Task updateTask(Task task) {
    Task existingTask = getTaskById(task.getId());

    // Validate title is not null or empty
    if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("Task title is required");
    }

    // Update allowed fields only
    existingTask.setTitle(task.getTitle());
    existingTask.setDescription(task.getDescription());
    existingTask.setPriority(task.getPriority() != null ? task.getPriority() : Priority.MEDIUM);
    existingTask.setDueDate(task.getDueDate());
    existingTask.setAssignedTo(task.getAssignedTo());

    // Do NOT allow changing project_id or column_id (use move endpoint)
    // updated_at is automatically set by @UpdateTimestamp

    return taskRepository.save(existingTask);
  }

  @Override
  public void deleteTask(Long taskId) {
    Task task = getTaskById(taskId);
    taskRepository.deleteById(taskId);
  }

  @Override
  public Task moveTaskToColumn(Long taskId, Long newColumnId) {
    Task task = getTaskById(taskId);
    BoardColumn newColumn = boardColumnService.getColumnById(newColumnId);

    if (newColumn == null) {
      throw new IllegalArgumentException("Column not found with id: " + newColumnId);
    }

    // Validate column belongs to same project
    if (!task.getProject().getId().equals(newColumn.getProject().getId())) {
      throw new IllegalArgumentException("Column does not belong to task's project");
    }

    // Prevent duplicate moves
    if (task.getColumn().getId().equals(newColumnId)) {
      return task; // Already in target column, no change needed
    }

    task.setColumn(newColumn);
    // updated_at is automatically set by @UpdateTimestamp

    return taskRepository.save(task);
  }

  @Override
  public Task assignTask(Long taskId, Long userId) {
    Task task = getTaskById(taskId);

    if (userId != null) {
      User user = userService.getUser(userId);
      if (user == null) {
        throw new IllegalArgumentException("User not found with id: " + userId);
      }
      task.setAssignedTo(user);
    } else {
      task.setAssignedTo(null); // Unassign
    }

    // updated_at is automatically set by @UpdateTimestamp
    return taskRepository.save(task);
  }
}

