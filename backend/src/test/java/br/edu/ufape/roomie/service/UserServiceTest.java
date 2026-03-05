package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.UpdateUserDTO;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Deve atualizar apenas o nome do usuário")
    void deveAtualizarNomeComSucesso() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Nome Antigo");
        existingUser.setEmail("antigo@email.com");

        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setName("Nome Novo");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User updatedUser = userService.updateProfile(userId, dto);

        assertThat(updatedUser.getName()).isEqualTo("Nome Novo");
        assertThat(updatedUser.getEmail()).isEqualTo("antigo@email.com");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar usar email já existente")
    void deveFalharEmailDuplicado() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("meu@email.com");

        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setEmail("outro@email.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("outro@email.com")).thenReturn(new User());

        assertThrows(ResponseStatusException.class, () -> {
            userService.updateProfile(userId, dto);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar senha com sucesso se a senha atual estiver correta")
    void deveAtualizarSenhaComSucesso() {
        Long userId = 1L;
        User user = new User();
        user.setPassword("senhaHashAntiga");

        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setCurrentPassword("123456");
        dto.setNewPassword("novasenha123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "senhaHashAntiga")).thenReturn(true);
        when(passwordEncoder.encode("novasenha123")).thenReturn("novoHash");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.updateProfile(userId, dto);

        assertThat(result.getPassword()).isEqualTo("novoHash");
    }

    @Test
    @DisplayName("Deve lançar erro se tentar mudar senha sem informar a senha atual")
    void deveFalharMudarSenhaSemSenhaAtual() {
        Long userId = 1L;
        User user = new User();
        user.setPassword("hash");

        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setNewPassword("novasenha");
        dto.setCurrentPassword("");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> {
            userService.updateProfile(userId, dto);
        });
    }

    @Test
    @DisplayName("Deve lançar erro se a senha atual informada estiver incorreta")
    void deveFalharSenhaAtualIncorreta() {
        Long userId = 1L;
        User user = new User();
        user.setPassword("hashReal");

        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setCurrentPassword("senhaErrada");
        dto.setNewPassword("nova");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaErrada", "hashReal")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> {
            userService.updateProfile(userId, dto);
        });
    }
}