package com.teamboard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.teamboard.entity.BoardColumn;
import com.teamboard.entity.ColumnName;
import com.teamboard.entity.Priority;
import com.teamboard.entity.Project;
import com.teamboard.entity.Task;
import com.teamboard.entity.User;
import com.teamboard.entity.Workspace;
import com.teamboard.repository.TaskRepository;
import com.teamboard.service.BoardColumnService;
import com.teamboard.service.ProjectService;
import com.teamboard.service.TaskImp;
import com.teamboard.service.TaskService;
import com.teamboard.service.UserService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TaskImpTest {

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private ProjectService projectService;

  @Mock
  private BoardColumnService boardColumnService;

  @Mock
  private UserService userService;

  private TaskService taskService;
  private Project testProject;
  private User testUser;
  private Workspace testWorkspace;
  private BoardColumn backlogColumn;

  @BeforeEach
  void setUp() {
    taskService = new TaskImp(taskRepository, projectService, boardColumnService, userService);

    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setName("Test User");

    testWorkspace = Workspace.builder()
        .id(1L)
        .name("Test Workspace")
        .owner(testUser)
        .build();

    testProject = Project.builder()
        .id(1L)
        .name("Test Project")
        .workspace(testWorkspace)
        .createdBy(testUser)
        .build();

    backlogColumn = BoardColumn.builder()
        .id(1L)
        .name(ColumnName.BACKLOG)
        .position(1)
        .project(testProject)
        .build();
  }

  @Test
  void testCreateTask_Success() {
    Task newTask = Task.builder()
        .title("Test Task")
        .description("Test Description")
        .project(testProject)
        .createdBy(testUser)
        .build();

    when(projectService.getProjectById(1L)).thenReturn(testProject);
    when(boardColumnService.getColumnsByProjectId(1L)).thenReturn(Arrays.asList(backlogColumn));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
      Task task = invocation.getArgument(0);
      task.setId(1L);
      return task;
    });

    Task result = taskService.createTask(newTask);

    assertNotNull(result);
    assertEquals("Test Task", result.getTitle());
    assertEquals(1L, result.getColumn().getId());
    assertEquals(Priority.MEDIUM, result.getPriority());

    verify(projectService, times(1)).getProjectById(1L);
    verify(boardColumnService, times(1)).getColumnsByProjectId(1L);
    verify(taskRepository, times(1)).save(any(Task.class));
  }

  @Test
  void testCreateTask_ProjectNotFound() {
    Task newTask = Task.builder()
        .title("Test Task")
        .project(Project.builder().id(999L).build())
        .createdBy(testUser)
        .build();

    when(projectService.getProjectById(999L)).thenReturn(null);

    assertThrows(
        IllegalArgumentException.class,
        () -> taskService.createTask(newTask)
    );

    verify(taskRepository, never()).save(any(Task.class));
  }

  @Test
  void testCreateTask_TitleRequired() {
    Task newTask = Task.builder()
        .title("")
        .project(testProject)
        .createdBy(testUser)
        .build();

    when(projectService.getProjectById(1L)).thenReturn(testProject);
    when(boardColumnService.getColumnsByProjectId(1L)).thenReturn(Arrays.asList(backlogColumn));

    assertThrows(
        IllegalArgumentException.class,
        () -> taskService.createTask(newTask)
    );

    verify(taskRepository, never()).save(any(Task.class));
  }

  @Test
  void testGetTaskById_Success() {
    Task task = Task.builder()
        .id(1L)
        .title("Test Task")
        .project(testProject)
        .column(backlogColumn)
        .createdBy(testUser)
        .build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

    Task result = taskService.getTaskById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Test Task", result.getTitle());

    verify(taskRepository, times(1)).findById(1L);
  }

  @Test
  void testGetTaskById_NotFound() {
    when(taskRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> taskService.getTaskById(999L)
    );
  }

  @Test
  void testUpdateTask_Success() {
    Task existingTask = Task.builder()
        .id(1L)
        .title("Old Title")
        .description("Old Description")
        .project(testProject)
        .column(backlogColumn)
        .priority(Priority.LOW)
        .createdBy(testUser)
        .build();

    Task updateData = Task.builder()
        .id(1L)
        .title("New Title")
        .description("New Description")
        .priority(Priority.HIGH)
        .build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Task result = taskService.updateTask(updateData);

    assertEquals("New Title", result.getTitle());
    assertEquals("New Description", result.getDescription());
    assertEquals(Priority.HIGH, result.getPriority());

    verify(taskRepository, times(1)).save(any(Task.class));
  }

  @Test
  void testMoveTaskToColumn_Success() {
    BoardColumn todColumn = BoardColumn.builder()
        .id(2L)
        .name(ColumnName.TO_DO)
        .position(2)
        .project(testProject)
        .build();

    Task task = Task.builder()
        .id(1L)
        .title("Test Task")
        .project(testProject)
        .column(backlogColumn)
        .createdBy(testUser)
        .build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(boardColumnService.getColumnById(2L)).thenReturn(todColumn);
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Task result = taskService.moveTaskToColumn(1L, 2L);

    assertEquals(2L, result.getColumn().getId());

    verify(taskRepository, times(1)).save(any(Task.class));
  }

  @Test
  void testMoveTaskToColumn_SameColumn() {
    Task task = Task.builder()
        .id(1L)
        .title("Test Task")
        .project(testProject)
        .column(backlogColumn)
        .createdBy(testUser)
        .build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(boardColumnService.getColumnById(1L)).thenReturn(backlogColumn);

    Task result = taskService.moveTaskToColumn(1L, 1L);

    assertEquals(1L, result.getColumn().getId());

    verify(taskRepository, never()).save(any(Task.class));
  }

  @Test
  void testAssignTask_Success() {
    User assignedUser = new User();
    assignedUser.setId(2L);
    assignedUser.setName("Assigned User");

    Task task = Task.builder()
        .id(1L)
        .title("Test Task")
        .project(testProject)
        .column(backlogColumn)
        .createdBy(testUser)
        .build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(userService.getUser(2L)).thenReturn(assignedUser);
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Task result = taskService.assignTask(1L, 2L);

    assertNotNull(result.getAssignedTo());
    assertEquals(2L, result.getAssignedTo().getId());

    verify(taskRepository, times(1)).save(any(Task.class));
  }

  @Test
  void testDeleteTask_Success() {
    Task task = Task.builder()
        .id(1L)
        .title("Test Task")
        .project(testProject)
        .column(backlogColumn)
        .createdBy(testUser)
        .build();

    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    doNothing().when(taskRepository).deleteById(1L);

    taskService.deleteTask(1L);

    verify(taskRepository, times(1)).deleteById(1L);
  }

  @Test
  void testGetTasksByProject_Success() {
    Task task1 = Task.builder()
        .id(1L)
        .title("Task 1")
        .project(testProject)
        .column(backlogColumn)
        .createdBy(testUser)
        .build();

    Task task2 = Task.builder()
        .id(2L)
        .title("Task 2")
        .project(testProject)
        .column(backlogColumn)
        .createdBy(testUser)
        .build();

    when(projectService.getProjectById(1L)).thenReturn(testProject);
    when(taskRepository.findByProjectIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(task1, task2));

    List<Task> result = taskService.getTasksByProject(1L);

    assertEquals(2, result.size());
    assertEquals("Task 1", result.get(0).getTitle());
    assertEquals("Task 2", result.get(1).getTitle());

    verify(taskRepository, times(1)).findByProjectIdOrderByCreatedAtDesc(1L);
  }
}

