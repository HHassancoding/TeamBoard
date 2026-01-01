package com.teamboard.DTO;

import com.teamboard.entity.ColumnName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumnResponseDTO {
  private Long id;
  private ColumnName name;
  private Integer position;
  private Long projectId;
  private LocalDateTime createdAt;
}

