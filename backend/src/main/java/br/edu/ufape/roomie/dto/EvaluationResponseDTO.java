package br.edu.ufape.roomie.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EvaluationResponseDTO {
  private Long id;
  private Long propertyId;
  private Long studentId;
  private String studentName;
  private Integer rating;
  private LocalDateTime timestamp;
}
