package com.br.ibetelvote.infrastructure.resources;

import com.br.ibetelvote.application.eleicao.dto.*;
import com.br.ibetelvote.domain.services.EleicaoService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/eleicoes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Eleições", description = "Endpoints para gestão de eleições")
@SecurityRequirement(name = "bearerAuth")
public class EleicaoController {

    private final EleicaoService eleicaoService;

    // === OPERAÇÕES BÁSICAS ===

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Listar eleições", description = "Lista todas as eleições com filtros opcionais")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de eleições retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Page<EleicaoListResponse>> getAllEleicoes(
            @Parameter(description = "Filtro por nome") @RequestParam(required = false) String nome,
            @Parameter(description = "Filtro por status ativo") @RequestParam(required = false) Boolean ativa,
            @Parameter(description = "Filtro por status (aberta, encerrada, futura)") @RequestParam(required = false) String status,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "dataInicio") String sort,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "desc") String direction
    ) {
        EleicaoFilterRequest filter = EleicaoFilterRequest.builder()
                .nome(nome)
                .ativa(ativa)
                .status(status)
                .page(page)
                .size(size)
                .sort(sort)
                .direction(direction)
                .build();

        Page<EleicaoListResponse> eleicoes = eleicaoService.getAllEleicoes(filter);
        return ResponseEntity.ok(eleicoes);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar eleição", description = "Cria uma nova eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Eleição criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<EleicaoResponse> createEleicao(@Valid @RequestBody CreateEleicaoRequest request) {
        EleicaoResponse response = eleicaoService.createEleicao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Buscar eleição por ID", description = "Retorna os dados de uma eleição específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Eleição encontrada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<EleicaoResponse> getEleicaoById(@PathVariable UUID id) {
        EleicaoResponse response = eleicaoService.getEleicaoById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar eleição", description = "Atualiza os dados de uma eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Eleição atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<EleicaoResponse> updateEleicao(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEleicaoRequest request) {
        EleicaoResponse response = eleicaoService.updateEleicao(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Remover eleição", description = "Remove uma eleição do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eleição removida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Eleição não pode ser removida"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<Void> deleteEleicao(@PathVariable UUID id) {
        eleicaoService.deleteEleicao(id);
        return ResponseEntity.noContent().build();
    }

    // === OPERAÇÕES DE CONTROLE ===

    @PostMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ativar eleição", description = "Ativa uma eleição para votação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Eleição ativada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Eleição não pode ser ativada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<Void> ativarEleicao(@PathVariable UUID id) {
        eleicaoService.ativarEleicao(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desativar eleição", description = "Desativa uma eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Eleição desativada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<Void> desativarEleicao(@PathVariable UUID id) {
        eleicaoService.desativarEleicao(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/encerrar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Encerrar eleição", description = "Encerra uma eleição permanentemente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Eleição encerrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Eleição não pode ser encerrada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<Void> encerrarEleicao(@PathVariable UUID id) {
        eleicaoService.encerrarEleicao(id);
        return ResponseEntity.ok().build();
    }

    // === CONSULTAS ESPECÍFICAS ===

    @GetMapping("/ativa")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Buscar eleição ativa", description = "Retorna a eleição atualmente ativa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Eleição ativa encontrada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Nenhuma eleição ativa")
    })
    public ResponseEntity<EleicaoResponse> getEleicaoAtiva() {
        EleicaoResponse response = eleicaoService.getEleicaoAtiva();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/abertas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Eleições abertas", description = "Lista eleições com votação em andamento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<EleicaoListResponse>> getEleicoesAbertas() {
        List<EleicaoListResponse> eleicoes = eleicaoService.getEleicoesAbertas();
        return ResponseEntity.ok(eleicoes);
    }

    @GetMapping("/encerradas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Eleições encerradas", description = "Lista eleições já finalizadas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<EleicaoListResponse>> getEleicoesEncerradas() {
        List<EleicaoListResponse> eleicoes = eleicaoService.getEleicoesEncerradas();
        return ResponseEntity.ok(eleicoes);
    }

    @GetMapping("/futuras")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Eleições futuras", description = "Lista eleições agendadas para o futuro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<EleicaoListResponse>> getEleicoesFuturas() {
        List<EleicaoListResponse> eleicoes = eleicaoService.getEleicoesFuturas();
        return ResponseEntity.ok(eleicoes);
    }

    @GetMapping("/recentes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Eleições recentes", description = "Lista eleições mais recentes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<EleicaoListResponse>> getRecentEleicoes(
            @Parameter(description = "Limite de resultados") @RequestParam(defaultValue = "10") int limit) {
        List<EleicaoListResponse> eleicoes = eleicaoService.getRecentEleicoes(limit);
        return ResponseEntity.ok(eleicoes);
    }

    // === VALIDAÇÕES ===

    @GetMapping("/{id}/can-activate")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar se pode ativar", description = "Verifica se uma eleição pode ser ativada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<Boolean> canActivateEleicao(@PathVariable UUID id) {
        boolean canActivate = eleicaoService.canActivateEleicao(id);
        return ResponseEntity.ok(canActivate);
    }

    @GetMapping("/{id}/is-open")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Verificar se está aberta", description = "Verifica se uma eleição está aberta para votação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<Boolean> isEleicaoAberta(@PathVariable UUID id) {
        boolean isOpen = eleicaoService.isEleicaoAberta(id);
        return ResponseEntity.ok(isOpen);
    }

    // === ESTATÍSTICAS ===

    @GetMapping("/stats/total")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de eleições", description = "Retorna o total de eleições cadastradas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalEleicoes() {
        long total = eleicaoService.getTotalEleicoes();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/ativas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de eleições ativas", description = "Retorna o total de eleições ativas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalEleicoesAtivas() {
        long total = eleicaoService.getTotalEleicoesAtivas();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/encerradas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de eleições encerradas", description = "Retorna o total de eleições encerradas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalEleicoesEncerradas() {
        long total = eleicaoService.getTotalEleicoesEncerradas();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/futuras")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de eleições futuras", description = "Retorna o total de eleições futuras")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalEleicoesFuturas() {
        long total = eleicaoService.getTotalEleicoesFuturas();
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