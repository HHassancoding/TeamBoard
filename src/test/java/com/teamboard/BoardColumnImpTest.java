package com.teamboard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.teamboard.entity.BoardColumn;
import com.teamboard.entity.ColumnName;
import com.teamboard.entity.Project;
import com.teamboard.entity.User;
import com.teamboard.entity.Workspace;
import com.teamboard.repository.BoardColumnRepository;
import com.teamboard.repository.ProjectRepo;
import com.teamboard.service.BoardColumnImp;
import com.teamboard.service.BoardColumnService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BoardColumnImpTest {

  @Mock
  private BoardColumnRepository boardColumnRepository;

  @Mock
  private ProjectRepo projectRepo;

  private BoardColumnService boardColumnService;
  private Project testProject;
  private User testUser;
  private Workspace testWorkspace;

  @BeforeEach
  void setUp() {
    boardColumnService = new BoardColumnImp(boardColumnRepository, projectRepo);

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
  }

  @Test
  void testCreateDefaultColumns_Success() {
    when(projectRepo.findById(1L)).thenReturn(Optional.of(testProject));
    when(boardColumnRepository.save(any(BoardColumn.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    boardColumnService.createDefaultColumns(1L);

    verify(projectRepo, times(1)).findById(1L);
    verify(boardColumnRepository, times(4)).save(any(BoardColumn.class));
  }

  @Test
  void testCreateDefaultColumns_ProjectNotFound() {
    when(projectRepo.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> boardColumnService.createDefaultColumns(999L),
        "Project not found with id: 999"
    );

    verify(boardColumnRepository, never()).save(any(BoardColumn.class));
  }

  @Test
  void testGetColumnsByProjectId_Success() {
    List<BoardColumn> mockColumns = new ArrayList<>();
    mockColumns.add(BoardColumn.builder()
        .id(1L)
        .name(ColumnName.BACKLOG)
        .position(1)
        .project(testProject)
        .build());
    mockColumns.add(BoardColumn.builder()
        .id(2L)
        .name(ColumnName.TO_DO)
        .position(2)
        .project(testProject)
        .build());

    when(projectRepo.findById(1L)).thenReturn(Optional.of(testProject));
    when(boardColumnRepository.findByProjectIdOrderByPosition(1L)).thenReturn(mockColumns);

    List<BoardColumn> result = boardColumnService.getColumnsByProjectId(1L);

    assertEquals(2, result.size());
    assertEquals(ColumnName.BACKLOG, result.get(0).getName());
    assertEquals(ColumnName.TO_DO, result.get(1).getName());
    assertEquals(1, result.get(0).getPosition());
    assertEquals(2, result.get(1).getPosition());

    verify(projectRepo, times(1)).findById(1L);
    verify(boardColumnRepository, times(1)).findByProjectIdOrderByPosition(1L);
  }

  @Test
  void testGetColumnsByProjectId_ProjectNotFound() {
    when(projectRepo.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> boardColumnService.getColumnsByProjectId(999L)
    );

    verify(boardColumnRepository, never()).findByProjectIdOrderByPosition(anyLong());
  }

  @Test
  void testGetColumnById_Success() {
    BoardColumn mockColumn = BoardColumn.builder()
        .id(1L)
        .name(ColumnName.BACKLOG)
        .position(1)
        .project(testProject)
        .build();

    when(boardColumnRepository.findById(1L)).thenReturn(Optional.of(mockColumn));

    BoardColumn result = boardColumnService.getColumnById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals(ColumnName.BACKLOG, result.getName());

    verify(boardColumnRepository, times(1)).findById(1L);
  }

  @Test
  void testGetColumnById_NotFound() {
    when(boardColumnRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> boardColumnService.getColumnById(999L)
    );

    verify(boardColumnRepository, times(1)).findById(999L);
  }
}

