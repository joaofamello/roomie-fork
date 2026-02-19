package br.edu.ufape.roomie.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${app.storage.upload-dir:uploads/imoveis}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível criar o diretório onde os arquivos serão armazenados.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Pega o nome original do arquivo e limpa o caminho
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Verifica se o nome do arquivo contém caracteres inválidos
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Desculpe! O nome do arquivo contém um caminho inválido " + originalFileName);
            }

            // Gera um nome único usando UUID para evitar sobrescrever imagens com o mesmo nome (ex: foto.jpg)
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Resolve o caminho final onde o arquivo será salvo
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);

            // Copia o arquivo para o diretório alvo (Substituindo se já existir)
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Retorna o nome do arquivo gerado para salvarmos no banco de dados
            return newFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Não foi possível armazenar o arquivo " + originalFileName + ". Por favor, tente novamente!", ex);
        }
    }
}