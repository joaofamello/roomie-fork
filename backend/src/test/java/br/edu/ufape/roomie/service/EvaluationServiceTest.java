package br.edu.ufape.roomie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import br.edu.ufape.roomie.dto.EvaluationRequestDTO;
import br.edu.ufape.roomie.dto.EvaluationResponseDTO;
import br.edu.ufape.roomie.dto.EvaluationSummaryDTO;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.PropertyEvaluation;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.ContractRepository;
import br.edu.ufape.roomie.repository.PropertyEvaluationRepository;
import br.edu.ufape.roomie.repository.PropertyRepository;
import br.edu.ufape.roomie.repository.StudentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

  @Mock private PropertyEvaluationRepository evaluationRepository;

  @Mock private PropertyRepository propertyRepository;

  @Mock private StudentRepository studentRepository;

  @Mock private ContractRepository contractRepository;

  @Mock private SecurityContext securityContext;

  @Mock private Authentication authentication;

  @InjectMocks private EvaluationService evaluationService;

  private User authUser;
  private Student student;
  private Property property;
  private User propertyOwner;

  @BeforeEach
  void setUp() {
    authUser = new User();
    authUser.setId(1L);
    authUser.setEmail("test@gmail.com");

    student = new Student();
    student.setId(1L);
    student.setEmail("test@gmail.com");
    student.setName("Teste Estudante");

    propertyOwner = new User();
    propertyOwner.setId(2L);
    propertyOwner.setEmail("proprietario@gmail.com");

    property = new Property();
    property.setId(10L);
    property.setOwner(propertyOwner);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void mockSecurityContext(User user) {
    if (user != null) {
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(true);
      when(authentication.getPrincipal()).thenReturn(user);
    } else {
      when(securityContext.getAuthentication()).thenReturn(null);
    }
    SecurityContextHolder.setContext(securityContext);
  }

  private void mockUnauthenticatedContext() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(false);
    SecurityContextHolder.setContext(securityContext);
  }

  private void mockWrongPrincipalContext() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn(new Object());
    SecurityContextHolder.setContext(securityContext);
  }

  // Usuário autenticado testes

  // Falha quando não tem autenticação
  @Test
  void createEvaluation_ShouldThrowException_WhenAuthenticationIsNull() {
    mockSecurityContext(null);

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              evaluationService.createEvaluation(10L, new EvaluationRequestDTO());
            });

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
  }

  // Falha quando o usuário não está autenticado
  @Test
  void createEvaluation_ShouldThrowException_WhenNotAuthenticated() {
    mockUnauthenticatedContext();

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              evaluationService.createEvaluation(10L, new EvaluationRequestDTO());
            });

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
  }

  // Falha quando o principal não é do tipo User
  @Test
  void createEvaluation_ShouldThrowException_WhenPrincipalIsNotUser() {
    mockWrongPrincipalContext();

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              evaluationService.createEvaluation(10L, new EvaluationRequestDTO());
            });

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
  }

  // Criar avaliação testes

  // Falha se o usuário que tentar avaliar não for estudante
  @Test
  void createEvaluation_ShouldThrowException_WhenStudentNotFound() {
    mockSecurityContext(authUser);
    when(studentRepository.findByEmail(authUser.getEmail())).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              evaluationService.createEvaluation(10L, new EvaluationRequestDTO());
            });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("Apenas estudantes podem avaliar imóveis.", exception.getReason());
  }

  // Falha se o imóvel não existir ou não for encontrado
  @Test
  void createEvaluation_ShouldThrowException_WhenPropertyNotFound() {
    mockSecurityContext(authUser);
    when(studentRepository.findByEmail(authUser.getEmail())).thenReturn(Optional.of(student));
    when(propertyRepository.findById(10L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              evaluationService.createEvaluation(10L, new EvaluationRequestDTO());
            });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Imóvel não encontrado.", exception.getReason());
  }

  // Falha se o estudante for o proprietário do imóvel
  @Test
  void createEvaluation_ShouldThrowException_WhenStudentIsOwner() {
    mockSecurityContext(authUser);
    when(studentRepository.findByEmail(authUser.getEmail())).thenReturn(Optional.of(student));
    property.setOwner(student); // Faz o estudante ser o proprietário do imóvel
    when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              evaluationService.createEvaluation(10L, new EvaluationRequestDTO());
            });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("Você não pode avaliar seu próprio imóvel.", exception.getReason());
  }

  // Falha se o estudante não tiver um contrato válido para o imóvel
  @Test
  void createEvaluation_ShouldThrowException_WhenNoValidContract() {
    mockSecurityContext(authUser);
    when(studentRepository.findByEmail(authUser.getEmail())).thenReturn(Optional.of(student));
    when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
    when(contractRepository.existsByPropertyIdAndUserIdAndStatusIn(
            eq(10L), eq(student.getId()), anyList()))
        .thenReturn(false);

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              evaluationService.createEvaluation(10L, new EvaluationRequestDTO());
            });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals(
        "Apenas estudantes com contrato neste imóvel podem avaliá-lo.", exception.getReason());
  }

  // Falha se o estudante já tiver avaliado o imóvel
  @Test
  void createEvaluation_ShouldThrowException_WhenAlreadyEvaluated() {
    mockSecurityContext(authUser);
    when(studentRepository.findByEmail(authUser.getEmail())).thenReturn(Optional.of(student));
    when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
    when(contractRepository.existsByPropertyIdAndUserIdAndStatusIn(
            eq(10L), eq(student.getId()), anyList()))
        .thenReturn(true);
    when(evaluationRepository.findByPropertyIdAndStudentId(10L, student.getId()))
        .thenReturn(Optional.of(new PropertyEvaluation()));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              evaluationService.createEvaluation(10L, new EvaluationRequestDTO());
            });

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals("Você já avaliou este imóvel.", exception.getReason());
  }

  // Caminho de Sucesso para criação de avaliação
  @Test
  void createEvaluation_ShouldSaveAndReturnResponse_WhenValidRequest() {
    mockSecurityContext(authUser);
    when(studentRepository.findByEmail(authUser.getEmail())).thenReturn(Optional.of(student));
    when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
    when(contractRepository.existsByPropertyIdAndUserIdAndStatusIn(
            eq(10L), eq(student.getId()), anyList()))
        .thenReturn(true);
    when(evaluationRepository.findByPropertyIdAndStudentId(10L, student.getId()))
        .thenReturn(Optional.empty());

    EvaluationRequestDTO request = new EvaluationRequestDTO();

    // Setando uma avaliação válida (ex: 5 estrelas)

    PropertyEvaluation savedEvaluation = new PropertyEvaluation();
    savedEvaluation.setIdEvaluation(100L);
    savedEvaluation.setProperty(property);
    savedEvaluation.setStudent(student);
    savedEvaluation.setRating(5);
    savedEvaluation.setTimestamp(LocalDateTime.now());

    when(evaluationRepository.save(any(PropertyEvaluation.class))).thenReturn(savedEvaluation);

    EvaluationResponseDTO response = evaluationService.createEvaluation(10L, request);

    assertNotNull(response);
    assertEquals(100L, response.getId());
    assertEquals(10L, response.getPropertyId());
    assertEquals(1L, response.getStudentId());
    verify(evaluationRepository, times(1)).save(any(PropertyEvaluation.class));
  }

  // Pegar avaliações por imóvel testes

  // Falha se o imóvel não existir ou não for encontrado
  @Test
  void getEvaluationsByProperty_ShouldThrowException_WhenPropertyNotFound() {
    when(propertyRepository.findById(10L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              evaluationService.getEvaluationsByProperty(10L);
            });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Imóvel não encontrado.", exception.getReason());
  }

  // Caminho de Sucesso para obter avaliações de um imóvel existente
  @Test
  void getEvaluationsByProperty_ShouldReturnSummary_WhenPropertyExists() {
    when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));

    PropertyEvaluation eval1 = new PropertyEvaluation();
    eval1.setIdEvaluation(1L);
    eval1.setProperty(property);
    eval1.setStudent(student);
    eval1.setRating(4);
    eval1.setTimestamp(LocalDateTime.now());

    PropertyEvaluation eval2 = new PropertyEvaluation();
    eval2.setIdEvaluation(2L);
    eval2.setProperty(property);
    eval2.setStudent(student);
    eval2.setRating(5);
    eval2.setTimestamp(LocalDateTime.now());

    when(evaluationRepository.findByPropertyId(10L)).thenReturn(List.of(eval1, eval2));
    when(evaluationRepository.findAverageRatingByPropertyId(10L)).thenReturn(4.5);
    when(evaluationRepository.countByPropertyId(10L)).thenReturn(2L);

    EvaluationSummaryDTO summary = evaluationService.getEvaluationsByProperty(10L);

    assertNotNull(summary);
    assertEquals(4.5, summary.getAverageRating());
    assertEquals(2L, summary.getTotalEvaluations());
    assertEquals(2, summary.getEvaluations().size());
  }

  // Caminho de Sucesso para obter avaliações de um imóvel sem avaliações locais (mas com média e
  // contagem)
  @Test
  void getEvaluationsByProperty_ShouldHandleNullAverage_WhenNoEvaluationsLocally() {
    when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
    when(evaluationRepository.findByPropertyId(10L)).thenReturn(List.of());
    when(evaluationRepository.findAverageRatingByPropertyId(10L)).thenReturn(0.0);
    when(evaluationRepository.countByPropertyId(10L)).thenReturn(0L);

    EvaluationSummaryDTO summary = evaluationService.getEvaluationsByProperty(10L);

    assertNotNull(summary);
    assertEquals(0.0, summary.getAverageRating());
    assertEquals(0L, summary.getTotalEvaluations());
    assertEquals(0, summary.getEvaluations().size());
  }

  // Usuário já avaliou o imóvel testes

  // Retorna true se o usuário tiver avaliado o imóvel
  @Test
  void hasUserEvaluated_ShouldReturnTrue_WhenEvaluationExists() {
    mockSecurityContext(authUser);
    when(evaluationRepository.findByPropertyIdAndStudentId(10L, authUser.getId()))
        .thenReturn(Optional.of(new PropertyEvaluation()));

    boolean result = evaluationService.hasUserEvaluated(10L);

    assertTrue(result);
  }

  // Retorna false se o usuário não tiver avaliado o imóvel
  @Test
  void hasUserEvaluated_ShouldReturnFalse_WhenEvaluationDoesNotExist() {
    mockSecurityContext(authUser);
    when(evaluationRepository.findByPropertyIdAndStudentId(10L, authUser.getId()))
        .thenReturn(Optional.empty());

    boolean result = evaluationService.hasUserEvaluated(10L);

    assertFalse(result);
  }
}
