package br.edu.ufape.roomie.model;

import br.edu.ufape.roomie.enums.ContractStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "contrato_locacao")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    private Long idContract;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_imovel", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudante", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proprietario", nullable = false)
    private User owner;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate startDate;

    @Column(name = "data_fim", nullable = false)
    private LocalDate endDate;

    @Column(name = "valor_aluguel", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status_contrato", nullable = false, columnDefinition = "status_contrato")
    private ContractStatus status;

    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("A data de início deve ser anterior à data de término.");
        }
    }
}
