package br.edu.ufape.roomie.controller;

import br.edu.ufape.roomie.dto.UserDTO;
import br.edu.ufape.roomie.dto.UserResponseDTO;
import br.edu.ufape.roomie.enums.UserGender;
import br.edu.ufape.roomie.enums.UserRole;
import br.edu.ufape.roomie.service.AuthService;
import br.edu.ufape.roomie.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<UserDTO> userDTOJacksonTester;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Test
    @DisplayName("Deveria retornar 201 ao registrar um usuário válido")
    void  testaRegistroComSucesso() throws Exception{
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

        var response = mvc.perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userDTOJacksonTester.write(userDTO).getJson())
        ).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
    }

}
