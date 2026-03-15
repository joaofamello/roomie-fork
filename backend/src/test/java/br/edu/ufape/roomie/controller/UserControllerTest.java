package br.edu.ufape.roomie.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import br.edu.ufape.roomie.dto.UpdateUserDTO;
import br.edu.ufape.roomie.enums.UserGender;
import br.edu.ufape.roomie.enums.UserRole;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.projection.OwnerReportView;
import br.edu.ufape.roomie.repository.UserRepository;
import br.edu.ufape.roomie.service.TokenService;
import br.edu.ufape.roomie.service.UserService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = UserController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
class UserControllerTest {

  @Autowired private MockMvc mvc;

  @Autowired private JacksonTester<UpdateUserDTO> updateUserDTOTester;

  @MockitoBean private UserService userService;

  @MockitoBean private UserRepository userRepository;

  @MockitoBean private TokenService tokenService;

  @MockitoBean private UserDetailsService userDetailsService;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("PATCH /api/user/profile deve retornar 200 com dados atualizados")
  void updateProfile_returns200() throws Exception {
    User currentUser = new User();
    currentUser.setId(1L);
    currentUser.setEmail("user@ufape.edu.br");
    currentUser.setName("João");
    currentUser.setGender(UserGender.MALE);
    currentUser.setRole(UserRole.USER);
    currentUser.setTelefones(new ArrayList<>());

    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                currentUser, null, currentUser.getAuthorities()));

    UpdateUserDTO dto = new UpdateUserDTO();
    dto.setName("João Atualizado");

    User updatedUser = new User();
    updatedUser.setId(1L);
    updatedUser.setName("João Atualizado");
    updatedUser.setEmail("user@ufape.edu.br");
    updatedUser.setGender(UserGender.MALE);
    updatedUser.setRole(UserRole.USER);
    updatedUser.setTelefones(new ArrayList<>());

    when(userService.updateProfile(eq(1L), any(UpdateUserDTO.class))).thenReturn(updatedUser);

    var response =
        mvc.perform(
                patch("/api/user/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateUserDTOTester.write(dto).getJson()))
            .andReturn()
            .getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).contains("João Atualizado");
  }

  @Test
  @DisplayName("GET /api/user/owners/report deve retornar 200 com lista de proprietários")
  void getOwnersReport_returns200() throws Exception {
    OwnerReportView report = mock(OwnerReportView.class);
    when(userRepository.findOwnerReports()).thenReturn(List.of(report));

    var response = mvc.perform(get("/api/user/owners/report")).andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
