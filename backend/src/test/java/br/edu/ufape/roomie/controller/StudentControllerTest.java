package br.edu.ufape.roomie.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import br.edu.ufape.roomie.dto.StudentDTO;
import br.edu.ufape.roomie.projection.StudentContactView;
import br.edu.ufape.roomie.projection.StudentEngagementView;
import br.edu.ufape.roomie.repository.StudentRepository;
import br.edu.ufape.roomie.service.StudentService;
import br.edu.ufape.roomie.service.TokenService;
import java.util.List;
import java.util.Optional;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = StudentController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
class StudentControllerTest {

  @Autowired private MockMvc mvc;

  @Autowired private JacksonTester<StudentDTO> studentDTOTester;

  @MockitoBean private StudentService studentService;

  @MockitoBean private StudentRepository studentRepository;

  @MockitoBean private TokenService tokenService;

  @MockitoBean private UserDetailsService userDetailsService;

  @Test
  @DisplayName("POST /api/students/profile deve retornar 201 ao criar perfil de estudante")
  void createStudentProfile_returns201() throws Exception {
    StudentDTO dto = new StudentDTO();
    dto.setUserId(1L);
    dto.setMajor("Ciência da Computação");
    dto.setInstitution("UFAPE");

    doNothing().when(studentService).promoteUserToStudent(1L, "Ciência da Computação", "UFAPE");

    var response =
        mvc.perform(
                post("/api/students/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(studentDTOTester.write(dto).getJson()))
            .andReturn()
            .getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
    assertThat(response.getContentAsString()).contains("sucesso");
  }

  @Test
  @DisplayName("PUT /api/students/profile deve retornar 200 ao atualizar perfil")
  void updateStudentProfile_returns200() throws Exception {
    StudentDTO dto = new StudentDTO();
    dto.setUserId(1L);
    dto.setMajor("Engenharia de Software");
    dto.setInstitution("UFAPE");

    doNothing().when(studentService).updateStudentProfile(1L, "Engenharia de Software", "UFAPE");

    var response =
        mvc.perform(
                put("/api/students/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(studentDTOTester.write(dto).getJson()))
            .andReturn()
            .getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).contains("sucesso");
  }

  @Test
  @DisplayName("GET /api/students deve retornar 200 com lista de contatos")
  void getAllStudents_returns200WithList() throws Exception {
    StudentContactView contact = mock(StudentContactView.class);
    when(contact.getNome()).thenReturn("Maria");
    when(studentRepository.findAllContacts()).thenReturn(List.of(contact));

    var response = mvc.perform(get("/api/students")).andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  @DisplayName("GET /api/students/{id} deve retornar 200 quando estudante for encontrado")
  void getStudentById_found_returns200() throws Exception {
    StudentContactView contact = mock(StudentContactView.class);
    when(studentRepository.findContactById(1L)).thenReturn(Optional.of(contact));

    var response = mvc.perform(get("/api/students/1")).andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  @DisplayName("GET /api/students/{id} deve retornar 404 quando estudante não for encontrado")
  void getStudentById_notFound_returns404() throws Exception {
    when(studentRepository.findContactById(99L)).thenReturn(Optional.empty());

    var response = mvc.perform(get("/api/students/99")).andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("GET /api/students/engagement deve retornar 200 com lista de engajamento")
  void getAllEngagement_returns200WithList() throws Exception {
    StudentEngagementView engagement = mock(StudentEngagementView.class);
    when(studentRepository.findAllEngagement()).thenReturn(List.of(engagement));

    var response = mvc.perform(get("/api/students/engagement")).andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  @DisplayName("GET /api/students/{id}/engagement deve retornar 200 quando encontrado")
  void getEngagementById_found_returns200() throws Exception {
    StudentEngagementView engagement = mock(StudentEngagementView.class);
    when(studentRepository.findEngagementById(1L)).thenReturn(Optional.of(engagement));

    var response = mvc.perform(get("/api/students/1/engagement")).andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  @DisplayName("GET /api/students/{id}/engagement deve retornar 404 quando não encontrado")
  void getEngagementById_notFound_returns404() throws Exception {
    when(studentRepository.findEngagementById(99L)).thenReturn(Optional.empty());

    var response = mvc.perform(get("/api/students/99/engagement")).andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }
}
