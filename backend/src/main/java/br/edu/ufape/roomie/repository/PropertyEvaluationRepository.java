package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.PropertyEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyEvaluationRepository extends JpaRepository<PropertyEvaluation, Long> {
}

