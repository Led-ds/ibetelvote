package com.br.ibetelvote.infrastructure.resources;

import com.br.ibetelvote.application.eleicao.dto.*;
import com.br.ibetelvote.application.shared.dto.UploadPhotoResponse;
import com.br.ibetelvote.domain.services.CandidatoService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/candidatos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Candidatos", description = "Endpoints para gestão de candidatos")
@SecurityRequirement(name = "bearerAuth")
public class CandidatoController {

    private final CandidatoService candidatoService;

    // === OPERAÇÕES BÁSICAS ===

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar candidato", description = "Cadastra um novo candidato para uma eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Candidato criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Membro já é candidato para este cargo")
    })
    public ResponseEntity<CandidatoResponse> createCandidato(@Valid @RequestBody CreateCandidatoRequest request) {
        CandidatoResponse response = candidatoService.createCandidato(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Buscar candidato por ID", description = "Retorna os dados de um candidato específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candidato encontrado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Candidato não encontrado")
    })
    public ResponseEntity<CandidatoResponse> getCandidatoById(@PathVariable UUID id) {
        CandidatoResponse response = candidatoService.getCandidatoById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar candidato", description = "Atualiza os dados de um candidato")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candidato atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Candidato não encontrado")
    })
    public ResponseEntity<CandidatoResponse> updateCandidato(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCandidatoRequest request) {
        CandidatoResponse response = candidatoService.updateCandidato(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Remover candidato", description = "Remove um candidato do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Candidato removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Candidato não pode ser removido"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Candidato não encontrado")
    })
    public ResponseEntity<Void> deleteCandidato(@PathVariable UUID id) {
        candidatoService.deleteCandidato(id);
        return ResponseEntity.noContent().build();
    }

    // === CONSULTAS POR ELEIÇÃO E CARGO ===

    @GetMapping("/eleicao/{eleicaoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Candidatos por eleição", description = "Lista todos os candidatos de uma eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<List<CandidatoResponse>> getCandidatosByEleicaoId(@PathVariable UUID eleicaoId) {
        List<CandidatoResponse> candidatos = candidatoService.getCandidatosByEleicaoId(eleicaoId);
        return ResponseEntity.ok(candidatos);
    }

    @GetMapping("/cargo/{cargoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Candidatos por cargo", description = "Lista todos os candidatos de um cargo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado")
    })
    public ResponseEntity<List<CandidatoResponse>> getCandidatosByCargoId(@PathVariable UUID cargoId) {
        List<CandidatoResponse> candidatos = candidatoService.getCandidatosByCargoId(cargoId);
        return ResponseEntity.ok(candidatos);
    }

    @GetMapping("/cargo/{cargoId}/aprovados")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Candidatos aprovados por cargo", description = "Lista candidatos aprovados de um cargo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado")
    })
    public ResponseEntity<List<CandidatoResponse>> getCandidatosAprovados(@PathVariable UUID cargoId) {
        List<CandidatoResponse> candidatos = candidatoService.getCandidatosAprovados(cargoId);
        return ResponseEntity.ok(candidatos);
    }

    // === OPERAÇÕES DE APROVAÇÃO ===

    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Aprovar candidato", description = "Aprova um candidato para participar da eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candidato aprovado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Candidato não pode ser aprovado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Candidato não encontrado")
    })
    public ResponseEntity<Void> aprovarCandidato(@PathVariable UUID id) {
        candidatoService.aprovarCandidato(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reprovar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Reprovar candidato", description = "Reprova um candidato com motivo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candidato reprovado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Motivo é obrigatório"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Candidato não encontrado")
    })
    public ResponseEntity<Void> reprovarCandidato(
            @PathVariable UUID id,
            @RequestBody ReprovarCandidatoRequest request) {
        candidatoService.reprovarCandidato(id, request.getMotivo());
        return ResponseEntity.ok().build();
    }

    // === OPERAÇÕES DE CONTROLE ===

    @PostMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ativar candidato", description = "Ativa um candidato desativado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candidato ativado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Candidato não encontrado")
    })
    public ResponseEntity<Void> ativarCandidato(@PathVariable UUID id) {
        candidatoService.ativarCandidato(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desativar candidato", description = "Desativa um candidato ativo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candidato desativado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Candidato não encontrado")
    })
    public ResponseEntity<Void> desativarCandidato(@PathVariable UUID id) {
        candidatoService.desativarCandidato(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/numero")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Definir número do candidato", description = "Define o número de campanha do candidato")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Número definido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Número já está em uso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Candidato não encontrado")
    })
    public ResponseEntity<Void> definirNumeroCandidato(
            @PathVariable UUID id,
            @RequestBody DefinirNumeroRequest request) {
        candidatoService.definirNumeroCandidato(id, request.getNumero());
        return ResponseEntity.ok().build();
    }

    // === OPERAÇÕES DE FOTO ===

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Upload de foto de campanha", description = "Faz upload da foto de campanha do candidato")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Candidato não encontrado")
    })
    public ResponseEntity<UploadPhotoResponse> uploadFotoCampanha(
            @PathVariable UUID id,
            @Parameter(description = "Arquivo de imagem (JPG, PNG, WEBP)")
            @RequestParam("file") MultipartFile file) {
        UploadPhotoResponse response = candidatoService.uploadFotoCampanha(id, file);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/foto")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Remover foto de campanha", description = "Remove a foto de campanha do candidato")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto removida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Candidato não encontrado")
    })
    public ResponseEntity<Void> removeFotoCampanha(@PathVariable UUID id) {
        candidatoService.removeFotoCampanha(id);
        return ResponseEntity.ok().build();
    }

    // === CONSULTAS ESPECÍFICAS ===

    @GetMapping("/pendentes-aprovacao")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Candidatos pendentes", description = "Lista candidatos aguardando aprovação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<CandidatoResponse>> getCandidatosPendentesAprovacao() {
        List<CandidatoResponse> candidatos = candidatoService.getCandidatosPendentesAprovacao();
        return ResponseEntity.ok(candidatos);
    }

    @GetMapping("/membro/{membroId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Candidaturas por membro", description = "Lista todas as candidaturas de um membro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<List<CandidatoResponse>> getCandidatosByMembroId(@PathVariable UUID membroId) {
        List<CandidatoResponse> candidatos = candidatoService.getCandidatosByMembroId(membroId);
        return ResponseEntity.ok(candidatos);
    }

    @GetMapping("/numero/{numero}/eleicao/{eleicaoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Candidato por número", description = "Busca candidato pelo número na eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candidato encontrado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Candidato não encontrado")
    })
    public ResponseEntity<CandidatoResponse> getCandidatoByNumero(
            @PathVariable String numero,
            @PathVariable UUID eleicaoId) {
        CandidatoResponse response = candidatoService.getCandidatoByNumero(numero, eleicaoId);
        return ResponseEntity.ok(response);
    }

    // === VALIDAÇÕES ===

    @GetMapping("/exists/membro-cargo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar candidatura existente", description = "Verifica se membro já é candidato ao cargo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Boolean> existsCandidatoByMembroAndCargo(
            @Parameter(description = "ID do membro") @RequestParam UUID membroId,
            @Parameter(description = "ID do cargo") @RequestParam UUID cargoId) {
        boolean exists = candidatoService.existsCandidatoByMembroAndCargo(membroId, cargoId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/numero")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar número disponível", description = "Verifica se número de candidato está disponível")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Boolean> existsCandidatoByNumero(
            @Parameter(description = "Número do candidato") @RequestParam String numero,
            @Parameter(description = "ID da eleição") @RequestParam UUID eleicaoId) {
        boolean exists = candidatoService.existsCandidatoByNumero(numero, eleicaoId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{id}/can-delete")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar se pode deletar", description = "Verifica se um candidato pode ser removido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Candidato não encontrado")
    })
    public ResponseEntity<Boolean> canDeleteCandidato(@PathVariable UUID id) {
        boolean canDelete = candidatoService.canDeleteCandidato(id);
        return ResponseEntity.ok(canDelete);
    }

    // === ESTATÍSTICAS ===

    @GetMapping("/stats/total/eleicao/{eleicaoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total por eleição", description = "Retorna o total de candidatos de uma eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalCandidatosByEleicao(@PathVariable UUID eleicaoId) {
        long total = candidatoService.getTotalCandidatosByEleicao(eleicaoId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/total/cargo/{cargoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total por cargo", description = "Retorna o total de candidatos de um cargo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalCandidatosByCargo(@PathVariable UUID cargoId) {
        long total = candidatoService.getTotalCandidatosByCargo(cargoId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/total/aprovados")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de aprovados", description = "Retorna o total de candidatos aprovados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalCandidatosAprovados() {
        long total = candidatoService.getTotalCandidatosAprovados();
        return ResponseEntity.ok(total);
    }

    // === DTOs INTERNOS ===

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class ReprovarCandidatoRequest {
        @jakarta.validation.constraints.NotBlank(message = "Motivo é obrigatório")
        private String motivo;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class DefinirNumeroRequest {
        @jakarta.validation.constraints.NotBlank(message = "Número é obrigatório")
        private String numero;
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

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_STATE")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
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