package br.edu.ufape.roomie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import br.edu.ufape.roomie.dto.UpdateUserDTO;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserService userService;

  @Test
  @DisplayName("Deve atualizar apenas o nome do usuário e ignorar campos vazios")
  void deveAtualizarNomeComSucesso() {
    Long userId = 1L;
    User existingUser = new User();
    existingUser.setId(userId);
    existingUser.setName("Nome Antigo");
    existingUser.setEmail("antigo@email.com");

    UpdateUserDTO dto = new UpdateUserDTO();
    dto.setName("Nome Novo");
    dto.setEmail(" "); // Testa isBlank
    dto.setNewPassword(""); // Testa isBlank

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

    assertThrows(
        ResponseStatusException.class,
        () -> {
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
  @DisplayName("Nao deve fazer nada se todos os campos forem nulos ou vazios")
  void naoDeveFazerNadaSeTudoVazio() {
    Long userId = 1L;
    User existingUser = new User();
    existingUser.setId(userId);
    existingUser.setName("Nome Antigo");

    UpdateUserDTO dto = new UpdateUserDTO();
    dto.setName("   "); // isBlank na primeira chamada

    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

    User updatedUser = userService.updateProfile(userId, dto);

    assertThat(updatedUser.getName()).isEqualTo("Nome Antigo");
  }

  @Test
  @DisplayName(
      "Deve lançar erro se tentar mudar senha sem informar a senha atual (senha atual nula)")
  void deveFalharMudarSenhaSemSenhaAtualNula() {
    Long userId = 1L;
    User user = new User();
    user.setPassword("hash");

    UpdateUserDTO dto = new UpdateUserDTO();
    dto.setNewPassword("novasenha");
    dto.setCurrentPassword(null);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    assertThrows(
        ResponseStatusException.class,
        () -> {
          userService.updateProfile(userId, dto);
        });
  }

  @Test
  @DisplayName("Deve lançar erro se tentar mudar senha com senha atual em branco")
  void deveFalharMudarSenhaSemSenhaAtual() {
    Long userId = 1L;
    User user = new User();
    user.setPassword("hash");

    UpdateUserDTO dto = new UpdateUserDTO();
    dto.setNewPassword("novasenha");
    dto.setCurrentPassword("");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    assertThrows(
        ResponseStatusException.class,
        () -> {
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

    assertThrows(
        ResponseStatusException.class,
        () -> {
          userService.updateProfile(userId, dto);
        });
  }

  @Test
  @DisplayName("Deve lançar erro se intentar mudar email e o usuario não existir")
  void deveUserNotFound() {
    Long userId = 99L;
    UpdateUserDTO dto = new UpdateUserDTO();

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        ResponseStatusException.class,
        () -> {
          userService.updateProfile(userId, dto);
        });

    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve atualizar o email com sucesso")
  void deveAtualizarEmailComSucesso() {
    Long userId = 1L;
    User user = new User();
    user.setId(userId);
    user.setEmail("antigo@email.com");

    UpdateUserDTO dto = new UpdateUserDTO();
    dto.setEmail("novo@email.com");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.findByEmail("novo@email.com")).thenReturn(null);
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

    User updatedUser = userService.updateProfile(userId, dto);

    assertThat(updatedUser.getEmail()).isEqualTo("novo@email.com");
  }

  @Test
  @DisplayName("Não deve atualizar email se for igual ao anterior")
  void naoDeveAtualizarSeEmailIguais() {
    Long userId = 1L;
    User user = new User();
    user.setId(userId);
    user.setEmail("mesmo@email.com");

    UpdateUserDTO dto = new UpdateUserDTO();
    dto.setEmail("mesmo@email.com");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

    User updatedUser = userService.updateProfile(userId, dto);

    assertThat(updatedUser.getEmail()).isEqualTo("mesmo@email.com");
    verify(userRepository, never()).findByEmail(anyString());
  }

  @Test
  @DisplayName("Deve atualizar telefones ignorando nulos ou vazios")
  void deveAtualizarTelefones() {
    Long userId = 1L;
    User user = new User();
    user.setId(userId);
    user.addTelefone("81999999999"); // telefone antigo que deve ser limpo

    UpdateUserDTO dto = new UpdateUserDTO();
    dto.setPhones(java.util.Arrays.asList("81988888888", null, " ", "81977777777"));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

    User result = userService.updateProfile(userId, dto);

    assertThat(result.getTelefones())
        .extracting(br.edu.ufape.roomie.model.Telefone::getNumero)
        .containsExactlyInAnyOrder("81988888888", "81977777777");
  }

  @Test
  @DisplayName("Deve limpar telefones se lista vier sem elementos válidos mas for não nula")
  void deveLimparTelefones() {
    Long userId = 1L;
    User user = new User();
    user.setId(userId);
    user.addTelefone("81999999999");

    UpdateUserDTO dto = new UpdateUserDTO();
    dto.setPhones(java.util.Collections.emptyList());

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

    User result = userService.updateProfile(userId, dto);

    assertThat(result.getTelefones()).isEmpty();
  }
}
