package br.edu.ufape.roomie.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ufape.roomie.enums.ContractStatus;
import br.edu.ufape.roomie.model.Contract;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    Optional<Contract> findByPropertyIdAndStudentIdAndStatus(Long propertyId, Long studentId, ContractStatus status);
    List<Contract> findByChatId(Long chatId);
    boolean existsByPropertyIdAndStudentIdAndStatusIn(Long propertyId, Long studentId, java.util.List<ContractStatus> statuses);
    List<Contract> findByPropertyIdAndStatus(Long propertyId, ContractStatus status);
}

