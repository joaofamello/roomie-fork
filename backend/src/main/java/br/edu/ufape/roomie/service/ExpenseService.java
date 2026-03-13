package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.ExpenseRequestDTO;
import br.edu.ufape.roomie.dto.ExpenseResponseDTO;
import br.edu.ufape.roomie.dto.ExpenseSummaryDTO;
import br.edu.ufape.roomie.enums.ContractStatus;
import br.edu.ufape.roomie.model.Contract;
import br.edu.ufape.roomie.model.Expense;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.ContractRepository;
import br.edu.ufape.roomie.repository.ExpenseRepository;
import br.edu.ufape.roomie.repository.PropertyRepository;
import br.edu.ufape.roomie.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final PropertyRepository propertyRepository;
    private final ContractRepository contractRepository;
    private final StudentRepository studentRepository;

    public ExpenseService(ExpenseRepository expenseRepository, PropertyRepository propertyRepository,
                          ContractRepository contractRepository, StudentRepository studentRepository) {
        this.expenseRepository = expenseRepository;
        this.propertyRepository = propertyRepository;
        this.contractRepository = contractRepository;
        this.studentRepository = studentRepository;
    }

    private Student getAuthenticatedStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
        }
        User user = (User) auth.getPrincipal();
        return studentRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas estudantes podem acessar despesas."));
    }

    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto) {
        Student student = getAuthenticatedStudent();

        Property property = propertyRepository.findById(dto.getPropertyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));

        boolean isResident = contractRepository.existsByPropertyIdAndStudentIdAndStatusIn(
                property.getId(), student.getId(), List.of(ContractStatus.ACTIVE));

        if (!isResident) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não é um morador ativo desta moradia.");
        }

        Expense expense = new Expense();
        expense.setProperty(property);
        expense.setRegisteredBy(student);
        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setExpenseDate(dto.getExpenseDate());

        return new ExpenseResponseDTO(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public ExpenseSummaryDTO getExpensesByProperty(Long propertyId) {
        Student student = getAuthenticatedStudent();

        boolean isResident = contractRepository.existsByPropertyIdAndStudentIdAndStatusIn(
                propertyId, student.getId(), List.of(ContractStatus.ACTIVE));

        if (!isResident) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas moradores podem ver as despesas.");
        }

        List<Expense> expenses = expenseRepository.findByPropertyId(propertyId);
        List<ExpenseResponseDTO> dtos = expenses.stream().map(ExpenseResponseDTO::new).collect(Collectors.toList());

        BigDecimal totalAmount = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Contract> activeContracts = contractRepository.findByPropertyIdAndStatus(propertyId, ContractStatus.ACTIVE);
        int numberOfResidents = activeContracts.size();

        BigDecimal amountPerResident = BigDecimal.ZERO;
        if (numberOfResidents > 0 && totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            amountPerResident = totalAmount.divide(BigDecimal.valueOf(numberOfResidents), 2, RoundingMode.HALF_UP);
        }

        ExpenseSummaryDTO summary = new ExpenseSummaryDTO();
        summary.setExpenses(dtos);
        summary.setTotalAmount(totalAmount);
        summary.setNumberOfResidents(numberOfResidents);
        summary.setAmountPerResident(amountPerResident);

        return summary;
    }
}