package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.enums.ContractStatus;
import br.edu.ufape.roomie.model.Contract;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, Long> {
  Optional<Contract> findByPropertyIdAndStudentIdAndStatus(
      Long propertyId, Long studentId, ContractStatus status);

  List<Contract> findByChatId(Long chatId);

  @org.springframework.data.jpa.repository.Query(
      "SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Contract c WHERE c.property.id = :propertyId AND (c.student.id = :userId OR c.owner.id = :userId) AND c.status IN :statuses")
  boolean existsByPropertyIdAndUserIdAndStatusIn(
      @org.springframework.data.repository.query.Param("propertyId") Long propertyId,
      @org.springframework.data.repository.query.Param("userId") Long userId,
      @org.springframework.data.repository.query.Param("statuses")
          java.util.List<ContractStatus> statuses);

  List<Contract> findByPropertyIdAndStatus(Long propertyId, ContractStatus status);
}
