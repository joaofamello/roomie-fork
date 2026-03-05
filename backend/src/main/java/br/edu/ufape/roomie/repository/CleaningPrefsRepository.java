package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.CleaningPrefs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CleaningPrefsRepository extends JpaRepository<CleaningPrefs, Long> {
}

