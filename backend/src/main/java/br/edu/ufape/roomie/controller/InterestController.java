package br.edu.ufape.roomie.controller;

import br.edu.ufape.roomie.dto.InterestSummaryDTO;
import br.edu.ufape.roomie.enums.InterestStatus;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.StudentRepository;
import br.edu.ufape.roomie.service.InterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.Optional;

@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;
    private final StudentRepository studentRepository;

    @PostMapping("/{id}/interest")
    public ResponseEntity<String> expressInterest(
            @PathVariable("id") Long propertyId,
            @AuthenticationPrincipal User loggedInUser) {

        Optional<Student> optStudent = studentRepository.findById(loggedInUser.getId());
        if (optStudent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso negado: Apenas estudantes podem demonstrar interesse em imóveis.");
        }

        try {
            interestService.registerInterest(propertyId, optStudent.get());
            return ResponseEntity.ok("Interesse registrado com sucesso. O administrador foi notificado.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }

    @GetMapping("/{id}/interest/check")
    public ResponseEntity<?> checkInterest(
            @PathVariable("id") Long propertyId,
            @AuthenticationPrincipal User loggedInUser) {

        if (!(loggedInUser instanceof Student student)) {
            return ResponseEntity.ok(java.util.Map.of("hasInterest", false));
        }

        boolean hasInterest = interestService.hasInterest(propertyId, student);
        return ResponseEntity.ok(java.util.Map.of("hasInterest", hasInterest));
    }

    @GetMapping("/{id}/interests")
    public ResponseEntity<?> getInterests(
            @PathVariable("id") Long propertyId,
            @AuthenticationPrincipal User loggedInUser) {
        try {
            List<InterestSummaryDTO> interests = interestService.listInterestsForProperty(propertyId, loggedInUser);
            return ResponseEntity.ok(interests);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/interests/{interestId}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long interestId,
            @RequestParam("status") InterestStatus status,
            @AuthenticationPrincipal User loggedInUser) {
        try {
            interestService.updateInterestStatus(interestId, status, loggedInUser);
            return ResponseEntity.ok("Status da proposta atualizado para " + status);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}