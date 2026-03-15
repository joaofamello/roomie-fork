package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Expense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
  List<Expense> findByPropertyId(Long propertyId);
}
