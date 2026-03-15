package br.edu.ufape.roomie.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EvaluationRequestDTO {

  @NotNull(message = "A nota é obrigatória")
  @Min(value = 1, message = "A nota mínima é 1")
  @Max(value = 5, message = "A nota máxima é 5")
  private Integer rating;
}
