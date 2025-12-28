package com.teamboard.DTO;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponseDTO {
  private Long id;
  private String name;
  private String description;
  private Long ownerId;
  private String ownerName;
  private String ownerEmail;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

