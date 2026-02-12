package br.edu.ufape.roomie.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "telefone")
@Data
@NoArgsConstructor
public class Telefone{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_telefone")
    private long idTelefone;

    @Column(name = "numero")
    private String numero;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private User usuario;

    public Telefone(String numero, User usuario) {
        this.numero = numero;
        this.usuario = usuario;
    }
}