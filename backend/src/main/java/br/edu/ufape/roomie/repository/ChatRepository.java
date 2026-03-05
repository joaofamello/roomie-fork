package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {
}

