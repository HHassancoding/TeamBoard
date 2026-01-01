package com.teamboard.DTO;

import com.teamboard.entity.Priority;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {
  private Long id;
  private String title;
  private String description;
  private Long projectId;
  private Long columnId;
  private Long assignedToId;
  private String assignedToName;
  private String assignedToInitials;
  private Priority priority;
  private LocalDateTime dueDate;
  private Long createdById;
  private String createdByName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime completedAt;
}

