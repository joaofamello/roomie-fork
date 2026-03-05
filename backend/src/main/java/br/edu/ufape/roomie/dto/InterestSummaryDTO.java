package br.edu.ufape.roomie.dto;

import br.edu.ufape.roomie.enums.InterestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterestSummaryDTO {
    private Long interestId;
    private Long studentId;
    private String studentName;
    private String major;
    private String institution;
    private InterestStatus status;
    private LocalDateTime interestDate;
}