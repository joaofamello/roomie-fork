package br.edu.ufape.roomie.model;

import br.edu.ufape.roomie.enums.PropertyStatus;
import br.edu.ufape.roomie.enums.PropertyType;
import br.edu.ufape.roomie.enums.UserGender;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Imovel")
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
    @Column(name = "tipo")
    private PropertyType type;

    @Column(nullable = false, name = "preco")
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero_moradores", nullable = false)
    private UserGender gender;

    @Column(name = "aceita_animais", nullable = false)
    private Boolean acceptAnimals;

    @Column(name = "vagas_disponiveis", nullable = false)
    private Integer availableVacancies;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PropertyStatus status = PropertyStatus.DRAFT;

    @OneToOne(mappedBy = "property", cascade = CascadeType.ALL)
    private Address address;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyPhoto> photos = new ArrayList<>();
}