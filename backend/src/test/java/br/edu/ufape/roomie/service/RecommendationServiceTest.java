package br.edu.ufape.roomie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import br.edu.ufape.roomie.dto.RoommateRecommendationDTO;
import br.edu.ufape.roomie.enums.StudySchedule;
import br.edu.ufape.roomie.model.*;
import br.edu.ufape.roomie.repository.StudentRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

  @Mock private StudentRepository studentRepository;

  @InjectMocks private RecommendationService recommendationService;

  private Student currentUser;

  @BeforeEach
  void setUp() {
    currentUser =
        criarEstudanteComHabito(
            1L, "João", "BCC", StudySchedule.MORNING, "Futebol", "Fitness", "Diária");
  }

  @Test
  @DisplayName("Deve lançar exceção se o estudante logado não tiver hábitos cadastrados")
  void testaExcecaoSemHabitos() {
    Student estudanteSemHabito = new Student();
    estudanteSemHabito.setId(10L);

    assertThatThrownBy(() -> recommendationService.getRecommendations(estudanteSemHabito))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Você precisa cadastrar seus hábitos antes de receber recomendações.");
  }

  @Test
  @DisplayName("Deve retornar 100% de compatibilidade quando os hábitos forem idênticos")
  void testaMatchCemPorCento() {
    Student target =
        criarEstudanteComHabito(
            2L, "Maria", "BCC", StudySchedule.MORNING, "Futebol", "Fitness", "Diária");

    when(studentRepository.findByIdNot(currentUser.getId())).thenReturn(List.of(target));

    List<RoommateRecommendationDTO> recommendations =
        recommendationService.getRecommendations(currentUser);

    assertThat(recommendations).hasSize(1);
    RoommateRecommendationDTO match = recommendations.getFirst();

    assertThat(match.getStudentId()).isEqualTo(2L);
    assertThat(match.getCompatibilityPercentage()).isEqualTo(100);
    assertThat(match.getCommonInterests())
        .contains("Estuda de manhã", "fitness", "diária", "futebol", "Mesmo curso (BCC)");
  }

  @Test
  @DisplayName("Deve retornar 50% de compatibilidade quando apenas metade dos hábitos combinarem")
  void testaMatchParcial() {
    Student target =
        criarEstudanteComHabito(
            2L, "Pedro", "Letras", StudySchedule.MORNING, "Leitura", "Fitness", "Semanal");

    when(studentRepository.findByIdNot(currentUser.getId())).thenReturn(List.of(target));

    List<RoommateRecommendationDTO> recommendations =
        recommendationService.getRecommendations(currentUser);

    assertThat(recommendations).hasSize(1);
    assertThat(recommendations.getFirst().getCompatibilityPercentage()).isEqualTo(50);
    assertThat(recommendations.getFirst().getCommonInterests())
        .containsExactlyInAnyOrder("Estuda de manhã", "fitness");
  }

  @Test
  @DisplayName("Deve retornar lista ordenada do maior para o menor percentual de afinidade")
  void testaOrdenacaoPorAfinidade() {
    Student targetFraco =
        criarEstudanteComHabito(
            2L, "Ana", "Direito", StudySchedule.EVENING, "Nenhum", "Fitness", "Mensal"); // ~25%
    Student targetForte =
        criarEstudanteComHabito(
            3L, "Lucas", "BCC", StudySchedule.MORNING, "Futebol", "Fitness", "Diária"); // 100%

    when(studentRepository.findByIdNot(currentUser.getId()))
        .thenReturn(List.of(targetFraco, targetForte));

    List<RoommateRecommendationDTO> recommendations =
        recommendationService.getRecommendations(currentUser);

    assertThat(recommendations).hasSize(2);
    assertThat(recommendations.get(0).getStudentId()).isEqualTo(3L); // Lucas
    assertThat(recommendations.get(0).getCompatibilityPercentage()).isEqualTo(100);

    assertThat(recommendations.get(1).getStudentId()).isEqualTo(2L); // Ana
    assertThat(recommendations.get(1).getCompatibilityPercentage()).isEqualTo(25);
  }

  @Test
  @DisplayName("Deve lidar com correntes em 0% mas que nao caem na exception do curso")
  void testaCurrentUserSemMajor() {
    Student currentUserSemCurso =
        criarEstudanteComHabito(1L, "Joao", "", null, "Futebol", "Fitness", "Diaria");
    Student target =
        criarEstudanteComHabito(
            2L, "Zezinho", "Engenharia", StudySchedule.EVENING, "Nadar", "Noturno", "Mensal");

    when(studentRepository.findByIdNot(currentUserSemCurso.getId())).thenReturn(List.of(target));

    List<RoommateRecommendationDTO> recommendations =
        recommendationService.getRecommendations(currentUserSemCurso);

    assertThat(recommendations).isEmpty();
  }

  @Test
  @DisplayName("Deve somar bonus caso o curso atual seja igual do target")
  void testaCurrentUserBonusCourse() {
    Student correnteComCurso =
        criarEstudanteComHabito(
            1L, "Joao", "Engenharia", StudySchedule.MORNING, "Futebol", "Fitness", "Diaria");
    Student target =
        criarEstudanteComHabito(
            2L, "Zezinho", "Engenharia", StudySchedule.EVENING, "Futebol", "Fitness", "Diaria");

    when(studentRepository.findByIdNot(correnteComCurso.getId())).thenReturn(List.of(target));

    List<RoommateRecommendationDTO> recommendations =
        recommendationService.getRecommendations(correnteComCurso);

    assertThat(recommendations).hasSize(1);
    assertThat(recommendations.getFirst().getCommonInterests())
        .contains("Mesmo curso (Engenharia)");
  }

  @Test
  @DisplayName("Deve ignorar estudantes que ainda não possuem hábitos cadastrados")
  void testaIgnorarEstudantesSemHabitos() {
    Student targetZero =
        criarEstudanteComHabito(
            2L, "Zezinho", "Engenharia", StudySchedule.EVENING, "Nadar", "Noturno", "Mensal");
    when(studentRepository.findByIdNot(currentUser.getId())).thenReturn(List.of(targetZero));

    List<RoommateRecommendationDTO> recommendations =
        recommendationService.getRecommendations(currentUser);

    assertThat(recommendations).isEmpty();
  }

  @Test
  @DisplayName("Deve calcular corretamente quando listas de hábitos forem vazias")
  void testaMatchComListasVazias() {
    Student target =
        criarEstudanteComHabito(2L, "Maria Vazias", "Historia", null, null, null, null);
    target.getHabit().setHobbies(List.of());
    target.getHabit().setLifeStyles(List.of());
    target.getHabit().setCleaningPrefs(List.of());

    Student currentUserVazio =
        criarEstudanteComHabito(1L, "Joao Vazio", "Historia", null, null, null, null);
    currentUserVazio.getHabit().setHobbies(List.of());
    currentUserVazio.getHabit().setLifeStyles(List.of());
    currentUserVazio.getHabit().setCleaningPrefs(List.of());

    when(studentRepository.findByIdNot(currentUserVazio.getId())).thenReturn(List.of(target));

    List<RoommateRecommendationDTO> recommendations =
        recommendationService.getRecommendations(currentUserVazio);

    // As they have the same major, "Mesmo curso (Historia)" is added, but no score from 1-4.
    // Final score will be 0, so it gets filtered out by `dto.getCompatibilityPercentage() > 0`
    assertThat(recommendations).isEmpty();
  }

  @Test
  @DisplayName("Deve lidar com target que nao tem study schedule sem quebrar")
  void testaTargetSemStudySchedule() {
    Student target =
        criarEstudanteComHabito(2L, "Pedro", "Letras", null, "Leitura", "Fitness", "Semanal");

    when(studentRepository.findByIdNot(currentUser.getId())).thenReturn(List.of(target));

    List<RoommateRecommendationDTO> recommendations =
        recommendationService.getRecommendations(currentUser);

    // currentUser has study schedule morning, target has none. Match should be just lifeStyle
    assertThat(recommendations).hasSize(1);
    RoommateRecommendationDTO rec = recommendations.getFirst();
    assertThat(rec.getStudySchedule()).isNull();
  }

  @Test
  @DisplayName("Deve ignorar estudantes que ainda não possuem hábitos cadastrados (null)")
  void testaIgnorarEstudantesSemHabitosTotalmente() {
    Student targetComHabito =
        criarEstudanteComHabito(
            2L, "Maria", "BCC", StudySchedule.MORNING, "Futebol", "Fitness", "Diária");
    Student targetSemHabito = new Student();
    targetSemHabito.setId(3L);
    targetSemHabito.setName("João Sem Hábito");

    when(studentRepository.findByIdNot(currentUser.getId()))
        .thenReturn(List.of(targetComHabito, targetSemHabito));

    List<RoommateRecommendationDTO> recommendations =
        recommendationService.getRecommendations(currentUser);

    assertThat(recommendations).hasSize(1);
    assertThat(recommendations.getFirst().getStudentId()).isEqualTo(2L);
  }

  private Student criarEstudanteComHabito(
      Long id,
      String name,
      String major,
      StudySchedule schedule,
      String hobbyDesc,
      String lifeStyleDesc,
      String cleanPrefDesc) {
    Student student = new Student();
    student.setId(id);
    student.setName(name);
    student.setMajor(major);

    Habit habit = new Habit();
    habit.setStudent(student);
    habit.setStudySchedule(schedule);

    Hobby hobby = new Hobby(null, habit, hobbyDesc);
    habit.setHobbies(List.of(hobby));

    LifeStyle lifeStyle = new LifeStyle(null, habit, lifeStyleDesc);
    habit.setLifeStyles(List.of(lifeStyle));

    CleaningPrefs cleaningPrefs = new CleaningPrefs(null, habit, cleanPrefDesc);
    habit.setCleaningPrefs(List.of(cleaningPrefs));

    student.setHabit(habit);
    return student;
  }
}
