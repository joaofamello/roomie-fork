package br.edu.ufape.roomie.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import br.edu.ufape.roomie.dto.ContractRequestDTO;
import br.edu.ufape.roomie.dto.ContractResponseDTO;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.service.ContractService;
import br.edu.ufape.roomie.service.TokenService;

@WebMvcTest(controllers = ContractController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
class ContractControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<ContractRequestDTO> contractRequestDTOJacksonTester;

    @MockitoBean
    private ContractService contractService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Authentication setupAuth(Long id) {
        User user = new User();
        user.setId(id);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    @DisplayName("Deveria retornar 201 ao criar contrato com dados válidos")
    void shouldCreateContract() throws Exception {
        ContractRequestDTO request = new ContractRequestDTO();
        request.setChatId(1L);
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setEndDate(LocalDate.of(2026, 2, 1));
        request.setPrice(new BigDecimal("1200.00"));

        ContractResponseDTO responseDto = new ContractResponseDTO();
        responseDto.setId(10L);

        when(contractService.createContract(any(ContractRequestDTO.class), any(User.class))).thenReturn(responseDto);

        var response = mvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contractRequestDTOJacksonTester.write(request).getJson())
                        .principal(setupAuth(1L)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("Deveria retornar 403 quando o serviço lançar IllegalStateException ao criar contrato")
    void shouldReturnForbiddenWhenCreateContractIllegalState() throws Exception {
        ContractRequestDTO request = new ContractRequestDTO();
        request.setChatId(1L);
        when(contractService.createContract(any(ContractRequestDTO.class), any()))
                .thenThrow(new IllegalStateException("já existe"));

        var response = mvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contractRequestDTOJacksonTester.write(request).getJson())
                        .principal(setupAuth(1L)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Deveria retornar 400 quando o serviço lançar IllegalArgumentException ao criar contrato")
    void shouldReturnBadRequestWhenCreateContractIllegalArgument() throws Exception {
        ContractRequestDTO request = new ContractRequestDTO();
        request.setChatId(1L);
        when(contractService.createContract(any(ContractRequestDTO.class), any()))
                .thenThrow(new IllegalArgumentException("dados inválidos"));

        var response = mvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contractRequestDTOJacksonTester.write(request).getJson())
                        .principal(setupAuth(1L)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Deveria retornar 404 quando o serviço lançar RuntimeException ao criar contrato")
    void shouldReturnNotFoundWhenCreateContractRuntimeException() throws Exception {
        ContractRequestDTO request = new ContractRequestDTO();
        request.setChatId(1L);
        when(contractService.createContract(any(ContractRequestDTO.class), any()))
                .thenThrow(new RuntimeException("não encontrado"));

        var response = mvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contractRequestDTOJacksonTester.write(request).getJson())
                        .principal(setupAuth(1L)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Deveria aceitar contrato com sucesso")
    void shouldAcceptContract() throws Exception {
        ContractResponseDTO responseDto = new ContractResponseDTO();
        responseDto.setId(1L);
        when(contractService.acceptContract(anyLong(), any())).thenReturn(responseDto);

        var response = mvc.perform(patch("/api/contracts/1/accept").principal(setupAuth(1L)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deveria retornar 403 ao tentar aceitar contrato sem permissão")
    void shouldReturnForbiddenWhenAcceptContractIllegalState() throws Exception {
        when(contractService.acceptContract(anyLong(), any()))
                .thenThrow(new IllegalStateException("não autorizado"));

        var response = mvc.perform(patch("/api/contracts/1/accept").principal(setupAuth(1L)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Deveria retornar 404 ao tentar aceitar contrato não existente")
    void shouldReturnNotFoundWhenAcceptContractRuntime() throws Exception {
        when(contractService.acceptContract(anyLong(), any()))
                .thenThrow(new RuntimeException("não encontrado"));

        var response = mvc.perform(patch("/api/contracts/1/accept").principal(setupAuth(1L)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Deveria rejeitar contrato com sucesso")
    void shouldRejectContract() throws Exception {
        ContractResponseDTO responseDto = new ContractResponseDTO();
        responseDto.setId(1L);
        when(contractService.rejectContract(anyLong(), any())).thenReturn(responseDto);

        var response = mvc.perform(patch("/api/contracts/1/reject").principal(setupAuth(1L)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deveria retornar 403 ao tentar rejeitar contrato sem permissão")
    void shouldReturnForbiddenWhenRejectContractIllegalState() throws Exception {
        when(contractService.rejectContract(anyLong(), any()))
                .thenThrow(new IllegalStateException("não autorizado"));

        var response = mvc.perform(patch("/api/contracts/1/reject").principal(setupAuth(1L)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Deveria retornar 404 ao tentar rejeitar contrato não existente")
    void shouldReturnNotFoundWhenRejectContractRuntime() throws Exception {
        when(contractService.rejectContract(anyLong(), any()))
                .thenThrow(new RuntimeException("não encontrado"));

        var response = mvc.perform(patch("/api/contracts/1/reject").principal(setupAuth(1L)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Deveria retornar 200 OK ao buscar contratos por chat")
    void shouldGetContractsByChat() throws Exception {
        when(contractService.getContractsByChat(anyLong(), any(User.class))).thenReturn(List.of(new ContractResponseDTO()));

        var response = mvc.perform(get("/api/contracts/chat/1").principal(setupAuth(1L)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deveria retornar 404 quando não encontrar contratos ao buscar por chat")
    void shouldReturnNotFoundWhenGetContractsByChatRuntime() throws Exception {
        when(contractService.getContractsByChat(anyLong(), any()))
                .thenThrow(new RuntimeException("não encontrado"));

        var response = mvc.perform(get("/api/contracts/chat/1").principal(setupAuth(1L)))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
