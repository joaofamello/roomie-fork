package br.edu.ufape.roomie.dto;

import br.edu.ufape.roomie.enums.ContractStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractResponseDTO {
  private Long id;
  private Long propertyId;
  private String propertyTitle;
  private Long studentId;
  private String studentName;
  private Long ownerId;
  private String ownerName;
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal price;
  private ContractStatus status;
}
