package br.edu.ufape.roomie.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ExpenseSummaryDTO {
    private List<ExpenseResponseDTO> expenses;
    private BigDecimal totalAmount;
    private int numberOfResidents;
    private BigDecimal amountPerResident;
}