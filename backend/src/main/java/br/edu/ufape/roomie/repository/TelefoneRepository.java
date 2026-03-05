package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Telefone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelefoneRepository extends JpaRepository<Telefone, Long> {
}

