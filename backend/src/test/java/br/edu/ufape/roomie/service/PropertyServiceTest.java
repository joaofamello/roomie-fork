package br.edu.ufape.roomie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.edu.ufape.roomie.dto.AddressDTO;
import br.edu.ufape.roomie.dto.PropertyRequestDTO;
import br.edu.ufape.roomie.dto.PropertyResponseDTO;
import br.edu.ufape.roomie.enums.PropertyStatus;
import br.edu.ufape.roomie.enums.PropertyType;
import br.edu.ufape.roomie.enums.UserGender;
import br.edu.ufape.roomie.model.Address;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.projection.PropertyDetailView;
import br.edu.ufape.roomie.repository.InterestRepository;
import br.edu.ufape.roomie.repository.PropertyPhotoRepository;
import br.edu.ufape.roomie.repository.PropertyRepository;
import br.edu.ufape.roomie.repository.StudentRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

  @Mock private PropertyRepository propertyRepository;

  @Mock private PropertyPhotoRepository propertyPhotoRepository;

  @Mock private FileStorageService fileStorageService;

  @Mock private StudentRepository studentRepository;

  @Mock private InterestRepository interestRepository;

  @InjectMocks private PropertyService propertyService;

  private User mockOwner;
  private PropertyRequestDTO validDto;

  @BeforeEach
  void setUp() {
    mockOwner = new User();
    mockOwner.setId(1L);
    mockOwner.setEmail("proprietario@ufape.edu.br");

    AddressDTO addressDto = new AddressDTO();
    addressDto.setStreet("Avenida Bom Pastor");
    addressDto.setDistrict("Boa Vista");
    addressDto.setNumber("123");
    addressDto.setCity("Garanhuns");
    addressDto.setState("PE");
    addressDto.setCep("55292-270");

    validDto = new PropertyRequestDTO();
    validDto.setTitle("Quarto Universitário");
    validDto.setDescription("Próximo à UFAPE, mobiliado.");
    validDto.setType(PropertyType.HOUSE);
    validDto.setPrice(new BigDecimal("500.00"));
    validDto.setGender(UserGender.MALE);
    validDto.setAcceptAnimals(false);
    validDto.setAvailableVacancies(1);
    validDto.setAddress(addressDto);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User user) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
  }

  @Test
  @DisplayName(
      "Deve lançar erro 401 Unauthorized se o usuário não estiver autenticado (isAuthenticated false)")
  void shouldThrowUnauthorizedWhenUserIsNotAuthenticated() {
    var auth = mock(org.springframework.security.core.Authentication.class);
    when(auth.isAuthenticated()).thenReturn(false);
    SecurityContextHolder.getContext().setAuthentication(auth);

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.deleteProperty(50L);
            });

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    assertEquals("Usuário não autenticado.", exception.getReason());
  }

  @Test
  @DisplayName("Deve criar um imóvel com endereço, fotos e status DRAFT com sucesso")
  void shouldCreatePropertyWithFullDetailsSuccessfully() {
    authenticateUser(mockOwner);

    MockMultipartFile mockPhoto =
        new MockMultipartFile("photos", "quarto.jpg", "image/jpeg", "image_bytes".getBytes());
    List<MultipartFile> photosList = List.of(mockPhoto);

    when(fileStorageService.storeFile(any(MultipartFile.class)))
        .thenReturn("uuid-gerado-quarto.jpg");

    when(propertyRepository.save(any(Property.class)))
        .thenAnswer(
            invocation -> {
              Property p = invocation.getArgument(0);
              p.setId(10L);
              return p;
            });

    Property result = propertyService.createProperty(validDto, photosList);

    assertNotNull(result);
    assertEquals(10L, result.getId());
    assertEquals(PropertyStatus.DRAFT, result.getStatus());
    assertEquals(mockOwner, result.getOwner());
    assertEquals("Quarto Universitário", result.getTitle());

    assertNotNull(result.getAddress());
    assertEquals("Avenida Bom Pastor", result.getAddress().getStreet());
    assertEquals("55292-270", result.getAddress().getCep());

    assertEquals(result, result.getAddress().getProperty());

    assertNotNull(result.getPhotos());
    assertEquals(1, result.getPhotos().size());
    assertEquals("/images/uuid-gerado-quarto.jpg", result.getPhotos().getFirst().getPath());
    assertEquals(result, result.getPhotos().getFirst().getProperty());

    verify(propertyRepository, times(1)).save(any(Property.class));
    verify(fileStorageService, times(1)).storeFile(any(MultipartFile.class));
  }

  @Test
  @DisplayName("Deve criar imóvel com fotos nulas com sucesso")
  void shouldCreatePropertyWithNullPhotosSuccessfully() {
    authenticateUser(mockOwner);

    when(propertyRepository.save(any(Property.class)))
        .thenAnswer(
            invocation -> {
              Property p = invocation.getArgument(0);
              p.setId(11L);
              return p;
            });

    Property result = propertyService.createProperty(validDto, null);

    assertNotNull(result);
    assertTrue(result.getPhotos().isEmpty());
    verify(propertyRepository, times(1)).save(any(Property.class));
    verify(fileStorageService, never()).storeFile(any());
  }

  @Test
  @DisplayName("Deve criar imóvel com lista de fotos vazia com sucesso")
  void shouldCreatePropertyWithEmptyPhotosListSuccessfully() {
    authenticateUser(mockOwner);

    when(propertyRepository.save(any(Property.class)))
        .thenAnswer(
            invocation -> {
              Property p = invocation.getArgument(0);
              p.setId(12L);
              return p;
            });

    Property result = propertyService.createProperty(validDto, Collections.emptyList());

    assertNotNull(result);
    assertTrue(result.getPhotos().isEmpty());
    verify(propertyRepository, times(1)).save(any(Property.class));
    verify(fileStorageService, never()).storeFile(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar criar imóvel sem proprietário logado")
  void shouldThrowExceptionWhenOwnerIsNull() {
    SecurityContextHolder.clearContext();

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.createProperty(validDto, null);
            });

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    assertEquals("Usuário não autenticado.", exception.getReason());

    verify(propertyRepository, never()).save(any());
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar cadastrar um imóvel com dados inválidos")
  void shouldThrowExceptionWhenDataIsInvalid() {
    var invalidProperty = new PropertyRequestDTO();
    invalidProperty.setTitle("");
    invalidProperty.setPrice(new BigDecimal("-500"));

    authenticateUser(mockOwner);

    assertThrows(
        Exception.class,
        () -> {
          propertyService.createProperty(invalidProperty, Collections.emptyList());
        });
  }

  @Test
  @DisplayName("Deve publicar um imóvel com sucesso mudando o status para ACTIVE")
  void shouldPublishPropertySuccessfully() {
    authenticateUser(mockOwner);
    Property property = new Property();
    property.setId(50L);
    property.setOwner(mockOwner);
    property.setStatus(PropertyStatus.DRAFT);

    when(propertyRepository.findById(50L)).thenReturn(Optional.of(property));
    when(propertyRepository.save(any(Property.class))).thenReturn(property);

    Property result = propertyService.publishProperty(50L);

    assertEquals(PropertyStatus.ACTIVE, result.getStatus());
    verify(propertyRepository, times(1)).save(property);
  }

  @Test
  @DisplayName(
      "Deve lançar erro 403 Forbidden se o usuário tentar publicar um imóvel de outro dono")
  void shouldThrowForbiddenWhenPublishingPropertyOfAnotherOwner() {
    User impostor = new User();
    impostor.setId(2L);
    authenticateUser(impostor);

    Property property = new Property();
    property.setId(50L);
    property.setOwner(mockOwner);

    when(propertyRepository.findById(50L)).thenReturn(Optional.of(property));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.publishProperty(50L);
            });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    verify(propertyRepository, never()).save(any(Property.class));
  }

  @Test
  @DisplayName("Deve lançar erro 404 NotFound ao tentar publicar imóvel inexistente")
  void shouldThrowNotFoundWhenPublishingNonExistentProperty() {
    authenticateUser(mockOwner);
    when(propertyRepository.findById(99L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.publishProperty(99L);
            });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Imóvel não encontrado.", exception.getReason());
  }

  @Test
  @DisplayName("Deve deletar o imóvel com sucesso")
  void shouldDeletePropertySuccessfully() {
    authenticateUser(mockOwner);
    Property property = new Property();
    property.setId(50L);
    property.setOwner(mockOwner);

    when(propertyRepository.findById(50L)).thenReturn(Optional.of(property));

    propertyService.deleteProperty(50L);

    verify(propertyRepository, times(1)).delete(property);
  }

  @Test
  @DisplayName("Deve lançar erro 403 Forbidden ao tentar deletar imóvel de outro dono")
  void shouldThrowForbiddenWhenDeletingPropertyOfAnotherOwner() {
    User impostor = new User();
    impostor.setId(2L);
    authenticateUser(impostor);

    Property property = new Property();
    property.setId(50L);
    property.setOwner(mockOwner);

    when(propertyRepository.findById(50L)).thenReturn(Optional.of(property));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.deleteProperty(50L);
            });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    verify(propertyRepository, never()).delete(any(Property.class));
  }

  @Test
  @DisplayName("Deve lançar erro 404 NotFound ao tentar deletar imóvel inexistente")
  void shouldThrowNotFoundWhenDeletingNonExistentProperty() {
    authenticateUser(mockOwner);
    when(propertyRepository.findById(99L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.deleteProperty(99L);
            });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Imóvel não encontrado.", exception.getReason());
  }

  @Test
  @DisplayName(
      "Deve lançar erro 404 NotFound se imóvel não for encontrado ao tentar colocar em rascunho")
  void shouldThrowNotFoundWhenSettingDraftNonExistentProperty() {
    authenticateUser(mockOwner);
    when(propertyRepository.findById(99L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.setPropertyToDraft(99L);
            });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Imóvel não encontrado.", exception.getReason());
  }

  @Test
  @DisplayName("Deve lançar erro 403 Forbidden ao tentar mudar para rascunho imóvel de outro dono")
  void shouldThrowForbiddenWhenSettingDraftPropertyOfAnotherOwner() {
    User impostor = new User();
    impostor.setId(2L);
    authenticateUser(impostor);

    Property property = new Property();
    property.setId(50L);
    property.setOwner(mockOwner);

    when(propertyRepository.findById(50L)).thenReturn(Optional.of(property));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.setPropertyToDraft(50L);
            });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    verify(propertyRepository, never()).save(any(Property.class));
  }

  @Test
  @DisplayName("Deve voltar o imóvel para rascunho (DRAFT) com sucesso")
  void shouldSetPropertyToDraftSuccessfully() {
    authenticateUser(mockOwner);
    Property property = new Property();
    property.setId(50L);
    property.setOwner(mockOwner);
    property.setStatus(PropertyStatus.ACTIVE);

    when(propertyRepository.findById(50L)).thenReturn(Optional.of(property));
    when(propertyRepository.save(any(Property.class))).thenReturn(property);

    Property result = propertyService.setPropertyToDraft(50L);

    assertEquals(PropertyStatus.DRAFT, result.getStatus());
    verify(propertyRepository, times(1)).save(property);
  }

  @Test
  @DisplayName("Deve lançar erro 404 NotFound ao tentar atualizar um imóvel que não existe")
  void shouldThrowNotFoundWhenUpdatingNonExistentProperty() {
    authenticateUser(mockOwner);
    when(propertyRepository.findById(99L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.updateProperty(99L, validDto, null);
            });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Imóvel não encontrado.", exception.getReason());
  }

  @Test
  @DisplayName(
      "Deve lançar erro 403 Forbidden se o usuário tentar atualizar um imóvel de outro dono")
  void shouldThrowForbiddenWhenUpdatingPropertyOfAnotherOwner() {
    User impostor = new User();
    impostor.setId(2L);
    authenticateUser(impostor);

    Property property = new Property();
    property.setId(50L);
    property.setOwner(mockOwner);

    when(propertyRepository.findById(50L)).thenReturn(Optional.of(property));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.updateProperty(50L, validDto, null);
            });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    verify(propertyRepository, never()).save(any(Property.class));
  }

  @Test
  @DisplayName("Deve atualizar o imóvel com sucesso, substituindo as fotos e voltando para DRAFT")
  void shouldUpdatePropertySuccessfully() {
    authenticateUser(mockOwner);
    Property property = new Property();
    property.setId(50L);
    property.setOwner(mockOwner);
    property.setAddress(new Address());
    property.setPhotos(new ArrayList<>());
    property.setStatus(PropertyStatus.ACTIVE);

    MockMultipartFile mockPhoto =
        new MockMultipartFile("photos", "nova_foto.jpg", "image/jpeg", "bytes".getBytes());

    when(propertyRepository.findById(50L)).thenReturn(Optional.of(property));
    when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn("uuid-nova-foto.jpg");
    when(propertyRepository.save(any(Property.class))).thenReturn(property);

    Property result = propertyService.updateProperty(50L, validDto, List.of(mockPhoto));

    assertEquals("Quarto Universitário", result.getTitle());
    assertEquals(PropertyStatus.DRAFT, result.getStatus());
    assertEquals(1, result.getPhotos().size());
    assertEquals("/images/uuid-nova-foto.jpg", result.getPhotos().getFirst().getPath());
  }

  @Test
  @DisplayName(
      "Deve atualizar o imóvel mantendo a lista original caso mande novas fotos como lista vazia")
  void shouldUpdatePropertyWithEmptyPhotosSuccessfully() {
    authenticateUser(mockOwner);
    Property property = new Property();
    property.setId(50L);
    property.setOwner(mockOwner);
    property.setAddress(new Address());
    property.setPhotos(new ArrayList<>());
    property.setStatus(PropertyStatus.ACTIVE);

    when(propertyRepository.findById(50L)).thenReturn(Optional.of(property));
    when(propertyRepository.save(any(Property.class))).thenReturn(property);

    Property result = propertyService.updateProperty(50L, validDto, Collections.emptyList());

    assertEquals("Quarto Universitário", result.getTitle());
    assertEquals(PropertyStatus.DRAFT, result.getStatus());
    assertTrue(result.getPhotos().isEmpty());
    verify(fileStorageService, never()).storeFile(any());
  }

  @Test
  @DisplayName("Deve atualizar o imóvel mantendo a lista vazia caso não envie novas fotos")
  void shouldUpdatePropertyWithoutPhotosSuccessfully() {
    authenticateUser(mockOwner);
    Property property = new Property();
    property.setId(50L);
    property.setOwner(mockOwner);
    property.setAddress(new Address());
    property.setPhotos(new ArrayList<>());

    when(propertyRepository.findById(50L)).thenReturn(Optional.of(property));
    when(propertyRepository.save(any(Property.class))).thenReturn(property);

    Property result = propertyService.updateProperty(50L, validDto, null);

    assertEquals("Quarto Universitário", result.getTitle());
    assertTrue(result.getPhotos().isEmpty());
    verify(fileStorageService, never()).storeFile(any());
  }

  @Test
  @DisplayName("Deve retornar os detalhes do anúncio corretamente quando ele estiver ACTIVE")
  void shouldReturnPropertyDetailsSuccessfully() {
    authenticateUser(mockOwner);

    PropertyDetailView mockView = mock(PropertyDetailView.class);
    when(mockView.getStatus()).thenReturn("ACTIVE");

    when(propertyRepository.findDetailById(50L)).thenReturn(Optional.of(mockView));
    when(propertyPhotoRepository.findPhotosByPropertyId(50L))
        .thenReturn(List.of("/images/foto1.jpg"));

    PropertyResponseDTO result = propertyService.getPropertyDetails(50L);

    assertNotNull(result);
    assertEquals(mockView, result.getDetails());
    assertEquals(1, result.getPhotos().size());
    assertEquals("/images/foto1.jpg", result.getPhotos().getFirst());
  }

  @Test
  @DisplayName(
      "Deve lançar erro 403 Forbidden ao tentar ver detalhes de um imóvel que não está ACTIVE")
  void shouldThrowForbiddenWhenPropertyIsNotActive() {
    authenticateUser(mockOwner);

    PropertyDetailView mockView = mock(PropertyDetailView.class);
    when(mockView.getStatus()).thenReturn("DRAFT"); // Imóvel não está ativo

    when(propertyRepository.findDetailById(50L)).thenReturn(Optional.of(mockView));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.getPropertyDetails(50L);
            });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("Anúncio não está disponivel", exception.getReason());
  }

  @Test
  @DisplayName("Deve lançar erro 404 NotFound se os detalhes do imóvel não existirem")
  void shouldThrowNotFoundWhenPropertyDetailsDoNotExist() {
    authenticateUser(mockOwner);
    when(propertyRepository.findDetailById(99L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.getPropertyDetails(99L);
            });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  @DisplayName(
      "Deve lançar erro 401 Unauthorized se o principal do contexto não for uma instância de User")
  void shouldThrowUnauthorizedWhenPrincipalIsNotUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("anonymousUser", null, null));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.deleteProperty(50L);
            });

    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    assertEquals("Usuário não autenticado.", exception.getReason());
  }

  @Test
  @DisplayName("Deve confirmar um estudante em uma moradia com sucesso")
  void shouldConfirmStudentSuccessfully() {
    authenticateUser(mockOwner);

    Property property = new Property();
    property.setId(10L);
    property.setAvailableVacancies(2);
    property.setOwner(mockOwner);
    property.setStatus(PropertyStatus.ACTIVE);
    property.setStatus(PropertyStatus.ACTIVE);

    br.edu.ufape.roomie.model.Student student = new br.edu.ufape.roomie.model.Student();
    student.setId(5L);

    when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
    when(studentRepository.findById(5L)).thenReturn(Optional.of(student));
    when(interestRepository.existsByStudentAndProperty(student, property)).thenReturn(true);
    when(propertyRepository.save(any(Property.class))).thenAnswer(i -> i.getArgument(0));

    Property result = propertyService.confirmStudent(10L, 5L);

    assertEquals(PropertyStatus.ACTIVE, result.getStatus());
    assertEquals(1, result.getAvailableVacancies());
  }

  @Test
  @DisplayName("Deve lançar erro 404 NotFound ao confirmar estudante em moradia que não existe")
  void shouldThrowNotFoundWhenConfirmingStudentInNonExistentProperty() {
    authenticateUser(mockOwner);
    when(propertyRepository.findById(99L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.confirmStudent(99L, 5L);
            });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Imóvel não encontrado.", exception.getReason());
  }

  @Test
  @DisplayName("Deve lançar erro 404 NotFound ao tentar confirmar com estudante que não existe")
  void shouldThrowNotFoundWhenConfirmingNonExistentStudent() {
    authenticateUser(mockOwner);
    Property property = new Property();
    property.setId(10L);
    property.setOwner(mockOwner);

    when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
    when(studentRepository.findById(99L)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.confirmStudent(10L, 99L);
            });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Estudante não encontrado.", exception.getReason());
  }

  @Test
  @DisplayName("Deve confirmar um estudante e mudar status para RENTED quando vagas zerarem")
  void shouldChangeStatusToRentedWhenVacanciesReachZero() {
    authenticateUser(mockOwner);

    Property property = new Property();
    property.setId(10L);
    property.setAvailableVacancies(1); // Somente 1 vaga para testar o zeramento e o status RENTED
    property.setOwner(mockOwner);
    property.setStatus(PropertyStatus.ACTIVE);

    br.edu.ufape.roomie.model.Student student = new br.edu.ufape.roomie.model.Student();
    student.setId(5L);

    when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
    when(studentRepository.findById(5L)).thenReturn(Optional.of(student));
    when(interestRepository.existsByStudentAndProperty(student, property)).thenReturn(true);
    when(propertyRepository.save(any(Property.class))).thenAnswer(i -> i.getArgument(0));

    Property result = propertyService.confirmStudent(10L, 5L);

    assertEquals(PropertyStatus.RENTED, result.getStatus());
    assertEquals(0, result.getAvailableVacancies());
  }

  @Test
  @DisplayName("Deve lançar erro 400 se o estudante não for um candidato")
  void shouldThrowBadRequestWhenStudentIsNotInterested() {
    authenticateUser(mockOwner);

    Property property = new Property();
    property.setId(10L);
    property.setOwner(mockOwner);

    br.edu.ufape.roomie.model.Student student = new br.edu.ufape.roomie.model.Student();
    student.setId(5L);

    when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
    when(studentRepository.findById(5L)).thenReturn(Optional.of(student));
    when(interestRepository.existsByStudentAndProperty(student, property)).thenReturn(false);

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.confirmStudent(10L, 5L);
            });

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(
        "O estudante selecionado não está na lista de candidatos desta moradia.",
        exception.getReason());
  }

  @Test
  @DisplayName("Deve lançar erro 403 se outro dono tentar confirmar a vaga")
  void shouldThrowForbiddenWhenNotOwnerTriesToConfirm() {
    User impostor = new User();
    impostor.setId(99L);
    authenticateUser(impostor);

    Property property = new Property();
    property.setId(10L);
    property.setOwner(mockOwner);

    when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> {
              propertyService.confirmStudent(10L, 5L);
            });

    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
  }
}
