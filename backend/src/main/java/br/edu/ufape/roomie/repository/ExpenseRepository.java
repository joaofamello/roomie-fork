package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByPropertyId(Long propertyId);
}