package br.edu.ufape.roomie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "mensagem")
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensagem")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_remetente", nullable = false)
    private User sender;

    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "timestamp_mensagem")
    private LocalDateTime timestamp;

    @Column(name = "lida")
    private Boolean read;
}
