package br.edu.ufape.roomie.controller;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import br.edu.ufape.roomie.dto.EvaluationRequestDTO;
import br.edu.ufape.roomie.dto.EvaluationResponseDTO;
import br.edu.ufape.roomie.dto.EvaluationSummaryDTO;
import br.edu.ufape.roomie.service.EvaluationService;
import br.edu.ufape.roomie.service.TokenService;

@WebMvcTest(controllers = EvaluationController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
class EvaluationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<EvaluationRequestDTO> evaluationRequestDTOJacksonTester;

    @MockitoBean
    private EvaluationService evaluationService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("Deveria criar avaliação e retornar 201")
    void shouldCreateEvaluation() throws Exception {
        EvaluationRequestDTO request = new EvaluationRequestDTO();
        request.setRating(5);

        EvaluationResponseDTO responseDto = new EvaluationResponseDTO(1L, 2L, 3L, "Aluno", 5, LocalDateTime.now());
        when(evaluationService.createEvaluation(anyLong(), any(EvaluationRequestDTO.class))).thenReturn(responseDto);

        var response = mvc.perform(post("/api/properties/1/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(evaluationRequestDTOJacksonTester.write(request).getJson()))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("Deveria retornar 400 ao enviar nota inválida")
    void shouldReturnBadRequestWhenRatingInvalid() throws Exception {
        EvaluationRequestDTO request = new EvaluationRequestDTO();
        request.setRating(0);

        var response = mvc.perform(post("/api/properties/1/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(evaluationRequestDTOJacksonTester.write(request).getJson()))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Deveria retornar resumo de avaliações e 200 OK")
    void shouldGetEvaluationsByProperty() throws Exception {
        EvaluationSummaryDTO summary = new EvaluationSummaryDTO(4.5, 10L, List.of());
        when(evaluationService.getEvaluationsByProperty(anyLong())).thenReturn(summary);

        var response = mvc.perform(get("/api/properties/1/evaluations")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deveria retornar 200 OK para checar se usuário avaliou")
    void shouldCheckHasEvaluated() throws Exception {
        when(evaluationService.hasUserEvaluated(anyLong())).thenReturn(true);

        var response = mvc.perform(get("/api/properties/1/evaluations/check")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }
}
