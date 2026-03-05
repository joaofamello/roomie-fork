package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Hobby;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HobbyRepository extends JpaRepository<Hobby, Long> {
}

