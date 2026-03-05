package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitRepository extends JpaRepository<Habit, Long> {
}

