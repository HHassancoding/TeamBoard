package com.teamboard.repository;

import com.teamboard.entity.Workspace;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
  List<Workspace> findByOwnerId(Long ownerId);

  Optional<Workspace> findByOwnerIdAndName(Long ownerId, String name);

  List<Workspace> findByNameContainingIgnoreCase(String name);
}

