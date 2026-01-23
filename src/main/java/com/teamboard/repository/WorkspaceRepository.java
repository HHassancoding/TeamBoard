package com.teamboard.repository;

import com.teamboard.entity.Workspace;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
  List<Workspace> findByOwnerId(Long ownerId);

  Optional<Workspace> findByOwnerIdAndName(Long ownerId, String name);

  List<Workspace> findByNameContainingIgnoreCase(String name);

  // Returns workspaces the user owns or is a member of (DISTINCT to avoid duplicates)
  @Query("""
      select distinct w
      from Workspace w
      left join com.teamboard.entity.WorkspaceMember wm on wm.workspace.id = w.id
      where w.owner.id = :userId or wm.user.id = :userId
      """)
  List<Workspace> findAllAccessibleByUser(@Param("userId") Long userId);
}
