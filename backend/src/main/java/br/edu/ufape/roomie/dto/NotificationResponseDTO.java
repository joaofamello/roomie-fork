package br.edu.ufape.roomie.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponseDTO {
    private Long id;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}

