package com.teamboard.service;

import com.teamboard.entity.BoardColumn;
import java.util.List;

public interface BoardColumnService {
  /**
   * Creates default 4 columns (Backlog, To Do, In Progress, Done) for a project.
   * @param projectId the project ID
   */
  void createDefaultColumns(Long projectId);

  /**
   * Retrieves all columns for a project, ordered by position.
   * @param projectId the project ID
   * @return list of columns ordered by position
   */
  List<BoardColumn> getColumnsByProjectId(Long projectId);

  /**
   * Retrieves a column by ID.
   * @param columnId the column ID
   * @return the board column
   */
  BoardColumn getColumnById(Long columnId);
}

