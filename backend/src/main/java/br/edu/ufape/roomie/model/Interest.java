package br.edu.ufape.roomie.model;

import br.edu.ufape.roomie.enums.InterestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "interesse", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_estudante", "id_imovel"})
})
@Data
@NoArgsConstructor
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interesse")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_estudante", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "id_imovel", nullable = false)
    private Property property;

    @Column(name = "data_interesse", nullable = false)
    private LocalDateTime interestDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "status_interesse")
    private InterestStatus status;

    public Interest(Student student, Property property) {
        this.student = student;
        this.property = property;
        this.interestDate = LocalDateTime.now();
        this.status = InterestStatus.PENDING;
    }
}