package br.edu.ufape.roomie.dto;

import br.edu.ufape.roomie.model.Expense;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseResponseDTO {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private Long registeredById;

    public ExpenseResponseDTO(Expense expense) {
        this.id = expense.getId();
        this.description = expense.getDescription();
        this.amount = expense.getAmount();
        this.expenseDate = expense.getExpenseDate();
        this.registeredById = expense.getRegisteredBy().getId();
    }
}