package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Notification;
import br.edu.ufape.roomie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    long countByRecipientAndReadFalse(User recipient);
}

