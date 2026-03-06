package br.edu.ufape.roomie.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ufape.roomie.repository.StudentRepository;
import br.edu.ufape.roomie.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Transactional
    public void promoteUserToStudent(Long userId, String major, String institution) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado!"));

        studentRepository.promoteUserToStudent(userId, major, institution);
    }

    @Transactional
    public void updateStudentProfile(Long userId, String major, String institution) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado!"));

        int linhasAfetadas = studentRepository.updateStudentProfile(userId, major, institution);

        if (linhasAfetadas == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este usuário ainda não possui um perfil de estudante para ser atualizado.");
        }
    }
}