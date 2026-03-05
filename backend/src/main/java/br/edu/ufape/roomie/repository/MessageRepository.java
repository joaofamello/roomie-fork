package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatIdOrderByTimestampAsc(Long chatId);

    long countByChatIdAndReadFalseAndSenderIdNot(Long chatId, Long userId);

    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.chat.id = :chatId AND m.sender.id != :userId AND m.read = false")
    void markMessagesAsReadByChatIdAndNotSenderId(@Param("chatId") Long chatId, @Param("userId") Long userId);
}

