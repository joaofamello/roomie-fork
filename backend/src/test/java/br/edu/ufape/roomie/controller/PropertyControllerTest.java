package br.edu.ufape.roomie.controller;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.ufape.roomie.dto.AddressDTO;
import br.edu.ufape.roomie.dto.PropertyRequestDTO;
import br.edu.ufape.roomie.dto.PropertyResponseDTO;
import br.edu.ufape.roomie.enums.PropertyStatus;
import br.edu.ufape.roomie.enums.PropertyType;
import br.edu.ufape.roomie.enums.UserGender;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.projection.PropertyDetailView;
import br.edu.ufape.roomie.repository.PropertyRepository;
import br.edu.ufape.roomie.service.PropertyService;
import br.edu.ufape.roomie.service.TokenService;

@WebMvcTest(controllers = PropertyController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
class PropertyControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<PropertyRequestDTO> propertyRequestDTOJacksonTester;

    @MockitoBean
    private PropertyService propertyService;

    @MockitoBean
    private PropertyRepository propertyRepository;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deveria devolver código HTTP 400 quando requisição não possui a parte 'data' obrigatória")
    @WithMockUser
    void testaCriarPropriedadeComDadosInvalidos() throws Exception {
        var response = mvc.perform(multipart("/api/properties")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Deveria devolver código HTTP 201 quando informações estão válidas")
    @WithMockUser
    void testaCriarPropridadeComInformacoesValidas() throws Exception {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setStreet("Avenida Bom Pastor");
        addressDTO.setDistrict("Boa Vista");
        addressDTO.setNumber("123");
        addressDTO.setCity("Garanhuns");
        addressDTO.setState("PE");
        addressDTO.setCep("55290-000");

        PropertyRequestDTO validDto = new PropertyRequestDTO();
        validDto.setTitle("Quarto bem localizado próximo à UFAPE");
        validDto.setDescription("Excelente quarto para estudantes.");
        validDto.setType(PropertyType.HOUSE);
        validDto.setPrice(new BigDecimal("450.00"));
        validDto.setGender(UserGender.OTHER);
        validDto.setAcceptAnimals(true);
        validDto.setHasGarage(false);
        validDto.setAvailableVacancies(2);
        validDto.setAddress(addressDTO);

        String dtoJson = propertyRequestDTOJacksonTester.write(validDto).getJson();
        MockMultipartFile dataPart = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                dtoJson.getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile photoPart = new MockMultipartFile(
                "photos",
                "quarto.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo-fake".getBytes()
        );

        Property mockProperty = new Property();
        mockProperty.setId(1L);
        when(propertyService.createProperty(any(PropertyRequestDTO.class), anyList())).thenReturn(mockProperty);

        var response = mvc.perform(multipart("/api/properties")
                        .file(dataPart)
                        .file(photoPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request se o título for vazio (Validação @Valid)")
    @WithMockUser
    void deveFalharValidacao() throws Exception {
        var dto = new PropertyRequestDTO();
        dto.setTitle("");
        dto.setPrice(new BigDecimal("1000"));

        String dtoJson = propertyRequestDTOJacksonTester.write(dto).getJson();
        MockMultipartFile dataPart = new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE, dtoJson.getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/api/properties").file(dataPart).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve listar propriedades usando filtros e retornar 200 OK")
    void testaGetAllProperties() throws Exception {
        when(propertyRepository.findWithFilters(anyString(), anyString(), anyDouble(), anyDouble(), anyString()))
                .thenReturn(List.of(new Property()));

        var response = mvc.perform(get("/api/properties")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deve chamar o repository com parâmetros fornecidos e retornar 200 OK")
    void testaGetAllPropertiesComParametros() throws Exception {
        when(propertyRepository.findWithFilters("Rio", "Centro", 100.0, 500.0, "HOUSE"))
                .thenReturn(List.of(new Property()));

        var response = mvc.perform(get("/api/properties")
                .param("location", "Rio")
                .param("district", "Centro")
                .param("minPrice", "100")
                .param("maxPrice", "500")
                .param("propertyType", "house")
        ).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(propertyRepository, times(1)).findWithFilters("Rio", "Centro", 100.0, 500.0, "HOUSE");
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao buscar propriedade por ID existente")
    void testaGetByIdEncontrado() throws Exception {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(new Property()));

        var response = mvc.perform(get("/api/properties/1")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao buscar propriedade por ID inexistente")
    void testaGetByIdNaoEncontrado() throws Exception {
        when(propertyRepository.findById(999L)).thenReturn(Optional.empty());

        var response = mvc.perform(get("/api/properties/999")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Deve listar todos os detalhes (views) e retornar 200 OK")
    void testaGetAllDetails() throws Exception {
        when(propertyRepository.findAllDetails()).thenReturn(List.of());

        var response = mvc.perform(get("/api/properties/details")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deve retornar detalhes de um anuncio formatados em DTO e 200 OK")
    void testaGetAnnouncementsDetails() throws Exception {
        PropertyDetailView mockDetailView = mock(PropertyDetailView.class);

        PropertyResponseDTO mockResponseDto = new PropertyResponseDTO(mockDetailView, List.of("foto1.png", "foto2.png"));

        when(propertyService.getPropertyDetails(1L)).thenReturn(mockResponseDto);

        var response = mvc.perform(get("/api/properties/announcements/1")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    private Authentication setupAuth(String email) {
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return auth;
    }

    @Test
    @DisplayName("Deve listar as propriedades do usuário logado (meus) e retornar 200 OK")
    void testaGetMyProperties() throws Exception {
        Authentication auth = setupAuth("dono@ufape.edu.br");
        when(propertyRepository.findMyDetails("dono@ufape.edu.br")).thenReturn(List.of());

        var response = mvc.perform(get("/api/properties/meus").principal(auth)).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deve publicar um imóvel com sucesso e retornar 200 OK")
    void testaPublishPropertyComSucesso() throws Exception {
        Authentication auth = setupAuth("dono@ufape.edu.br");

        User dono = new User();
        dono.setId(1L);

        Property property = new Property();
        property.setId(1L);
        property.setOwner(dono);
        property.setStatus(PropertyStatus.DRAFT);

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));

        var response = mvc.perform(patch("/api/properties/1/publish").principal(auth)).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(propertyRepository, times(1)).save(any(Property.class));
    }

    @Test
    @DisplayName("Deve retornar 403 Forbidden ao tentar publicar imóvel de outro usuário")
    void testaPublishPropertyAcessoNegado() throws Exception {
        Authentication auth = setupAuth("invasor@ufape.edu.br"); // ID logado = 1

        User donoReal = new User();
        donoReal.setId(2L);

        Property property = new Property();
        property.setId(1L);
        property.setOwner(donoReal);

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));

        var response = mvc.perform(patch("/api/properties/1/publish").principal(auth)).andReturn().getResponse();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentAsString()).isEqualTo("Você não tem permissão para publicar este imóvel.");
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar publicar um imóvel já ativo")
    void testaPublishPropertyJaPublicado() throws Exception {
        Authentication auth = setupAuth("dono@ufape.edu.br");

        User dono = new User();
        dono.setId(1L);

        Property property = new Property();
        property.setId(1L);
        property.setOwner(dono);
        property.setStatus(PropertyStatus.ACTIVE); // Já está ativo

        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));

        var response = mvc.perform(patch("/api/properties/1/publish").principal(auth)).andReturn().getResponse();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo("Imóvel já está publicado.");
    }

    @Test
    @DisplayName("Deve deletar imóvel e retornar 204 No Content")
    void testaDeletePropertyComSucesso() throws Exception {
        doNothing().when(propertyService).deleteProperty(1L);

        var response = mvc.perform(delete("/api/properties/1")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("Deve retornar 403 Forbidden se o service lançar exceção no delete")
    void testaDeletePropertyComErro() throws Exception {
        doThrow(new RuntimeException("Não autorizado")).when(propertyService).deleteProperty(1L);

        var response = mvc.perform(delete("/api/properties/1")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found quando deleteProperty lançar ResponseStatusException")
    void testaDeletePropertyNotFound() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "não encontrado"))
                .when(propertyService).deleteProperty(1L);

        var response = mvc.perform(delete("/api/properties/1")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString()).contains("não encontrado");
    }

    @Test
    @DisplayName("Deve alterar para rascunho com sucesso e retornar 200 OK")
    void testaSetToDraftComSucesso() throws Exception {
        Property property = new Property();
        property.setId(1L);
        when(propertyService.setPropertyToDraft(1L)).thenReturn(property);

        var response = mvc.perform(patch("/api/properties/1/draft")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deve retornar 403 Forbidden se o service lançar exceção no draft")
    void testaSetToDraftComErro() throws Exception {
        doThrow(new RuntimeException("Não autorizado")).when(propertyService).setPropertyToDraft(1L);

        var response = mvc.perform(patch("/api/properties/1/draft")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found se o service lançar ResponseStatusException no draft")
    void testaSetToDraftNotFound() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "nao encontrado"))
                .when(propertyService).setPropertyToDraft(1L);

        var response = mvc.perform(patch("/api/properties/1/draft")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Deve atualizar imóvel com multipart via método PUT e retornar 200 OK")
    void testaUpdateProperty() throws Exception {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setStreet("Avenida Bom Pastor Atualizada");
        addressDTO.setDistrict("Boa Vista");
        addressDTO.setNumber("123A");
        addressDTO.setCity("Garanhuns");
        addressDTO.setState("PE");
        addressDTO.setCep("55290-000");

        PropertyRequestDTO dto = new PropertyRequestDTO();
        dto.setTitle("Novo Titulo Atualizado");
        dto.setDescription("Descrição atualizada do imóvel.");
        dto.setType(PropertyType.HOUSE);
        dto.setPrice(new BigDecimal("500.00"));
        dto.setGender(UserGender.OTHER);
        dto.setAcceptAnimals(false);
        dto.setHasGarage(true);
        dto.setAvailableVacancies(1);
        dto.setAddress(addressDTO);

        String dtoJson = propertyRequestDTOJacksonTester.write(dto).getJson();
        MockMultipartFile dataPart = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                dtoJson.getBytes(StandardCharsets.UTF_8)
        );

        Property updatedProperty = new Property();
        updatedProperty.setId(1L);
        when(propertyService.updateProperty(eq(1L), any(), any())).thenReturn(updatedProperty);

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/api/properties/1");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        var response = mvc.perform(builder.file(dataPart).contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao confirmar um estudante na moradia")
    @WithMockUser
    void testaConfirmStudentComSucesso() throws Exception {
        Property property = new Property();
        property.setId(1L);
        property.setStatus(PropertyStatus.RENTED);

        br.edu.ufape.roomie.model.Student student = new br.edu.ufape.roomie.model.Student();
        student.setId(5L);

        when(propertyService.confirmStudent(1L, 5L)).thenReturn(property);

        var response = mvc.perform(patch("/api/properties/1/confirm")
                        .param("studentId", "5"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).contains("\"status\":\"RENTED\"");
        assertThat(response.getContentAsString()).contains("\"confirmedStudentId\":5");
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found quando buscar detalhes por id inexistente")
    void testaGetDetailsByIdNaoEncontrado() throws Exception {
        when(propertyRepository.findDetailById(999L)).thenReturn(Optional.empty());

        var response = mvc.perform(get("/api/properties/999/details")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Deve retornar 200 OK com detalhes por id existente")
    void testaGetDetailsByIdEncontrado() throws Exception {
        PropertyDetailView mockDetailView = mock(PropertyDetailView.class);
        when(propertyRepository.findDetailById(1L)).thenReturn(Optional.of(mockDetailView));

        var response = mvc.perform(get("/api/properties/1/details")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao buscar ranking")
    void testaGetRanking() throws Exception {
        when(propertyRepository.findAllRanking()).thenReturn(List.of());

        var response = mvc.perform(get("/api/properties/ranking")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao buscar propriedades do residente logado")
    void testaGetMyResidentProperties() throws Exception {
        Authentication auth = setupAuth("resident@ufape.edu.br");
        when(propertyRepository.findResidentDetails(1L)).thenReturn(List.of());

        var response = mvc.perform(get("/api/properties/resident/me").principal(auth)).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found ao publicar imóvel inexistente")
    void testaPublishPropertyNotFound() throws Exception {
        Authentication auth = setupAuth("dono@ufape.edu.br");
        when(propertyRepository.findById(1L)).thenReturn(Optional.empty());

        var response = mvc.perform(patch("/api/properties/1/publish").principal(auth)).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando confirmar estudante falhar")
    void testaConfirmStudentComErro() throws Exception {
        when(propertyService.confirmStudent(1L, 5L))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "algo deu errado"));

        var response = mvc.perform(patch("/api/properties/1/confirm").param("studentId", "5")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("algo deu errado");
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found quando confirmar estudante falhar com ResponseStatusException")
    void testaConfirmStudentNotFound() throws Exception {
        when(propertyService.confirmStudent(1L, 5L))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "não encontrado"));

        var response = mvc.perform(patch("/api/properties/1/confirm").param("studentId", "5")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString()).contains("não encontrado");
    }

}
