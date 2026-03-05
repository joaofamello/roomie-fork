package br.edu.ufape.roomie.dto;

import lombok.Data;

@Data
public class StudentDTO {
    private Long userId;
    private String major;
    private String institution;
}
