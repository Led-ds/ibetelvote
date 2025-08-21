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
@Tag(name = "Eleições", description = "Endpoints para gestão de eleições - Sistema refatorado")
@SecurityRequirement(name = "bearerAuth")
public class EleicaoController {

    private final EleicaoService eleicaoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar eleição",
            description = "Cria uma nova eleição no sistema. A eleição é criada inativa e deve ser ativada após configuração dos candidatos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Eleição criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou conflito de período"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<EleicaoResponse> createEleicao(@Valid @RequestBody CreateEleicaoRequest request) {
        EleicaoResponse response = eleicaoService.createEleicao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Buscar eleição por ID",
            description = "Retorna os dados completos de uma eleição específica, incluindo candidatos e cargos com candidatos")
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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Listar eleições",
            description = "Lista todas as eleições com filtros opcionais. Suporte a paginação e ordenação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de eleições retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Page<EleicaoListResponse>> getAllEleicoes(
            @Parameter(description = "Filtro por nome") @RequestParam(required = false) String nome,
            @Parameter(description = "Filtro por status ativo") @RequestParam(required = false) Boolean ativa,
            @Parameter(description = "Filtro por status (aberta, encerrada, futura)") @RequestParam(required = false) String status,
            @Parameter(description = "Filtro por eleições com candidatos") @RequestParam(required = false) Boolean temCandidatos,
            @Parameter(description = "Filtro por eleições com candidatos aprovados") @RequestParam(required = false) Boolean temCandidatosAprovados,
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "dataInicio") String sort,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "desc") String direction
    ) {
        EleicaoFilterRequest filter = EleicaoFilterRequest.builder()
                .nome(nome)
                .ativa(ativa)
                .status(status)
                .temCandidatos(temCandidatos)
                .temCandidatosAprovados(temCandidatosAprovados)
                .page(page)
                .size(size)
                .sort(sort)
                .direction(direction)
                .build();

        Page<EleicaoListResponse> eleicoes = eleicaoService.getAllEleicoes(filter);
        return ResponseEntity.ok(eleicoes);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar eleição",
            description = "Atualiza os dados de uma eleição. Não é possível atualizar eleição com votação em andamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Eleição atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou conflito de estado"),
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
    @Operation(summary = "Remover eleição",
            description = "Remove uma eleição do sistema. Não é possível remover eleição com votação em andamento ou que já possua votos.")
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

    @PostMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ativar eleição",
            description = "Ativa uma eleição para votação. Valida se há candidatos aprovados e se não há conflito com outras eleições ativas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Eleição ativada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Eleição não pode ser ativada - verificar validações"),
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
    @Operation(summary = "Encerrar eleição",
            description = "Encerra uma eleição permanentemente, definindo a data de fim como agora")
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

    @GetMapping("/ativa")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Buscar eleição ativa",
            description = "Retorna a eleição atualmente ativa com candidatos aprovados")
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
    @Operation(summary = "Eleições abertas",
            description = "Lista eleições com votação em andamento (ativas e dentro do período)")
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
    @Operation(summary = "Eleições encerradas",
            description = "Lista eleições já finalizadas")
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
    @Operation(summary = "Eleições futuras",
            description = "Lista eleições agendadas para o futuro")
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
    @Operation(summary = "Eleições recentes",
            description = "Lista eleições mais recentes criadas no sistema")
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

    @GetMapping("/com-candidatos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Eleições com candidatos aprovados",
            description = "Lista eleições que possuem pelo menos um candidato aprovado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<EleicaoListResponse>> getEleicoesComCandidatos() {
        List<EleicaoListResponse> eleicoes = eleicaoService.getEleicoesComCandidatosAprovados();
        return ResponseEntity.ok(eleicoes);
    }

    // === VALIDAÇÕES ===

    @GetMapping("/{id}/validacao")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Validar eleição para ativação",
            description = "Retorna validação completa se uma eleição pode ser ativada, incluindo motivos de impedimento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<EleicaoValidacaoResponse> validarEleicao(@PathVariable UUID id) {
        EleicaoValidacaoResponse validacao = eleicaoService.validarEleicaoParaAtivacao(id);
        return ResponseEntity.ok(validacao);
    }

    @GetMapping("/{id}/can-activate")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar se pode ativar",
            description = "Verifica rapidamente se uma eleição pode ser ativada (resposta booleana)")
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
    @Operation(summary = "Verificar se está aberta",
            description = "Verifica se uma eleição está aberta para votação no momento atual")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<Boolean> isEleicaoAberta(@PathVariable UUID id) {
        boolean isOpen = eleicaoService.isEleicaoAberta(id);
        return ResponseEntity.ok(isOpen);
    }

    // === CONFIGURAÇÕES ===

    @PutMapping("/{id}/configuracoes")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar configurações",
            description = "Atualiza apenas as configurações específicas da eleição (votos, elegíveis, etc.)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configurações atualizadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou eleição em votação"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<EleicaoResponse> updateConfiguracoes(
            @PathVariable UUID id,
            @Valid @RequestBody EleicaoConfigRequest request) {
        EleicaoResponse response = eleicaoService.updateConfiguracoes(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/total")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de eleições",
            description = "Retorna o total de eleições cadastradas no sistema")
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
    @Operation(summary = "Total de eleições ativas",
            description = "Retorna o total de eleições atualmente ativas")
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
    @Operation(summary = "Total de eleições encerradas",
            description = "Retorna o total de eleições já finalizadas")
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
    @Operation(summary = "Total de eleições futuras",
            description = "Retorna o total de eleições agendadas para o futuro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalEleicoesFuturas() {
        long total = eleicaoService.getTotalEleicoesFuturas();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Estatísticas detalhadas da eleição",
            description = "Retorna estatísticas completas de uma eleição específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<EleicaoStatsResponse> getEstatisticasEleicao(@PathVariable UUID id) {
        EleicaoStatsResponse stats = eleicaoService.getEstatisticasEleicao(id);
        return ResponseEntity.ok(stats);
    }

    // === CONSULTAS AVANÇADAS ===

    @PostMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Busca avançada",
            description = "Busca eleições com filtros avançados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<EleicaoListResponse>> buscarEleicoes(
            @Valid @RequestBody EleicaoFilterRequest filter) {
        List<EleicaoListResponse> eleicoes = eleicaoService.buscarEleicoesComFiltros(filter);
        return ResponseEntity.ok(eleicoes);
    }

    // === EXCEPTION HANDLERS ===

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Argumento inválido na requisição: {} - {}", request.getRequestURI(), e.getMessage());

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
        log.warn("Estado inválido na requisição: {} - {}", request.getRequestURI(), e.getMessage());

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
        log.error("Erro interno na requisição: {} - {}", request.getRequestURI(), e.getMessage(), e);

        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("Erro interno do servidor")
                .path(request.getRequestURI())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // === ERROR RESPONSE DTO ===

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