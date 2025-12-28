package com.teamboard.repository;

import com.teamboard.entity.WorkspaceMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
  List<WorkspaceMember> findByWorkspaceId(Long workspaceId);

  List<WorkspaceMember> findByUserId(Long userId);

  Optional<WorkspaceMember> findByUserIdAndWorkspaceId(Long userId, Long workspaceId);
}

