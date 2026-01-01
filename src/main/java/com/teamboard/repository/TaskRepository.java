package com.teamboard.repository;

import com.teamboard.entity.Task;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
  List<Task> findByProjectIdOrderByCreatedAtDesc(Long projectId);
  List<Task> findByColumnIdOrderByCreatedAtDesc(Long columnId);
  List<Task> findByAssignedToId(Long userId);
  List<Task> findByProjectIdAndColumnIdOrderByCreatedAtDesc(Long projectId, Long columnId);
}

