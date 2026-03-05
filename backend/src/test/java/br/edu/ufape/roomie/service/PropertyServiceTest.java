package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.AddressDTO;
import br.edu.ufape.roomie.dto.PropertyRequestDTO;
import br.edu.ufape.roomie.enums.PropertyStatus;
import br.edu.ufape.roomie.enums.PropertyType;
import br.edu.ufape.roomie.enums.UserGender;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.PropertyRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private PropertyService propertyService;

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

    @Test
    @DisplayName("Deve criar um imóvel com endereço, fotos e status DRAFT com sucesso")
    void shouldCreatePropertyWithFullDetailsSuccessfully() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockOwner, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockMultipartFile mockPhoto = new MockMultipartFile(
                "photos", "quarto.jpg", "image/jpeg", "image_bytes".getBytes());
        List<MultipartFile> photosList = List.of(mockPhoto);

        when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn("uuid-gerado-quarto.jpg");

        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> {
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
        assertEquals("/images/uuid-gerado-quarto.jpg", result.getPhotos().get(0).getPath());
        assertEquals(result, result.getPhotos().get(0).getProperty());

        verify(propertyRepository, times(1)).save(any(Property.class));
        verify(fileStorageService, times(1)).storeFile(any(MultipartFile.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar imóvel sem proprietário logado")
    void shouldThrowExceptionWhenOwnerIsNull() {
        SecurityContextHolder.clearContext();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            propertyService.createProperty(validDto, null);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Usuário não autenticado.", exception.getReason());

        verify(propertyRepository, never()).save(any());
        verify(fileStorageService, never()).storeFile(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar um imóvel com dados inválidos")
    void shouldThrowExceptionWhenDataIsInvalid() {
        var invalidProperty = new PropertyRequestDTO();
        invalidProperty.setTitle("");
        invalidProperty.setPrice(new BigDecimal("-500"));

        var authentication = mock(Authentication.class);
        var securityContext = mock(SecurityContext.class);
        var userMock = new User();
        userMock.setId(1L);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userMock);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(Exception.class, () -> {
            propertyService.createProperty(invalidProperty, Collections.emptyList());
        });
    }

    @Test
    @DisplayName("Deve lançar erro 403 Forbidden se o usuário tentar publicar um imóvel de outro dono")
    void shouldThrowForbiddenWhenPublishingPropertyOfAnotherOwner() {
        var propertyId = 50L;

        var realOwner = new User();
        realOwner.setId(1L);

        var property = new Property();
        property.setId(propertyId);
        property.setOwner(realOwner);

        var impostor = new User();
        impostor.setId(2L);

        var authentication = mock(Authentication.class);
        var securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(impostor);
        SecurityContextHolder.setContext(securityContext);

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        assertThrows(ResponseStatusException.class, () -> {
            propertyService.publishProperty(propertyId);
        });

        verify(propertyRepository, never()).save(any(Property.class));

    }
}