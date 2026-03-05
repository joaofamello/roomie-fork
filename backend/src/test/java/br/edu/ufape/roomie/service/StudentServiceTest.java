package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.StudentRepository;
import br.edu.ufape.roomie.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    @DisplayName("Deve promover usuário a estudante com sucesso")
    void shouldPromoteUserToStudentSuccessfully() {
        var userId = 1L;
        var user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        studentService.promoteUserToStudent(userId, "Ciência da Computação", "UFAPE");

        verify(studentRepository, times(1)).promoteUserToStudent(userId, "Ciência da Computação", "UFAPE");
    }

    @Test
    @DisplayName("Deve lançar erro 404 ao tentar promover usuário inexistente")
    void shouldThrowNotFoundWhenPromotingNonexistentUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            studentService.promoteUserToStudent(99L, "Curso", "Inst");
        });

        verify(studentRepository, never()).promoteUserToStudent(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve atualizar perfil de estudante com sucesso")
    void shouldUpdateStudentProfileSuccessfully() {
        var userId = 1L;
        var user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(studentRepository.updateStudentProfile(anyLong(), anyString(), anyString())).thenReturn(1);

        studentService.updateStudentProfile(userId, "Curso", "Inst");
        verify(studentRepository, times(1)).updateStudentProfile(userId, "Curso", "Inst");
    }

    @Test
    @DisplayName("Deve lançar erro 400 se tentar atualizar perfil de quem não é estudante")
    void deveFalharAtualizacaoSeNaoForEstudante() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(studentRepository.updateStudentProfile(anyLong(), anyString(), anyString())).thenReturn(0);

        // Execução e Verificação
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            studentService.updateStudentProfile(userId, "Curso", "Inst");
        });

        assertEquals(400, exception.getStatusCode().value());
    }
}
