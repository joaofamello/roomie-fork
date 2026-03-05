package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.UserDTO;
import br.edu.ufape.roomie.dto.UserResponseDTO;
import br.edu.ufape.roomie.enums.UserGender;
import br.edu.ufape.roomie.enums.UserRole;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Deve registrar um usuário com senha criptografada e retornar DTO com sucesso")
    void testaRegistroComSucesso() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("usuario teste");
        userDTO.setEmail("teste@ufape.edu.br");
        userDTO.setPassword("123456");
        userDTO.setRole(UserRole.USER);
        userDTO.setGender(UserGender.MALE);
        userDTO.setPhones(List.of("87999999999"));

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName(userDTO.getName());
        savedUser.setEmail(userDTO.getEmail());

        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDTO response = authService.register(userDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User userSalvoNoBanco = userCaptor.getValue();

        assertThat(userSalvoNoBanco.getRole()).isEqualTo(UserRole.USER);
        assertThat(userSalvoNoBanco.getName()).isEqualTo("usuario teste");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("teste@ufape.edu.br");

        verify(passwordEncoder).encode("123456");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar email já existente")
    void testaExcecaoEmailJaExistente() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("duplicado@ufape.edu.br");

        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(new User());

        assertThrows(ResponseStatusException.class, () -> {
            authService.register(userDTO);
        });
        verify(userRepository, never()).save(any(User.class));
    }
}
