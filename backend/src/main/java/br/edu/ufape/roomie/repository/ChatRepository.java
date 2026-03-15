package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Chat;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {

  List<Chat> findByUser(User owner);

  List<Chat> findByStudent(Student student);

  /** Busca chats onde o usuário é o proprietário usando ID */
  List<Chat> findByUserId(Long userId);

  /** Busca chats onde o usuário é o estudante interessado usando ID */
  List<Chat> findByStudentId(Long studentId);

  Optional<Chat> findByStudentIdAndUserIdAndPropertyId(
      Long studentId, Long userId, Long propertyId);
}
