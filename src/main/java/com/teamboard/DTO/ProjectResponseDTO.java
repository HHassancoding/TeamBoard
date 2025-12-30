package com.teamboard.DTO;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponseDTO {
  private Long id;
  private String name;
  private String description;
  private Long workspaceId;
  private Long createdById;
  private String createdByName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
