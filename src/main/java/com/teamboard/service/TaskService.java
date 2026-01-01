package com.teamboard.service;

import com.teamboard.entity.Task;
import java.util.List;

public interface TaskService {
  /**
   * Creates a new task in the Backlog column of the project.
   * @param task the task to create
   * @return created task
   */
  Task createTask(Task task);

  /**
   * Gets a task by ID.
   * @param taskId the task ID
   * @return the task
   */
  Task getTaskById(Long taskId);

  /**
   * Gets all tasks for a project, ordered by creation date.
   * @param projectId the project ID
   * @return list of tasks
   */
  List<Task> getTasksByProject(Long projectId);

  /**
   * Gets all tasks in a specific column.
   * @param columnId the column ID
   * @return list of tasks
   */
  List<Task> getTasksByColumn(Long columnId);

  /**
   * Updates a task (title, description, assignee, priority, due date).
   * @param task the updated task
   * @return updated task
   */
  Task updateTask(Task task);

  /**
   * Deletes a task.
   * @param taskId the task ID
   */
  void deleteTask(Long taskId);

  /**
   * Moves a task to a different column.
   * @param taskId the task ID
   * @param newColumnId the new column ID
   * @return updated task
   */
  Task moveTaskToColumn(Long taskId, Long newColumnId);

  /**
   * Assigns a task to a user.
   * @param taskId the task ID
   * @param userId the user ID (can be null to unassign)
   * @return updated task
   */
  Task assignTask(Long taskId, Long userId);
}

