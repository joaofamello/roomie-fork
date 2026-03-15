package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Interest;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.Student;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {
  boolean existsByStudentAndProperty(Student student, Property property);

  List<Interest> findByPropertyId(Long propertyId);
}
