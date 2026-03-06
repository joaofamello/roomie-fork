package br.edu.ufape.roomie.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "notificacao")
@Data
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacao")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_destinatario", nullable = false)
    private User recipient;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "lida", nullable = false)
    private boolean read = false;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime createdAt;

    public Notification(User recipient, String message) {
        this.recipient = recipient;
        this.message = message;
        this.createdAt = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
        this.read = false;
    }
}


