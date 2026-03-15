package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.PropertyEvaluation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropertyEvaluationRepository extends JpaRepository<PropertyEvaluation, Long> {

  List<PropertyEvaluation> findByPropertyId(@Param("id") Long propertyId);

  Optional<PropertyEvaluation> findByPropertyIdAndStudentId(Long propertyId, Long studentId);

  @Query(
      "SELECT COALESCE(AVG(e.rating), 0) FROM PropertyEvaluation e WHERE e.property.id = :propertyId")
  Double findAverageRatingByPropertyId(@Param("propertyId") Long propertyId);

  @Query("SELECT COUNT(e) FROM PropertyEvaluation e WHERE e.property.id = :propertyId")
  Long countByPropertyId(@Param("propertyId") Long propertyId);
}
