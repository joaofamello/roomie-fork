package br.edu.ufape.roomie.controller;

import br.edu.ufape.roomie.dto.EvaluationRequestDTO;
import br.edu.ufape.roomie.dto.EvaluationResponseDTO;
import br.edu.ufape.roomie.dto.EvaluationSummaryDTO;
import br.edu.ufape.roomie.service.EvaluationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/properties/{propertyId}/evaluations")
public class EvaluationController {

  private final EvaluationService evaluationService;

  public EvaluationController(EvaluationService evaluationService) {
    this.evaluationService = evaluationService;
  }

  @PostMapping
  public ResponseEntity<EvaluationResponseDTO> create(
      @PathVariable Long propertyId, @Valid @RequestBody EvaluationRequestDTO dto) {
    EvaluationResponseDTO response = evaluationService.createEvaluation(propertyId, dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<EvaluationSummaryDTO> getByProperty(@PathVariable Long propertyId) {
    return ResponseEntity.ok(evaluationService.getEvaluationsByProperty(propertyId));
  }

  @GetMapping("/check")
  public ResponseEntity<Boolean> hasEvaluated(@PathVariable Long propertyId) {
    return ResponseEntity.ok(evaluationService.hasUserEvaluated(propertyId));
  }
}
