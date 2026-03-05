package br.edu.ufape.roomie.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@BatchSize(size = 30)
@Entity
@Table(name = "endereco")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_endereco")
    private Long idAddress;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_imovel",
            referencedColumnName = "id_imovel",
            nullable = false,
            unique = true
    )
    private Property property;

    @Column(name = "rua", nullable = false, length = 100)
    private String street;
    @Column(name = "bairro", nullable = false, length = 100)
    private String district;
    @Column(name = "numero", nullable = false, length = 10)
    private String number;
    @Column(name = "cidade", nullable = false, length = 50)
    private String city;
    @Column(name = "estado", nullable = false, length = 50)
    private String state;
    @Column(nullable = false, length = 20)
    private String cep;
}
