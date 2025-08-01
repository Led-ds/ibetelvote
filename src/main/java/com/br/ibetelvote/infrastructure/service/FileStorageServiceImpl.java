package com.br.ibetelvote.infrastructure.service;

import com.br.ibetelvote.domain.services.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;
    private final String baseUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 600;

    public FileStorageServiceImpl(@Value("${app.file.upload-dir:./uploads}") String uploadDir,
                                  @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de upload", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String directory) throws IOException {
        validateFile(file);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = FilenameUtils.getExtension(originalFileName).toLowerCase();
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;

        // Criar diretório se não existir
        Path targetLocation = this.fileStorageLocation.resolve(directory);
        Files.createDirectories(targetLocation);

        Path filePath = targetLocation.resolve(fileName);

        // Se for imagem, redimensionar
        if (isImageFile(fileExtension)) {
            processImageFile(file, filePath);
        } else {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Arquivo salvo: {} -> {}", originalFileName, fileName);
        return directory + "/" + fileName;
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("Arquivo deletado: {}", fileName);
        } catch (IOException e) {
            log.error("Erro ao deletar arquivo: {}", fileName, e);
        }
    }

    @Override
    public boolean fileExists(String fileName) {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        return Files.exists(filePath);
    }

    @Override
    public byte[] loadFile(String fileName) throws IOException {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();

        if (!Files.exists(filePath)) {
            throw new IOException("Arquivo não encontrado: " + fileName);
        }

        return Files.readAllBytes(filePath);
    }

    @Override
    public String getFileUrl(String fileName) {
        return baseUrl + "/api/v1/files/" + fileName;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo está vazio");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Arquivo muito grande. Máximo permitido: 5MB");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.contains("..")) {
            throw new IllegalArgumentException("Nome do arquivo inválido");
        }

        String fileExtension = FilenameUtils.getExtension(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("Tipo de arquivo não permitido. Permitidos: " +
                    String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private boolean isImageFile(String extension) {
        return Arrays.asList("jpg", "jpeg", "png", "webp").contains(extension.toLowerCase());
    }

    private void processImageFile(MultipartFile file, Path filePath) throws IOException {
        Thumbnails.of(file.getInputStream())
                .size(MAX_WIDTH, MAX_HEIGHT)
                .keepAspectRatio(true)
                .outputQuality(0.8)
                .toFile(filePath.toFile());
    }
}