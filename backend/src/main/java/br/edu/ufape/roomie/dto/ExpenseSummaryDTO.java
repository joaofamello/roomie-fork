package br.edu.ufape.roomie.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class ExpenseSummaryDTO {
  private List<ExpenseResponseDTO> expenses;
  private BigDecimal totalAmount;
  private int numberOfResidents;
  private BigDecimal amountPerResident;
}
