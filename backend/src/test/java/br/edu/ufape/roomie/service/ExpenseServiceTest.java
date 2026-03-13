package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.ExpenseRequestDTO;
import br.edu.ufape.roomie.dto.ExpenseResponseDTO;
import br.edu.ufape.roomie.dto.ExpenseSummaryDTO;
import br.edu.ufape.roomie.enums.ContractStatus;
import br.edu.ufape.roomie.model.Contract;
import br.edu.ufape.roomie.model.Expense;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.repository.ContractRepository;
import br.edu.ufape.roomie.repository.ExpenseRepository;
import br.edu.ufape.roomie.repository.PropertyRepository;
import br.edu.ufape.roomie.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private Student mockStudent;

    @BeforeEach
    void setUp() {
        mockStudent = new Student();
        mockStudent.setId(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockStudent, null, null)
        );
        lenient().when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateExpenseSuccessfully() {
        Property property = new Property();
        property.setId(10L);

        ExpenseRequestDTO dto = new ExpenseRequestDTO();
        dto.setPropertyId(10L);
        dto.setDescription("Energia");
        dto.setAmount(new BigDecimal("150.00"));
        dto.setExpenseDate(LocalDate.now());

        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(contractRepository.existsByPropertyIdAndStudentIdAndStatusIn(10L, 1L, List.of(ContractStatus.ACTIVE))).thenReturn(true);
        when(expenseRepository.save(any())).thenAnswer(i -> {
            Expense e = i.getArgument(0);
            e.setId(50L);
            return e;
        });

        ExpenseResponseDTO result = expenseService.createExpense(dto);

        assertNotNull(result);
        assertEquals("Energia", result.getDescription());
        assertEquals(new BigDecimal("150.00"), result.getAmount());
    }

    @Test
    void shouldThrowForbiddenWhenNotResident() {
        Property property = new Property();
        property.setId(10L);

        ExpenseRequestDTO dto = new ExpenseRequestDTO();
        dto.setPropertyId(10L);

        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(contractRepository.existsByPropertyIdAndStudentIdAndStatusIn(10L, 1L, List.of(ContractStatus.ACTIVE))).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> expenseService.createExpense(dto));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void shouldCalculateSummarySuccessfully() {
        Expense e1 = new Expense();
        e1.setId(1L); e1.setAmount(new BigDecimal("100.00")); e1.setRegisteredBy(mockStudent); e1.setExpenseDate(LocalDate.now());

        Expense e2 = new Expense();
        e2.setId(2L); e2.setAmount(new BigDecimal("50.00")); e2.setRegisteredBy(mockStudent); e2.setExpenseDate(LocalDate.now());

        when(contractRepository.existsByPropertyIdAndStudentIdAndStatusIn(10L, 1L, List.of(ContractStatus.ACTIVE))).thenReturn(true);
        when(expenseRepository.findByPropertyId(10L)).thenReturn(List.of(e1, e2));

        when(contractRepository.findByPropertyIdAndStatus(10L, ContractStatus.ACTIVE)).thenReturn(List.of(new Contract(), new Contract(), new Contract()));

        ExpenseSummaryDTO result = expenseService.getExpensesByProperty(10L);

        assertEquals(new BigDecimal("150.00"), result.getTotalAmount());
        assertEquals(3, result.getNumberOfResidents());
        assertEquals(new BigDecimal("50.00"), result.getAmountPerResident());
        assertEquals(2, result.getExpenses().size());
    }

    @Test
    @DisplayName("Deve retornar valor por morador igual a ZERO se não houver moradores ativos para dividir")
    void shouldReturnZeroAmountPerResidentWhenNoActiveResidents() {
        Expense e1 = new Expense();
        e1.setId(1L);
        e1.setAmount(new BigDecimal("100.00"));
        e1.setRegisteredBy(mockStudent);
        e1.setExpenseDate(LocalDate.now());

        when(contractRepository.existsByPropertyIdAndStudentIdAndStatusIn(10L, 1L, List.of(ContractStatus.ACTIVE))).thenReturn(true);
        when(expenseRepository.findByPropertyId(10L)).thenReturn(List.of(e1));

        when(contractRepository.findByPropertyIdAndStatus(10L, ContractStatus.ACTIVE)).thenReturn(List.of());

        ExpenseSummaryDTO result = expenseService.getExpensesByProperty(10L);

        assertEquals(new BigDecimal("100.00"), result.getTotalAmount());
        assertEquals(0, result.getNumberOfResidents());
        assertEquals(BigDecimal.ZERO, result.getAmountPerResident());
    }
}