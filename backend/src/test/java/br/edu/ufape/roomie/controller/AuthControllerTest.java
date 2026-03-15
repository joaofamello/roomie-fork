package br.edu.ufape.roomie.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import br.edu.ufape.roomie.dto.LoginDTO;
import br.edu.ufape.roomie.dto.UserDTO;
import br.edu.ufape.roomie.dto.UserResponseDTO;
import br.edu.ufape.roomie.enums.UserGender;
import br.edu.ufape.roomie.enums.UserRole;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.service.AuthService;
import br.edu.ufape.roomie.service.TokenService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
class AuthControllerTest {

  @Autowired private MockMvc mvc;

  @Autowired private JacksonTester<UserDTO> userDTOJacksonTester;

  @Autowired private JacksonTester<LoginDTO> loginDTOJacksonTester;

  @MockitoBean private AuthService authService;

  @MockitoBean private TokenService tokenService;

  @MockitoBean private AuthenticationManager authenticationManager;

  @Test
  @DisplayName("Deveria retornar 201 ao registrar um usuário válido")
  void testaRegistroComSucesso() throws Exception {
    UserDTO userDTO = new UserDTO();
    userDTO.setName("Nome teste");
    userDTO.setEmail("teste@gmail.com");
    userDTO.setPassword("123456");
    userDTO.setCpf("17381521032");
    userDTO.setGender(UserGender.MALE);
    userDTO.setRole(UserRole.USER);
    userDTO.setPhones(List.of("87996448912"));

    UserResponseDTO responseDTO = new UserResponseDTO();
    responseDTO.setId(1L);
    responseDTO.setName(userDTO.getName());
    responseDTO.setEmail(userDTO.getEmail());
    responseDTO.setRole(userDTO.getRole());

    when(authService.register(any(UserDTO.class))).thenReturn(responseDTO);

    var response =
        mvc.perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userDTOJacksonTester.write(userDTO).getJson()))
            .andReturn()
            .getResponse();
    assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
  }

  @Test
  @DisplayName("Deveria retornar 200 e token ao fazer login com credenciais válidas")
  void testaLoginComSucesso() throws Exception {
    LoginDTO loginDTO = new LoginDTO();
    loginDTO.setEmail("teste@gmail.com");
    loginDTO.setPassword("123456");

    User user = new User();
    user.setId(1L);
    user.setEmail(loginDTO.getEmail());
    user.setPassword(loginDTO.getPassword());
    user.setName("Nome teste");
    user.setCpf("17381521032");
    user.setGender(UserGender.MALE);
    user.setRole(UserRole.USER);

    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    when(authenticationManager.authenticate(any())).thenReturn(authentication);
    when(tokenService.generateToken(user)).thenReturn("token-123");

    var response =
        mvc.perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginDTOJacksonTester.write(loginDTO).getJson()))
            .andReturn()
            .getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).contains("token-123");
  }
}
