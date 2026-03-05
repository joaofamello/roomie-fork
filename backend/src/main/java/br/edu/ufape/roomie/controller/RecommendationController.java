package br.edu.ufape.roomie.controller;

import br.edu.ufape.roomie.dto.RoommateRecommendationDTO;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.StudentRepository;
import br.edu.ufape.roomie.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final StudentRepository studentRepository;

    @GetMapping("/roommates")
    public ResponseEntity<?> getRoommateRecommendations(@AuthenticationPrincipal User loggedInUser) {
        Optional<Student> optStudent = studentRepository.findById(loggedInUser.getId());
        if (optStudent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso negado: Apenas estudantes podem receber recomendações de colegas de quarto.");
        }

        try {
            List<RoommateRecommendationDTO> recommendations = recommendationService.getRecommendations(optStudent.get());
            return ResponseEntity.ok(recommendations);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}