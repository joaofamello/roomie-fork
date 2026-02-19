package br.edu.ufape.roomie.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Imagem_Imovel")
@Data
@NoArgsConstructor
public class PropertyPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagem")
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_imovel", nullable = false)
    private Property property;

    @Column(nullable = false, name = "caminho_imagem")
    private String path;
}