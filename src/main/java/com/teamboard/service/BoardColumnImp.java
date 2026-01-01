package com.teamboard.service;

import com.teamboard.entity.BoardColumn;
import com.teamboard.entity.ColumnName;
import com.teamboard.entity.Project;
import com.teamboard.repository.BoardColumnRepository;
import com.teamboard.repository.ProjectRepo;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BoardColumnImp implements BoardColumnService {

  private final BoardColumnRepository boardColumnRepository;
  private final ProjectRepo projectRepo;

  public BoardColumnImp(BoardColumnRepository boardColumnRepository, ProjectRepo projectRepo) {
    this.boardColumnRepository = boardColumnRepository;
    this.projectRepo = projectRepo;
  }

  @Override
  public void createDefaultColumns(Long projectId) {
    Project project = projectRepo.findById(projectId)
        .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

    // Create 4 default columns
    ColumnName[] columns = {ColumnName.BACKLOG, ColumnName.TO_DO, ColumnName.IN_PROGRESS, ColumnName.DONE};
    for (int i = 0; i < columns.length; i++) {
      BoardColumn boardColumn = BoardColumn.builder()
          .name(columns[i])
          .position(i + 1)
          .project(project)
          .build();
      boardColumnRepository.save(boardColumn);
    }
  }

  @Override
  public List<BoardColumn> getColumnsByProjectId(Long projectId) {
    // Verify project exists
    projectRepo.findById(projectId)
        .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

    return boardColumnRepository.findByProjectIdOrderByPosition(projectId);
  }

  @Override
  public BoardColumn getColumnById(Long columnId) {
    return boardColumnRepository.findById(columnId)
        .orElseThrow(() -> new IllegalArgumentException("Board column not found with id: " + columnId));
  }
}

