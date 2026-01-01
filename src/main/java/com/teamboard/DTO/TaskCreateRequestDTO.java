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
public class TaskCreateRequestDTO {
  private String title;
  private String description;
  private Long assignedToId;
  private Priority priority;
  private LocalDateTime dueDate;
}

