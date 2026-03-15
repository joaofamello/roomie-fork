package br.edu.ufape.roomie.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractRequestDTO {
  private Long chatId;
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal price;
}
