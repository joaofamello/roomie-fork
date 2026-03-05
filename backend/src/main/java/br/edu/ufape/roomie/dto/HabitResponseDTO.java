package br.edu.ufape.roomie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabitResponseDTO {
    private Long id;
    private String studySchedule;
    private List<String> hobbies;
    private List<String> lifeStyles;
    private List<String> cleaningPrefs;
}
