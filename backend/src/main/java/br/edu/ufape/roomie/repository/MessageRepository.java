package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}

