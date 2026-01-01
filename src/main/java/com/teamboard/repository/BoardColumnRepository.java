package com.teamboard.repository;

import com.teamboard.entity.BoardColumn;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {
  List<BoardColumn> findByProjectIdOrderByPosition(Long projectId);
}

