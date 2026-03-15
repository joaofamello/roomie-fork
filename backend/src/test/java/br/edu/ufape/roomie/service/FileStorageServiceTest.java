package br.edu.ufape.roomie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class FileStorageServiceTest {

  @TempDir Path tempDir;
  private FileStorageService fileStorageService;

  @BeforeEach
  void setUp() {
    fileStorageService = new FileStorageService(tempDir.toAbsolutePath().toString());
  }

  @Test
  @DisplayName("Deve salvar arquivo com sucesso e gerar nome único com UUID")
  void deveSalvarArquivoComSucesso() {
    // Cenário
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "casa-praia.jpg", MediaType.IMAGE_JPEG_VALUE, "conteudo-da-imagem".getBytes());

    String savedFileName = fileStorageService.storeFile(file);

    assertThat(savedFileName).isNotNull();
    assertThat(savedFileName).isNotEqualTo("casa-praia.jpg");
    assertThat(savedFileName).endsWith(".jpg");

    assertThat(Files.exists(tempDir.resolve(savedFileName))).isTrue();
  }

  @Test
  @DisplayName("Deve lançar exceção se nome do arquivo conter '..'")
  void deveRejeitarPathTraversal() {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "../arquivo-malicioso.txt", MediaType.TEXT_PLAIN_VALUE, "virus".getBytes());

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              fileStorageService.storeFile(file);
            });

    assertThat(exception.getMessage()).contains("caminho inválido");
  }

  @Test
  @DisplayName("Deve rejeitar nome do arquivo vazio ou em branco")
  void deveRejeitarNomeVazio() {
    MockMultipartFile file =
        new MockMultipartFile("file", "   ", MediaType.IMAGE_JPEG_VALUE, "conteudo".getBytes());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              fileStorageService.storeFile(file);
            });

    assertThat(exception.getMessage()).contains("Nome do arquivo inválido");
  }

  @Test
  @DisplayName("Deve rejeitar nome do arquivo nulo usando mock")
  void deveRejeitarNomeNulo() {
    org.springframework.web.multipart.MultipartFile mockFile =
        org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
    org.mockito.Mockito.when(mockFile.getOriginalFilename()).thenReturn(null);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              fileStorageService.storeFile(mockFile);
            });

    assertThat(exception.getMessage()).contains("Nome do arquivo inválido");
  }

  @Test
  @DisplayName("Deve rejeitar nome de arquivo vazio para garantir a branch no isBlank")
  void deveRejeitarNomeExatamenteVazio() {
    MockMultipartFile file =
        new MockMultipartFile("file", "", MediaType.IMAGE_JPEG_VALUE, "conteudo".getBytes());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              fileStorageService.storeFile(file);
            });

    assertThat(exception.getMessage()).contains("Nome do arquivo inválido");
  }

  @Test
  @DisplayName("Deve lançar exceção de inicialização ao tentar criar diretório em caminho inválido")
  void deveLancarExcecaoAoCriarDiretorio() throws Exception {
    Path conflictFile = tempDir.resolve("arquivo_conflito.txt");
    Files.createFile(conflictFile);

    // Tentar usar o arquivo existente como pasta base falhará
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              new FileStorageService(conflictFile.toAbsolutePath().toString() + "/novo_diretorio");
            });

    assertThat(exception.getMessage()).contains("Não foi possível criar o diretório");
  }

  @Test
  @DisplayName("Deve lançar exceção ao encontrar IOException durante a cópia")
  void deveLancarExcecaoNoCatchIoException() throws Exception {
    org.springframework.web.multipart.MultipartFile mockFile =
        org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
    org.mockito.Mockito.when(mockFile.getOriginalFilename()).thenReturn("imagem.png");
    org.mockito.Mockito.when(mockFile.getInputStream())
        .thenThrow(new java.io.IOException("Falha forçada no Input Stream"));

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              fileStorageService.storeFile(mockFile);
            });

    assertThat(exception.getMessage()).contains("Não foi possível armazenar o arquivo");
  }
}
