package com.br.ibetelvote.infrastructure.resources;

import com.br.ibetelvote.domain.services.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Arquivos", description = "Endpoints para servir arquivos estáticos")
public class FileController {

    private final FileStorageService fileStorageService;

    private static final Map<String, MediaType> MEDIA_TYPE_MAP = new HashMap<>();

    static {
        MEDIA_TYPE_MAP.put("jpg", MediaType.IMAGE_JPEG);
        MEDIA_TYPE_MAP.put("jpeg", MediaType.IMAGE_JPEG);
        MEDIA_TYPE_MAP.put("png", MediaType.IMAGE_PNG);
        MEDIA_TYPE_MAP.put("webp", MediaType.valueOf("image/webp"));
        MEDIA_TYPE_MAP.put("gif", MediaType.IMAGE_GIF);
        MEDIA_TYPE_MAP.put("pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping("/{fileName:.+}")
    @Operation(summary = "Servir arquivo", description = "Retorna um arquivo estático")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Arquivo não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro ao carregar arquivo")
    })
    public ResponseEntity<Resource> serveFile(
            @Parameter(description = "Nome do arquivo") @PathVariable String fileName,
            HttpServletRequest request) {

        try {
            // Verificar se arquivo existe
            if (!fileStorageService.fileExists(fileName)) {
                log.warn("Arquivo não encontrado: {}", fileName);
                return ResponseEntity.notFound().build();
            }

            // Carregar arquivo
            byte[] fileData = fileStorageService.loadFile(fileName);
            Resource resource = new ByteArrayResource(fileData);

            // Determinar content type
            String fileExtension = FilenameUtils.getExtension(fileName).toLowerCase();
            MediaType mediaType = MEDIA_TYPE_MAP.getOrDefault(fileExtension, MediaType.APPLICATION_OCTET_STREAM);

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentLength(fileData.length);

            // Cache headers para imagens
            if (isImageFile(fileExtension)) {
                headers.setCacheControl("public, max-age=31536000"); // 1 ano
            }

            log.debug("Servindo arquivo: {} ({})", fileName, mediaType);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (IOException e) {
            log.error("Erro ao carregar arquivo: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download/{fileName:.+}")
    @Operation(summary = "Download de arquivo", description = "Força o download de um arquivo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Download iniciado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Arquivo não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro ao carregar arquivo")
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Nome do arquivo") @PathVariable String fileName) {

        try {
            if (!fileStorageService.fileExists(fileName)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileData = fileStorageService.loadFile(fileName);
            Resource resource = new ByteArrayResource(fileData);

            String fileExtension = FilenameUtils.getExtension(fileName).toLowerCase();
            MediaType mediaType = MEDIA_TYPE_MAP.getOrDefault(fileExtension, MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(fileData.length)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (IOException e) {
            log.error("Erro ao fazer download do arquivo: {}", fileName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{fileName:.+}")
    @Operation(summary = "Deletar arquivo", description = "Remove um arquivo do servidor")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Arquivo deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Arquivo não encontrado")
    })
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "Nome do arquivo") @PathVariable String fileName) {

        if (!fileStorageService.fileExists(fileName)) {
            return ResponseEntity.notFound().build();
        }

        fileStorageService.deleteFile(fileName);
        log.info("Arquivo deletado: {}", fileName);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/{fileName:.+}")
    @Operation(summary = "Verificar existência", description = "Verifica se um arquivo existe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "404", description = "Arquivo não encontrado")
    })
    public ResponseEntity<Map<String, Object>> checkFileExists(
            @Parameter(description = "Nome do arquivo") @PathVariable String fileName) {

        boolean exists = fileStorageService.fileExists(fileName);

        Map<String, Object> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("exists", exists);

        if (exists) {
            response.put("url", fileStorageService.getFileUrl(fileName));
        }

        return ResponseEntity.ok(response);
    }

    // Exception Handlers
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "INVALID_FILE");
        error.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> handleIOException(IOException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "FILE_ERROR");
        error.put("message", "Erro ao processar arquivo");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Métodos auxiliares
    private boolean isImageFile(String extension) {
        return extension != null &&
                (extension.equals("jpg") || extension.equals("jpeg") ||
                        extension.equals("png") || extension.equals("webp") ||
                        extension.equals("gif"));
    }
}