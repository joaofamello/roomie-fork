package br.edu.ufape.roomie.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ufape.roomie.repository.StudentRepository;
import br.edu.ufape.roomie.repository.UserRepository;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

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