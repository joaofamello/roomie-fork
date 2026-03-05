package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, Long> {
}

