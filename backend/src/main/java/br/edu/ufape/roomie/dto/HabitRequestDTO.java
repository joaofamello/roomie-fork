package br.edu.ufape.roomie.dto;

import lombok.Data;

import java.util.List;

@Data
public class HabitRequestDTO {
    private String studySchedule;       // MORNING, AFTERNOON, EVENING, DAWN
    private List<String> hobbies;       // ex: ["Leitura", "Esportes", "Games"]
    private List<String> lifeStyles;    // ex: ["Introvertido", "Noturno"]
    private List<String> cleaningPrefs; // ex: ["Organizado", "Limpa diariamente"]
}
