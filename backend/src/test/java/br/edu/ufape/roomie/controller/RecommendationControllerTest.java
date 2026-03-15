package br.edu.ufape.roomie.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import br.edu.ufape.roomie.dto.RoommateRecommendationDTO;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.StudentRepository;
import br.edu.ufape.roomie.service.RecommendationService;
import br.edu.ufape.roomie.service.TokenService;
import java.nio.charset.StandardCharsets;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = RecommendationController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
class RecommendationControllerTest {

  @Autowired private MockMvc mvc;

  @Autowired private JacksonTester<List<RoommateRecommendationDTO>> dtoListJacksonTester;

  @MockitoBean private RecommendationService recommendationService;

  @MockitoBean private TokenService tokenService;

  @MockitoBean private UserDetailsService userDetailsService;

  @MockitoBean private StudentRepository studentRepository;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Deveria devolver código HTTP 200 e a lista de recomendações com sucesso")
  void testaRetornarRecomendacoesComSucesso() throws Exception {
    Student mockStudent = new Student();
    mockStudent.setId(1L);
    mockStudent.setEmail("estudante@ufape.edu.br");

    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                mockStudent, null, mockStudent.getAuthorities()));

    when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));

    RoommateRecommendationDTO recomendacao =
        new RoommateRecommendationDTO(
            2L,
            "Maria",
            "BCC",
            85,
            List.of("Estuda de MORNING", "fitness"),
            "manhã",
            List.of("fitness"),
            List.of("introvertido"),
            List.of("organizado"));
    List<RoommateRecommendationDTO> listaRecomendacoes = List.of(recomendacao);

    when(recommendationService.getRecommendations(any(Student.class)))
        .thenReturn(listaRecomendacoes);

    var response = mvc.perform(get("/recommendations/roommates")).andReturn().getResponse();

    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    String jsonEsperado = dtoListJacksonTester.write(listaRecomendacoes).getJson();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
  }

  @Test
  @DisplayName("Deveria devolver código HTTP 400 quando o estudante não tiver hábitos cadastrados")
  void testaRetornarErroSemHabitos() throws Exception {
    Student mockStudent = new Student();
    mockStudent.setId(1L);
    mockStudent.setEmail("estudante@ufape.edu.br");

    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                mockStudent, null, mockStudent.getAuthorities()));

    when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));

    when(recommendationService.getRecommendations(any(Student.class)))
        .thenThrow(
            new IllegalStateException(
                "Você precisa cadastrar seus hábitos antes de receber recomendações."));

    var response = mvc.perform(get("/recommendations/roommates")).andReturn().getResponse();

    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(response.getContentAsString())
        .isEqualTo("Você precisa cadastrar seus hábitos antes de receber recomendações.");
  }

  @Test
  @DisplayName("Deveria devolver código HTTP 403 quando o usuário logado não for um estudante")
  void testaRetornarProibidoUsuarioNaoEstudante() throws Exception {
    User mockUser = new User();
    mockUser.setId(2L);
    mockUser.setEmail("dono@ufape.edu.br");

    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities()));

    var response = mvc.perform(get("/recommendations/roommates")).andReturn().getResponse();

    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(response.getContentAsString())
        .isEqualTo(
            "Acesso negado: Apenas estudantes podem receber recomendações de colegas de quarto.");
  }
}
