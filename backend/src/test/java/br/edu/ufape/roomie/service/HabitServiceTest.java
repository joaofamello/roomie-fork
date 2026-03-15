package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.HabitRequestDTO;
import br.edu.ufape.roomie.dto.HabitResponseDTO;
import br.edu.ufape.roomie.enums.StudySchedule;
import br.edu.ufape.roomie.model.*;
import br.edu.ufape.roomie.repository.HabitRepository;
import br.edu.ufape.roomie.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private HabitService habitService;

    private Student mockStudent;
    private Habit mockHabit;

    @BeforeEach
    void setUp() {
        mockStudent = new Student();
        mockStudent.setId(1L);

        mockHabit = new Habit();
        mockHabit.setIdHabit(10L);
        mockHabit.setStudent(mockStudent);
        mockHabit.setStudySchedule(StudySchedule.MORNING);
        mockHabit.setHobbies(new ArrayList<>());
        mockHabit.setLifeStyles(new ArrayList<>());
        mockHabit.setCleaningPrefs(new ArrayList<>());
    }

    // ─── getHabitByStudent ────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve lançar 404 quando estudante não for encontrado ao buscar hábitos")
    void getHabit_studentNotFound_throws404() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> habitService.getHabitByStudent(mockStudent));

        assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Deve retornar null quando estudante não possui hábitos cadastrados")
    void getHabit_noHabit_returnsNull() {
        mockStudent.setHabit(null);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));

        HabitResponseDTO result = habitService.getHabitByStudent(mockStudent);

        assertNull(result);
    }

    @Test
    @DisplayName("Deve retornar DTO correto quando estudante possui hábitos")
    void getHabit_withHabit_returnsDTO() {
        Hobby hobby = new Hobby();
        hobby.setHobby("Leitura");
        mockHabit.getHobbies().add(hobby);

        mockStudent.setHabit(mockHabit);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));

        HabitResponseDTO result = habitService.getHabitByStudent(mockStudent);

        assertNotNull(result);
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStudySchedule()).isEqualTo("MORNING");
        assertThat(result.getHobbies()).containsExactly("Leitura");
    }

    // ─── createOrUpdateHabit ─────────────────────────────────────────────────

    @Test
    @DisplayName("Deve lançar 404 quando estudante não for encontrado ao salvar hábitos")
    void createOrUpdate_studentNotFound_throws404() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        HabitRequestDTO dto = new HabitRequestDTO();
        dto.setStudySchedule("MORNING");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> habitService.createOrUpdateHabit(mockStudent, dto));

        assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Deve criar novo hábito quando estudante ainda não tem um")
    void createOrUpdate_createsNewHabit() {
        mockStudent.setHabit(null);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(habitRepository.save(any(Habit.class))).thenAnswer(inv -> inv.getArgument(0));

        HabitRequestDTO dto = new HabitRequestDTO();
        dto.setStudySchedule("AFTERNOON");
        dto.setHobbies(List.of("Games"));
        dto.setLifeStyles(List.of("Introvertido"));
        dto.setCleaningPrefs(List.of("Organizado"));

        HabitResponseDTO result = habitService.createOrUpdateHabit(mockStudent, dto);

        assertNotNull(result);
        assertThat(result.getStudySchedule()).isEqualTo("AFTERNOON");
        assertThat(result.getHobbies()).containsExactly("Games");
        assertThat(result.getLifeStyles()).containsExactly("Introvertido");
        assertThat(result.getCleaningPrefs()).containsExactly("Organizado");
        verify(habitRepository).save(any(Habit.class));
    }

    @Test
    @DisplayName("Deve atualizar hábito existente do estudante")
    void createOrUpdate_updatesExistingHabit() {
        mockStudent.setHabit(mockHabit);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(habitRepository.save(any(Habit.class))).thenAnswer(inv -> inv.getArgument(0));

        HabitRequestDTO dto = new HabitRequestDTO();
        dto.setStudySchedule("EVENING");
        dto.setHobbies(List.of("Música", "Esportes"));
        dto.setLifeStyles(new ArrayList<>());
        dto.setCleaningPrefs(new ArrayList<>());

        HabitResponseDTO result = habitService.createOrUpdateHabit(mockStudent, dto);

        assertThat(result.getStudySchedule()).isEqualTo("EVENING");
        assertThat(result.getHobbies()).containsExactlyInAnyOrder("Música", "Esportes");
    }

    @Test
    @DisplayName("Deve lançar 400 quando o horário de estudo for inválido")
    void createOrUpdate_invalidStudySchedule_throws400() {
        mockStudent.setHabit(mockHabit);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));

        HabitRequestDTO dto = new HabitRequestDTO();
        dto.setStudySchedule("INVALIDO");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> habitService.createOrUpdateHabit(mockStudent, dto));

        assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Deve aceitar horário de estudo nulo (sem horário definido)")
    void createOrUpdate_nullStudySchedule_setsNull() {
        mockStudent.setHabit(mockHabit);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(habitRepository.save(any(Habit.class))).thenAnswer(inv -> inv.getArgument(0));

        HabitRequestDTO dto = new HabitRequestDTO();
        dto.setStudySchedule(null);
        dto.setHobbies(List.of());
        dto.setLifeStyles(List.of());
        dto.setCleaningPrefs(List.of());

        HabitResponseDTO result = habitService.createOrUpdateHabit(mockStudent, dto);

        assertNull(result.getStudySchedule());
    }

    @Test
    @DisplayName("Deve aceitar horário de estudo em branco (sem horário definido)")
    void createOrUpdate_blankStudySchedule_setsNull() {
        mockStudent.setHabit(mockHabit);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(habitRepository.save(any(Habit.class))).thenAnswer(inv -> inv.getArgument(0));

        HabitRequestDTO dto = new HabitRequestDTO();
        dto.setStudySchedule("   ");
        dto.setHobbies(new ArrayList<>());
        dto.setLifeStyles(new ArrayList<>());
        dto.setCleaningPrefs(new ArrayList<>());

        HabitResponseDTO result = habitService.createOrUpdateHabit(mockStudent, dto);

        assertNull(result.getStudySchedule());
    }

    @Test
    @DisplayName("Deve ignorar elementos vazios ou nulos nas listas (Hobbies, LifeStyles, CleaningPrefs)")
    void createOrUpdate_ignoresNullOrBlankListElements() {
        mockStudent.setHabit(mockHabit);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(habitRepository.save(any(Habit.class))).thenAnswer(inv -> inv.getArgument(0));

        HabitRequestDTO dto = new HabitRequestDTO();
        
        List<String> hobbies = new ArrayList<>();
        hobbies.add(null);
        hobbies.add("");
        hobbies.add("   ");
        hobbies.add("Valid Hobby");
        dto.setHobbies(hobbies);

        List<String> lifeStyles = new ArrayList<>();
        lifeStyles.add(null);
        lifeStyles.add("");
        lifeStyles.add("   ");
        lifeStyles.add("Valid LifeStyle");
        dto.setLifeStyles(lifeStyles);

        List<String> cleaningPrefs = new ArrayList<>();
        cleaningPrefs.add(null);
        cleaningPrefs.add("");
        cleaningPrefs.add("   ");
        cleaningPrefs.add("Valid CleaningPref");
        dto.setCleaningPrefs(cleaningPrefs);

        HabitResponseDTO result = habitService.createOrUpdateHabit(mockStudent, dto);

        assertThat(result.getHobbies()).containsExactly("Valid Hobby");
        assertThat(result.getLifeStyles()).containsExactly("Valid LifeStyle");
        assertThat(result.getCleaningPrefs()).containsExactly("Valid CleaningPref");
    }

    @Test
    @DisplayName("Deve limpar listas corretamente com entradas null no DTO (Hobbies, LifeStyles, CleaningPrefs)")
    void createOrUpdate_nullListRequests_clearLists() {
        mockStudent.setHabit(mockHabit);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(habitRepository.save(any(Habit.class))).thenAnswer(inv -> inv.getArgument(0));

        HabitRequestDTO dto = new HabitRequestDTO();
        dto.setHobbies(null);
        dto.setLifeStyles(null);
        dto.setCleaningPrefs(null);

        HabitResponseDTO result = habitService.createOrUpdateHabit(mockStudent, dto);

        assertThat(result.getHobbies()).isEmpty();
        assertThat(result.getLifeStyles()).isEmpty();
        assertThat(result.getCleaningPrefs()).isEmpty();
    }
}
