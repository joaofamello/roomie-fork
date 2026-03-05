package br.edu.ufape.roomie.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long ownerId;
    private String ownerName;
    private Long propertyId;
    private String propertyTitle;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;

    private long unreadCount;
}


