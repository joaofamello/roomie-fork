package br.edu.ufape.roomie.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "despesa_compartilhada")
@Data
@NoArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_despesa")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_imovel", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudante_registro", nullable = false)
    private Student registeredBy;

    @Column(name = "descricao", nullable = false)
    private String description;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "data_despesa", nullable = false)
    private LocalDate expenseDate;
}