package br.edu.ufape.roomie.dto;

import br.edu.ufape.roomie.enums.InterestStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterestSummaryDTO {
  private Long interestId;
  private Long studentId;
  private String studentName;
  private String studentEmail;
  private String major;
  private String institution;
  private InterestStatus status;
  private LocalDateTime interestDate;
}
