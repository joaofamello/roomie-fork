package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.RoommateRecommendationDTO;
import br.edu.ufape.roomie.model.Habit;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.repository.StudentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationService {

  private final StudentRepository studentRepository;

  public List<RoommateRecommendationDTO> getRecommendations(Student currentUser) {
    if (currentUser.getHabit() == null) {
      throw new IllegalStateException(
          "Você precisa cadastrar seus hábitos antes de receber recomendações.");
    }

    Habit userHabit = currentUser.getHabit();

    List<Student> allOtherStudents = studentRepository.findByIdNot(currentUser.getId());

    return allOtherStudents.stream()
        .filter(student -> student.getHabit() != null) // Só compara com quem tem hábito
        .map(targetStudent -> calculateAffinity(currentUser, targetStudent))
        .filter(dto -> dto.getCompatibilityPercentage() > 0) // (Opcional) Ignora quem tem 0%
        .sorted(
            (d1, d2) ->
                d2.getCompatibilityPercentage()
                    .compareTo(d1.getCompatibilityPercentage())) // Ordena do maior pro menor
        .collect(Collectors.toList());
  }

  private RoommateRecommendationDTO calculateAffinity(Student currentUser, Student targetStudent) {
    Habit userHabit = currentUser.getHabit();
    Habit targetHabit = targetStudent.getHabit();

    double score = 0;
    List<String> commonTags = new ArrayList<>();

    // 1. Comparar Horário de Estudo (Peso 25)
    if (userHabit.getStudySchedule() != null
        && userHabit.getStudySchedule() == targetHabit.getStudySchedule()) {
      score += 25;
      commonTags.add("Estuda de " + userHabit.getStudySchedule().getLabel());
    }

    // 2. Comparar Estilo de Vida (Peso 25)
    Set<String> userLifeStyles =
        userHabit.getLifeStyles().stream()
            .map(l -> l.getStyle().toLowerCase())
            .collect(Collectors.toSet());
    Set<String> targetLifeStyles =
        targetHabit.getLifeStyles().stream()
            .map(l -> l.getStyle().toLowerCase())
            .collect(Collectors.toSet());
    List<String> commonLifeStyles =
        userLifeStyles.stream().filter(targetLifeStyles::contains).toList();

    if (!userLifeStyles.isEmpty()) {
      score += ((double) commonLifeStyles.size() / userLifeStyles.size()) * 25;
      commonTags.addAll(commonLifeStyles);
    }

    // 3. Comparar Preferências de Limpeza (Peso 25)
    Set<String> userCleanPrefs =
        userHabit.getCleaningPrefs().stream()
            .map(c -> c.getPref().toLowerCase())
            .collect(Collectors.toSet());
    Set<String> targetCleanPrefs =
        targetHabit.getCleaningPrefs().stream()
            .map(c -> c.getPref().toLowerCase())
            .collect(Collectors.toSet());
    List<String> commonCleanPrefs =
        userCleanPrefs.stream().filter(targetCleanPrefs::contains).toList();

    if (!userCleanPrefs.isEmpty()) {
      score += ((double) commonCleanPrefs.size() / userCleanPrefs.size()) * 25;
      commonTags.addAll(commonCleanPrefs);
    }

    // 4. Comparar Hobbies (Peso 25)
    Set<String> userHobbies =
        userHabit.getHobbies().stream()
            .map(h -> h.getHobby().toLowerCase())
            .collect(Collectors.toSet());
    Set<String> targetHobbies =
        targetHabit.getHobbies().stream()
            .map(h -> h.getHobby().toLowerCase())
            .collect(Collectors.toSet());
    List<String> commonHobbies = userHobbies.stream().filter(targetHobbies::contains).toList();

    if (!userHobbies.isEmpty()) {
      score += ((double) commonHobbies.size() / userHobbies.size()) * 25;
      commonTags.addAll(commonHobbies);
    }

    // 5. Bônus Opcional: Mesmo Curso
    if (currentUser.getMajor().equalsIgnoreCase(targetStudent.getMajor())) {
      commonTags.add("Mesmo curso (" + currentUser.getMajor() + ")");
    }

    int finalPercentage = (int) Math.round(score);

    return new RoommateRecommendationDTO(
        targetStudent.getId(),
        targetStudent.getName(),
        targetStudent.getMajor(),
        finalPercentage,
        commonTags,
        targetHabit.getStudySchedule() != null ? targetHabit.getStudySchedule().getLabel() : null,
        targetHabit.getHobbies().stream().map(h -> h.getHobby().toLowerCase()).toList(),
        targetHabit.getLifeStyles().stream().map(l -> l.getStyle().toLowerCase()).toList(),
        targetHabit.getCleaningPrefs().stream().map(c -> c.getPref().toLowerCase()).toList());
  }
}
