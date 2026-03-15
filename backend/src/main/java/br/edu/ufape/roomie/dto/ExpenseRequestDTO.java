package br.edu.ufape.roomie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ExpenseRequestDTO {
  @NotNull(message = "O ID do imóvel é obrigatório")
  private Long propertyId;

  @NotBlank(message = "A descrição é obrigatória")
  private String description;

  @NotNull(message = "O valor é obrigatório")
  @Positive(message = "O valor deve ser maior que zero")
  private BigDecimal amount;

  @NotNull(message = "A data da despesa é obrigatória")
  private LocalDate expenseDate;
}
