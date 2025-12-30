package com.teamboard.repository;

import com.teamboard.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepo extends JpaRepository<Project, Long> {
  List<Project> findByWorkspaceId(Long workspaceId);
  Optional<Project> findByWorkspaceIdAndId(Long workspaceId, Long id);
}
