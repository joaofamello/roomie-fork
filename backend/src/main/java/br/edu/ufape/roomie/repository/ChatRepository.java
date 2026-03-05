package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Chat;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByUser(User owner);

    List<Chat> findByStudent(Student student);

    Optional<Chat> findByStudentIdAndUserIdAndPropertyId(Long studentId, Long userId, Long propertyId);
}

