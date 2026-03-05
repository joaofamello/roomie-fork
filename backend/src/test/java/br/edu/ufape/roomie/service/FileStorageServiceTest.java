package br.edu.ufape.roomie.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toAbsolutePath().toString());
    }

    @Test
    @DisplayName("Deve salvar arquivo com sucesso e gerar nome único com UUID")
    void deveSalvarArquivoComSucesso() {
        // Cenário
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "casa-praia.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo-da-imagem".getBytes()
        );

        String savedFileName = fileStorageService.storeFile(file);

        assertThat(savedFileName).isNotNull();
        assertThat(savedFileName).isNotEqualTo("casa-praia.jpg");
        assertThat(savedFileName).endsWith(".jpg");

        assertThat(Files.exists(tempDir.resolve(savedFileName))).isTrue();
    }

    @Test
    @DisplayName("Deve lançar exceção se nome do arquivo conter '..'")
    void deveRejeitarPathTraversal() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../arquivo-malicioso.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "virus".getBytes()
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.storeFile(file);
        });

        assertThat(exception.getMessage()).contains("caminho inválido");
    }
}