package br.edu.ufape.roomie.model;

import br.edu.ufape.roomie.enums.PropertyStatus;
import br.edu.ufape.roomie.enums.PropertyType;
import br.edu.ufape.roomie.enums.UserGender;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "imovel")
@Data
@NoArgsConstructor
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imovel")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_proprietario", nullable = false)
    private User owner;

    @Column(nullable = false, name = "titulo")
    private String title;

    @Column(columnDefinition = "TEXT", name = "descricao")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo", nullable = false, columnDefinition = "tipo_imovel")
    private PropertyType type;

    @Column(nullable = false, name = "preco", precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "genero_moradores", nullable = false, columnDefinition = "tipo_genero")
    private UserGender gender;

    @Column(name = "aceita_animais", nullable = false)
    private Boolean acceptAnimals;

    @Column(name = "tem_garagem", nullable = false)
    private Boolean hasGarage;

    @Column(name = "vagas_disponiveis", nullable = false)
    private Integer availableVacancies;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "status_anuncio")
    private PropertyStatus status = PropertyStatus.DRAFT;

    @OneToOne(mappedBy = "property", cascade = CascadeType.ALL)
    private Address address;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyPhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyEvaluation> evaluations = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contract> contracts = new ArrayList<>();
}