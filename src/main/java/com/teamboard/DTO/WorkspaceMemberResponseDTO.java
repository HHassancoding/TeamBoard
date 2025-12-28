package com.teamboard.DTO;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberResponseDTO {
  private Long id;
  private Long userId;
  private String userEmail;
  private String userName;
  private String role;
  private LocalDateTime joinedAt;
  private LocalDateTime updatedAt;
}

