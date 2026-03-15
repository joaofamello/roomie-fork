package br.edu.ufape.roomie.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
