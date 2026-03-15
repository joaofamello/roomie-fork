package br.edu.ufape.roomie.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import br.edu.ufape.roomie.dto.HabitRequestDTO;
import br.edu.ufape.roomie.dto.HabitResponseDTO;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.StudentRepository;
import br.edu.ufape.roomie.service.HabitService;
import br.edu.ufape.roomie.service.TokenService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = HabitController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
class HabitControllerTest {

  @Autowired private MockMvc mvc;

  @Autowired private JacksonTester<HabitRequestDTO> habitRequestDTOTester;

  @MockitoBean private HabitService habitService;

  @MockitoBean private StudentRepository studentRepository;

  @MockitoBean private TokenService tokenService;

  @MockitoBean private UserDetailsService userDetailsService;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthUser(User user) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
  }

  // ─── GET /api/habits ─────────────────────────────────────────────────────

  @Test
  @DisplayName("GET /api/habits deve retornar 403 quando usuário não é estudante")
  void getMyHabits_notStudent_returns403() throws Exception {
    User user = new User();
    user.setId(99L);
    setAuthUser(user);

    when(studentRepository.findById(99L)).thenReturn(Optional.empty());

    var response = mvc.perform(get("/api/habits")).andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
  }

  @Test
  @DisplayName("GET /api/habits deve retornar 204 quando estudante não tem hábitos")
  void getMyHabits_noHabits_returns204() throws Exception {
    Student student = new Student();
    student.setId(1L);
    setAuthUser(student);

    when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
    when(habitService.getHabitByStudent(student)).thenReturn(null);

    var response = mvc.perform(get("/api/habits")).andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
  }

  @Test
  @DisplayName("GET /api/habits deve retornar 200 com DTO quando estudante tem hábitos")
  void getMyHabits_withHabits_returns200() throws Exception {
    Student student = new Student();
    student.setId(1L);
    setAuthUser(student);

    HabitResponseDTO dto =
        new HabitResponseDTO(
            10L, "MORNING", List.of("Games"), List.of("Noturno"), List.of("Organizado"));
    when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
    when(habitService.getHabitByStudent(student)).thenReturn(dto);

    var response = mvc.perform(get("/api/habits")).andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  // ─── POST /api/habits ────────────────────────────────────────────────────

  @Test
  @DisplayName("POST /api/habits deve retornar 403 quando usuário não é estudante")
  void saveHabits_notStudent_returns403() throws Exception {
    User user = new User();
    user.setId(99L);
    setAuthUser(user);

    when(studentRepository.findById(99L)).thenReturn(Optional.empty());

    HabitRequestDTO dto = new HabitRequestDTO();
    dto.setStudySchedule("MORNING");

    var response =
        mvc.perform(
                post("/api/habits")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(habitRequestDTOTester.write(dto).getJson()))
            .andReturn()
            .getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
  }

  @Test
  @DisplayName("POST /api/habits deve retornar 200 com resposta quando estudante salva hábitos")
  void saveHabits_validStudent_returns200() throws Exception {
    Student student = new Student();
    student.setId(1L);
    setAuthUser(student);

    HabitRequestDTO dto = new HabitRequestDTO();
    dto.setStudySchedule("AFTERNOON");
    dto.setHobbies(List.of("Leitura"));
    dto.setLifeStyles(List.of("Introvertido"));
    dto.setCleaningPrefs(List.of("Organizado"));

    HabitResponseDTO responseDto =
        new HabitResponseDTO(
            10L, "AFTERNOON", List.of("Leitura"), List.of("Introvertido"), List.of("Organizado"));
    when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
    when(habitService.createOrUpdateHabit(any(Student.class), any(HabitRequestDTO.class)))
        .thenReturn(responseDto);

    var response =
        mvc.perform(
                post("/api/habits")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(habitRequestDTOTester.write(dto).getJson()))
            .andReturn()
            .getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
