package com.br.ibetelvote.infrastructure.resources;

import com.br.ibetelvote.application.membro.dto.*;
import com.br.ibetelvote.application.shared.dto.UploadPhotoResponse;
import com.br.ibetelvote.domain.services.MembroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/membros")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Membros", description = "Endpoints para gestão de membros da igreja")
@SecurityRequirement(name = "bearerAuth")
public class MembroController {

    private final MembroService membroService;

    // === OPERAÇÕES BÁSICAS ===

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Listar membros", description = "Lista todos os membros com filtros opcionais")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de membros retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Page<MembroListResponse>> getAllMembros(
            @Parameter(description = "Filtro por nome") @RequestParam(required = false) String nome,
            @Parameter(description = "Filtro por email") @RequestParam(required = false) String email,
            @Parameter(description = "Filtro por cargo") @RequestParam(required = false) String cargo,
            @Parameter(description = "Filtro por status ativo") @RequestParam(required = false) Boolean ativo,
            @Parameter(description = "Filtro por associação com usuário") @RequestParam(required = false) Boolean hasUser,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "nome") String sort,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "asc") String direction
    ) {
        MembroFilterRequest filter = MembroFilterRequest.builder()
                .nome(nome)
                .email(email)
                .cargo(cargo)
                .ativo(ativo)
                .hasUser(hasUser)
                .page(page)
                .size(size)
                .sort(sort)
                .direction(direction)
                .build();

        Page<MembroListResponse> membros = membroService.getAllMembros(filter);
        return ResponseEntity.ok(membros);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cadastrar membro", description = "Cadastra um novo membro da igreja")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membro criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    public ResponseEntity<MembroResponse> createMembro(@Valid @RequestBody CreateMembroRequest request) {
        MembroResponse response = membroService.createMembro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Buscar membro por ID", description = "Retorna os dados de um membro específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro encontrado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroResponse> getMembroById(@PathVariable UUID id) {
        MembroResponse response = membroService.getMembroById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar membro", description = "Atualiza os dados de um membro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroResponse> updateMembro(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMembroRequest request) {
        MembroResponse response = membroService.updateMembro(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Remover membro", description = "Remove um membro do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Membro removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<Void> deleteMembro(@PathVariable UUID id) {
        membroService.deleteMembro(id);
        return ResponseEntity.noContent().build();
    }

    // === OPERAÇÕES DE CONTROLE ===

    @PostMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ativar membro", description = "Ativa um membro desativado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro ativado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<Void> activateMembro(@PathVariable UUID id) {
        membroService.activateMembro(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desativar membro", description = "Desativa um membro ativo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro desativado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<Void> deactivateMembro(@PathVariable UUID id) {
        membroService.deactivateMembro(id);
        return ResponseEntity.ok().build();
    }

    // === OPERAÇÕES DE ASSOCIAÇÃO ===

    @PostMapping("/{id}/associate-user")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Associar usuário", description = "Associa um usuário ao membro para acesso ao sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário associado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou usuário já associado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro ou usuário não encontrado")
    })
    public ResponseEntity<Void> associateUser(
            @PathVariable UUID id,
            @Valid @RequestBody AssociateUserRequest request) {
        membroService.associateUser(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/dissociate-user")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desassociar usuário", description = "Remove a associação entre membro e usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário desassociado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Membro não possui usuário associado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<Void> dissociateUser(@PathVariable UUID id) {
        membroService.dissociateUser(id);
        return ResponseEntity.ok().build();
    }

    // === OPERAÇÕES DE FOTO ===

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Upload de foto", description = "Faz upload da foto do membro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<UploadPhotoResponse> uploadPhoto(
            @PathVariable UUID id,
            @Parameter(description = "Arquivo de imagem (JPG, PNG, WEBP)")
            @RequestParam("file") MultipartFile file) {
        UploadPhotoResponse response = membroService.uploadPhoto(id, file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/foto")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Remover foto", description = "Remove a foto do membro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto removida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<Void> removePhoto(@PathVariable UUID id) {
        membroService.removePhoto(id);
        return ResponseEntity.ok().build();
    }

    // === CONSULTAS ESPECÍFICAS ===

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Buscar membro por email", description = "Retorna os dados de um membro pelo email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro encontrado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroResponse> getMembroByEmail(@PathVariable String email) {
        MembroResponse response = membroService.getMembroByEmail(email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/without-user")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Membros sem usuário", description = "Lista membros que não possuem usuário associado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<MembroListResponse>> getMembrosWithoutUser() {
        List<MembroListResponse> membros = membroService.getMembrosWithoutUser();
        return ResponseEntity.ok(membros);
    }

    @GetMapping("/with-user")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Membros com usuário", description = "Lista membros que possuem usuário associado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<MembroListResponse>> getMembrosWithUser() {
        List<MembroListResponse> membros = membroService.getMembrosWithUser();
        return ResponseEntity.ok(membros);
    }

    @GetMapping("/without-photo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Membros sem foto", description = "Lista membros que não possuem foto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<MembroListResponse>> getMembrosWithoutPhoto() {
        List<MembroListResponse> membros = membroService.getMembrosWithoutPhoto();
        return ResponseEntity.ok(membros);
    }

    @GetMapping("/incomplete-profile")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Membros com perfil incompleto", description = "Lista membros com dados obrigatórios faltando")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<MembroListResponse>> getMembrosWithIncompleteProfile() {
        List<MembroListResponse> membros = membroService.getMembrosWithIncompleteProfile();
        return ResponseEntity.ok(membros);
    }

    // === ESTATÍSTICAS ===

    @GetMapping("/stats/total")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de membros", description = "Retorna o total de membros cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalMembros() {
        long total = membroService.getTotalMembros();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/ativos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de membros ativos", description = "Retorna o total de membros ativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalMembrosAtivos() {
        long total = membroService.getTotalMembrosAtivos();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/with-user")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de membros com usuário", description = "Retorna o total de membros associados a usuários")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalMembrosWithUser() {
        long total = membroService.getTotalMembrosWithUser();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/without-user")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de membros sem usuário", description = "Retorna o total de membros não associados a usuários")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalMembrosWithoutUser() {
        long total = membroService.getTotalMembrosWithoutUser();
        return ResponseEntity.ok(total);
    }

    // === EXCEPTION HANDLERS ===

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_REQUEST")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("Erro interno do servidor")
                .path(request.getRequestURI())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ErrorResponse DTO interno
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class ErrorResponse {
        private String code;
        private String message;
        private String path;
        private java.time.LocalDateTime timestamp;
    }
}