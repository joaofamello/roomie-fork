package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.EvaluationRequestDTO;
import br.edu.ufape.roomie.dto.EvaluationResponseDTO;
import br.edu.ufape.roomie.dto.EvaluationSummaryDTO;
import br.edu.ufape.roomie.enums.ContractStatus;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.PropertyEvaluation;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.ContractRepository;
import br.edu.ufape.roomie.repository.PropertyEvaluationRepository;
import br.edu.ufape.roomie.repository.PropertyRepository;
import br.edu.ufape.roomie.repository.StudentRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EvaluationService {

  private final PropertyEvaluationRepository evaluationRepository;
  private final PropertyRepository propertyRepository;
  private final StudentRepository studentRepository;
  private final ContractRepository contractRepository;

  public EvaluationService(
      PropertyEvaluationRepository evaluationRepository,
      PropertyRepository propertyRepository,
      StudentRepository studentRepository,
      ContractRepository contractRepository) {
    this.evaluationRepository = evaluationRepository;
    this.propertyRepository = propertyRepository;
    this.studentRepository = studentRepository;
    this.contractRepository = contractRepository;
  }

  @Transactional
  public EvaluationResponseDTO createEvaluation(Long propertyId, EvaluationRequestDTO dto) {
    User user = getAuthenticatedUser();

    Student student =
        studentRepository
            .findByEmail(user.getEmail())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Apenas estudantes podem avaliar imóveis."));

    Property property =
        propertyRepository
            .findById(propertyId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));

    if (property.getOwner().getId().equals(student.getId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Você não pode avaliar seu próprio imóvel.");
    }

    boolean hasContract =
        contractRepository.existsByPropertyIdAndUserIdAndStatusIn(
            propertyId, student.getId(), List.of(ContractStatus.ACTIVE, ContractStatus.FINISHED));
    if (!hasContract) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Apenas estudantes com contrato neste imóvel podem avaliá-lo.");
    }

    evaluationRepository
        .findByPropertyIdAndStudentId(propertyId, student.getId())
        .ifPresent(
            existing -> {
              throw new ResponseStatusException(
                  HttpStatus.CONFLICT, "Você já avaliou este imóvel.");
            });

    PropertyEvaluation evaluation = new PropertyEvaluation();
    evaluation.setProperty(property);
    evaluation.setStudent(student);
    evaluation.setRating(dto.getRating());
    evaluation.setTimestamp(LocalDateTime.now());

    PropertyEvaluation saved = evaluationRepository.save(evaluation);
    return toResponseDTO(saved);
  }

  @Transactional(readOnly = true)
  public EvaluationSummaryDTO getEvaluationsByProperty(Long propertyId) {
    propertyRepository
        .findById(propertyId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));

    List<PropertyEvaluation> evaluations = evaluationRepository.findByPropertyId(propertyId);
    Double average = evaluationRepository.findAverageRatingByPropertyId(propertyId);
    Long total = evaluationRepository.countByPropertyId(propertyId);

    List<EvaluationResponseDTO> dtos = evaluations.stream().map(this::toResponseDTO).toList();

    return new EvaluationSummaryDTO(Math.round(average * 100.0) / 100.0, total, dtos);
  }

  @Transactional(readOnly = true)
  public boolean hasUserEvaluated(Long propertyId) {
    User user = getAuthenticatedUser();
    return evaluationRepository.findByPropertyIdAndStudentId(propertyId, user.getId()).isPresent();
  }

  private EvaluationResponseDTO toResponseDTO(PropertyEvaluation evaluation) {
    return new EvaluationResponseDTO(
        evaluation.getIdEvaluation(),
        evaluation.getProperty().getId(),
        evaluation.getStudent().getId(),
        evaluation.getStudent().getName(),
        evaluation.getRating(),
        evaluation.getTimestamp());
  }

  private User getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
    }
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof User)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
    }
    return (User) principal;
  }
}
