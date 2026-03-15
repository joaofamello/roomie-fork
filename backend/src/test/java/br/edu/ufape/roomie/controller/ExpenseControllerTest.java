package br.edu.ufape.roomie.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import br.edu.ufape.roomie.dto.ExpenseRequestDTO;
import br.edu.ufape.roomie.dto.ExpenseResponseDTO;
import br.edu.ufape.roomie.dto.ExpenseSummaryDTO;
import br.edu.ufape.roomie.model.Expense;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.service.ExpenseService;
import br.edu.ufape.roomie.service.TokenService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = ExpenseController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
class ExpenseControllerTest {

  @Autowired private MockMvc mvc;

  @Autowired private JacksonTester<ExpenseRequestDTO> expenseRequestDTOJacksonTester;

  @MockitoBean private ExpenseService expenseService;

  @MockitoBean private TokenService tokenService;

  @MockitoBean private UserDetailsService userDetailsService;

  @Test
  @DisplayName("Deve retornar 201 CREATED ao criar uma despesa com dados válidos")
  @WithMockUser
  void testaCriarDespesaComSucesso() throws Exception {
    ExpenseRequestDTO request = new ExpenseRequestDTO();
    request.setPropertyId(10L);
    request.setDescription("Conta de Luz");
    request.setAmount(new BigDecimal("150.00"));
    request.setExpenseDate(LocalDate.now());

    Student mockStudent = new Student();
    mockStudent.setId(1L);

    Expense mockExpense = new Expense();
    mockExpense.setId(50L);
    mockExpense.setDescription("Conta de Luz");
    mockExpense.setAmount(new BigDecimal("150.00"));
    mockExpense.setExpenseDate(LocalDate.now());
    mockExpense.setRegisteredBy(mockStudent);

    ExpenseResponseDTO mockResponse = new ExpenseResponseDTO(mockExpense);

    when(expenseService.createExpense(any(ExpenseRequestDTO.class))).thenReturn(mockResponse);

    var response =
        mvc.perform(
                post("/api/expenses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(expenseRequestDTOJacksonTester.write(request).getJson()))
            .andReturn()
            .getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
    assertThat(response.getContentAsString()).contains("\"description\":\"Conta de Luz\"");
    assertThat(response.getContentAsString()).contains("\"amount\":150.00");
  }

  @Test
  @DisplayName("Deve retornar 400 BAD REQUEST se enviar valor negativo ou dados vazios")
  @WithMockUser
  void testaCriarDespesaComDadosInvalidos() throws Exception {
    ExpenseRequestDTO request = new ExpenseRequestDTO();
    request.setPropertyId(null);
    request.setDescription("");
    request.setAmount(new BigDecimal("-50.00"));
    request.setExpenseDate(null);

    var response =
        mvc.perform(
                post("/api/expenses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(expenseRequestDTOJacksonTester.write(request).getJson()))
            .andReturn()
            .getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  @DisplayName("Deve retornar 200 OK e o resumo financeiro da moradia")
  @WithMockUser
  void testaBuscarResumoDespesasDaMoradia() throws Exception {
    ExpenseSummaryDTO mockSummary = new ExpenseSummaryDTO();
    mockSummary.setTotalAmount(new BigDecimal("300.00"));
    mockSummary.setNumberOfResidents(3);
    mockSummary.setAmountPerResident(new BigDecimal("100.00"));
    mockSummary.setExpenses(List.of());
    when(expenseService.getExpensesByProperty(10L)).thenReturn(mockSummary);

    var response = mvc.perform(get("/api/expenses/property/10")).andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).contains("\"totalAmount\":300.00");
    assertThat(response.getContentAsString()).contains("\"numberOfResidents\":3");
    assertThat(response.getContentAsString()).contains("\"amountPerResident\":100.00");
  }
}
