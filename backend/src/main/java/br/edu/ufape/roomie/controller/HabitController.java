package br.edu.ufape.roomie.controller;

import br.edu.ufape.roomie.dto.HabitRequestDTO;
import br.edu.ufape.roomie.dto.HabitResponseDTO;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.StudentRepository;
import br.edu.ufape.roomie.service.HabitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;
    private final StudentRepository studentRepository;

    @GetMapping
    public ResponseEntity<?> getMyHabits(@AuthenticationPrincipal User loggedInUser) {
        Optional<Student> optStudent = studentRepository.findById(loggedInUser.getId());
        if (optStudent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso negado: Apenas estudantes podem gerenciar hábitos.");
        }

        HabitResponseDTO response = habitService.getHabitByStudent(optStudent.get());
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> saveHabits(
            @AuthenticationPrincipal User loggedInUser,
            @RequestBody HabitRequestDTO dto
    ) {
        Optional<Student> optStudent = studentRepository.findById(loggedInUser.getId());
        if (optStudent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso negado: Apenas estudantes podem gerenciar hábitos.");
        }

        HabitResponseDTO response = habitService.createOrUpdateHabit(optStudent.get(), dto);
        return ResponseEntity.ok(response);
    }
}
