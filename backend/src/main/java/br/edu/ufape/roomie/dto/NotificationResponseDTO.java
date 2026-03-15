package br.edu.ufape.roomie.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NotificationResponseDTO {
  private Long id;
  private String message;
  private boolean read;
  private LocalDateTime createdAt;
}
