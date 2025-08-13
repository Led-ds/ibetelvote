package com.br.ibetelvote.infrastructure.resources;

import com.br.ibetelvote.application.eleicao.dto.*;
import com.br.ibetelvote.domain.services.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/votos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Votos", description = "Endpoints para gestão de votos e votação")
@SecurityRequirement(name = "bearerAuth")
public class VotoController {

    private final VotoService votoService;

    // === OPERAÇÃO PRINCIPAL ===

    @PostMapping("/votar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Registrar votação", description = "Registra os votos de um membro em uma eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Votação registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de votação inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Membro já votou nesta eleição")
    })
    public ResponseEntity<List<VotoResponse>> votar(
            @Valid @RequestBody VotarRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        UUID membroId = UUID.fromString(authentication.getName()); // Assumindo que o ID está no token
        String ipOrigem = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        List<VotoResponse> votos = votoService.votar(membroId, request, ipOrigem, userAgent);
        return ResponseEntity.status(HttpStatus.CREATED).body(votos);
    }

    // === CONSULTAS POR MEMBRO ===

    @GetMapping("/membro/{membroId}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or #membroId == authentication.name")
    @Operation(summary = "Votos por membro", description = "Lista todos os votos de um membro específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de votos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<List<VotoResponse>> getVotosByMembro(@PathVariable UUID membroId) {
        List<VotoResponse> votos = votoService.getVotosByMembroId(membroId);
        return ResponseEntity.ok(votos);
    }

    @GetMapping("/membro/{membroId}/eleicao/{eleicaoId}/ja-votou")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO') or #membroId == authentication.name")
    @Operation(summary = "Verificar se já votou", description = "Verifica se um membro já votou em uma eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Boolean> membroJaVotou(
            @PathVariable UUID membroId,
            @PathVariable UUID eleicaoId) {
        boolean jaVotou = votoService.membroJaVotou(membroId, eleicaoId);
        return ResponseEntity.ok(jaVotou);
    }

    @GetMapping("/membro/{membroId}/cargo/{cargoId}/eleicao/{eleicaoId}/ja-votou")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO') or #membroId == authentication.name")
    @Operation(summary = "Verificar voto no cargo", description = "Verifica se um membro já votou em um cargo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Boolean> membroJaVotouNoCargo(
            @PathVariable UUID membroId,
            @PathVariable UUID cargoId,
            @PathVariable UUID eleicaoId) {
        boolean jaVotou = votoService.membroJaVotouNoCargo(membroId, cargoId, eleicaoId);
        return ResponseEntity.ok(jaVotou);
    }

    // === CONSULTAS POR ELEIÇÃO ===

    @GetMapping("/eleicao/{eleicaoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Votos por eleição", description = "Lista todos os votos de uma eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de votos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<List<VotoResponse>> getVotosByEleicao(@PathVariable UUID eleicaoId) {
        List<VotoResponse> votos = votoService.getVotosByEleicaoId(eleicaoId);
        return ResponseEntity.ok(votos);
    }

    @GetMapping("/eleicao/{eleicaoId}/total")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de votos da eleição", description = "Retorna o total de votos de uma eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalVotosByEleicao(@PathVariable UUID eleicaoId) {
        long total = votoService.getTotalVotosByEleicao(eleicaoId);
        return ResponseEntity.ok(total);
    }

    // === CONSULTAS POR CARGO ===

    @GetMapping("/cargo/{cargoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Votos por cargo", description = "Lista todos os votos de um cargo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de votos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado")
    })
    public ResponseEntity<List<VotoResponse>> getVotosByCargo(@PathVariable UUID cargoId) {
        List<VotoResponse> votos = votoService.getVotosByCargoId(cargoId);
        return ResponseEntity.ok(votos);
    }

    @GetMapping("/cargo/{cargoId}/total")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de votos do cargo", description = "Retorna o total de votos de um cargo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalVotosByCargo(@PathVariable UUID cargoId) {
        long total = votoService.getTotalVotosByCargo(cargoId);
        return ResponseEntity.ok(total);
    }

    // === CONSULTAS POR CANDIDATO ===

    @GetMapping("/candidato/{candidatoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Votos por candidato", description = "Lista todos os votos de um candidato")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de votos retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Candidato não encontrado")
    })
    public ResponseEntity<List<VotoResponse>> getVotosByCandidato(@PathVariable UUID candidatoId) {
        List<VotoResponse> votos = votoService.getVotosByCandidatoId(candidatoId);
        return ResponseEntity.ok(votos);
    }

    @GetMapping("/candidato/{candidatoId}/total")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de votos do candidato", description = "Retorna o total de votos de um candidato")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalVotosByCandidato(@PathVariable UUID candidatoId) {
        long total = votoService.getTotalVotosByCandidato(candidatoId);
        return ResponseEntity.ok(total);
    }

    // === RELATÓRIOS E ESTATÍSTICAS ===

    @GetMapping("/eleicao/{eleicaoId}/estatisticas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Estatísticas da eleição", description = "Retorna estatísticas de votação de uma eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Map<String, Long>> getEstatisticasVotacao(@PathVariable UUID eleicaoId) {
        Map<String, Long> estatisticas = votoService.getEstatisticasVotacao(eleicaoId);
        return ResponseEntity.ok(estatisticas);
    }

    @GetMapping("/cargo/{cargoId}/estatisticas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Estatísticas do cargo", description = "Retorna estatísticas de votação de um cargo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Map<String, Long>> getEstatisticasPorCargo(@PathVariable UUID cargoId) {
        Map<String, Long> estatisticas = votoService.getEstatisticasPorCargo(cargoId);
        return ResponseEntity.ok(estatisticas);
    }

    @GetMapping("/eleicao/{eleicaoId}/resultados")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Resultados por candidato", description = "Retorna os resultados de votação por candidato")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<Map<String, Object>>> getResultadosPorCandidato(@PathVariable UUID eleicaoId) {
        List<Map<String, Object>> resultados = votoService.getResultadosPorCandidato(eleicaoId);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/eleicao/{eleicaoId}/progresso")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Progresso da votação", description = "Retorna o progresso de votação por hora")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progresso retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<Map<String, Object>>> getProgressoVotacaoPorHora(@PathVariable UUID eleicaoId) {
        List<Map<String, Object>> progresso = votoService.getProgressoVotacaoPorHora(eleicaoId);
        return ResponseEntity.ok(progresso);
    }

    // === VALIDAÇÕES ===

    @GetMapping("/eleicao/{eleicaoId}/disponivel")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Verificar disponibilidade", description = "Verifica se uma eleição está disponível para votação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Boolean> isEleicaoDisponivelParaVotacao(@PathVariable UUID eleicaoId) {
        boolean disponivel = votoService.isEleicaoDisponivelParaVotacao(eleicaoId);
        return ResponseEntity.ok(disponivel);
    }

    @GetMapping("/membro/{membroId}/elegivel")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO') or #membroId == authentication.name")
    @Operation(summary = "Verificar elegibilidade", description = "Verifica se um membro está elegível para votar")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Boolean> isMembroElegivelParaVotar(@PathVariable UUID membroId) {
        boolean elegivel = votoService.isMembroElegivelParaVotar(membroId);
        return ResponseEntity.ok(elegivel);
    }

    @PostMapping("/validar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Validar votação", description = "Valida uma votação antes de registrá-la")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de votação inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<String>> validarVotacao(
            @Valid @RequestBody VotarRequest request,
            Authentication authentication) {

        UUID membroId = UUID.fromString(authentication.getName());
        List<String> erros = votoService.validarVotacao(membroId, request);
        return ResponseEntity.ok(erros);
    }

    // === AUDITORIA ===

    @GetMapping("/stats/validos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de votos válidos", description = "Retorna o total de votos válidos no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalVotosValidos() {
        long total = votoService.getTotalVotosValidos();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/branco")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de votos em branco", description = "Retorna o total de votos em branco no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalVotosBranco() {
        long total = votoService.getTotalVotosBranco();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/nulo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de votos nulos", description = "Retorna o total de votos nulos no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalVotosNulo() {
        long total = votoService.getTotalVotosNulo();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/eleicao/{eleicaoId}/auditoria")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Dados para auditoria", description = "Retorna dados de votação para auditoria (sem dados sensíveis)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados de auditoria retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<VotoResponse>> getVotosParaAuditoria(@PathVariable UUID eleicaoId) {
        List<VotoResponse> votos = votoService.getVotosParaAuditoria(eleicaoId);
        return ResponseEntity.ok(votos);
    }

    // === MÉTODOS UTILITÁRIOS ===

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
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
        log.error("Erro interno no VotoController", e);
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