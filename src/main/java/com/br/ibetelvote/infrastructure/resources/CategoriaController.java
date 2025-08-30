package com.br.ibetelvote.infrastructure.resources;

import com.br.ibetelvote.application.categoria.dto.CategoriaBasicInfo;
import com.br.ibetelvote.application.categoria.dto.CategoriaResponse;
import com.br.ibetelvote.application.categoria.dto.CreateCategoriaRequest;
import com.br.ibetelvote.application.categoria.dto.UpdateCategoriaRequest;
import com.br.ibetelvote.domain.services.CategoriaService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categorias", description = "Endpoints para gestão de categorias de cargos ministeriais")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaController {

    private final CategoriaService categoriaService;

    // === OPERAÇÕES BÁSICAS ===

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar categoria", description = "Cria uma nova categoria de cargos ministeriais")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Categoria já existe com este nome")
    })
    public ResponseEntity<CategoriaResponse> createCategoria(@Valid @RequestBody CreateCategoriaRequest request) {
        CategoriaResponse response = categoriaService.createCategoria(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Buscar categoria por ID", description = "Retorna os dados de uma categoria específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    public ResponseEntity<CategoriaResponse> getCategoriaById(@PathVariable UUID id) {
        CategoriaResponse response = categoriaService.getCategoriaById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Listar categorias", description = "Lista todas as categorias com paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Page<CategoriaResponse>> getAllCategorias(
            @PageableDefault(size = 20, sort = "ordemExibicao") Pageable pageable) {
        Page<CategoriaResponse> response = categoriaService.getAllCategorias(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Listar todas as categorias", description = "Lista todas as categorias sem paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<CategoriaResponse>> getAllCategoriasList() {
        List<CategoriaResponse> response = categoriaService.getAllCategorias();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar categoria", description = "Atualiza os dados de uma categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "409", description = "Nome já existe")
    })
    public ResponseEntity<CategoriaResponse> updateCategoria(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoriaRequest request) {
        CategoriaResponse response = categoriaService.updateCategoria(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Remover categoria", description = "Remove uma categoria do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria removida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Categoria não pode ser removida"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    public ResponseEntity<Void> deleteCategoria(@PathVariable UUID id) {
        categoriaService.deleteCategoria(id);
        return ResponseEntity.noContent().build();
    }

    // === OPERAÇÕES DE STATUS ===

    @PatchMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ativar categoria", description = "Ativa uma categoria específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria ativada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    public ResponseEntity<CategoriaResponse> ativarCategoria(@PathVariable UUID id) {
        CategoriaResponse response = categoriaService.ativarCategoria(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desativar categoria", description = "Desativa uma categoria específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria desativada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    public ResponseEntity<CategoriaResponse> desativarCategoria(@PathVariable UUID id) {
        CategoriaResponse response = categoriaService.desativarCategoria(id);
        return ResponseEntity.ok(response);
    }

    // === CONSULTAS ESPECÍFICAS ===

    @GetMapping("/ativas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Categorias ativas", description = "Lista todas as categorias ativas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CategoriaResponse>> getCategoriasAtivas() {
        List<CategoriaResponse> categorias = categoriaService.getCategoriasAtivas();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/ativas/page")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Categorias ativas paginadas", description = "Lista categorias ativas com paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Page<CategoriaResponse>> getCategoriasAtivas(
            @PageableDefault(size = 20, sort = "ordemExibicao") Pageable pageable) {
        Page<CategoriaResponse> categorias = categoriaService.getCategoriasAtivas(pageable);
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/inativas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Categorias inativas", description = "Lista todas as categorias inativas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<CategoriaResponse>> getCategoriasInativas() {
        List<CategoriaResponse> categorias = categoriaService.getCategoriasInativas();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/ordenadas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Categorias ordenadas", description = "Lista categorias ordenadas por exibição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CategoriaResponse>> getCategoriasOrderByExibicao() {
        List<CategoriaResponse> categorias = categoriaService.getCategoriasOrderByExibicao();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/para-selecao")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Categorias para seleção", description = "Lista categorias básicas para seleção")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CategoriaBasicInfo>> getCategoriasParaSelecao() {
        List<CategoriaBasicInfo> categorias = categoriaService.getCategoriasParaSelecao();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/com-cargos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Categorias com cargos", description = "Lista categorias que possuem cargos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CategoriaResponse>> getCategoriasComCargos() {
        List<CategoriaResponse> categorias = categoriaService.getCategoriasComCargos();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/com-cargos-ativos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Categorias com cargos ativos", description = "Lista categorias que possuem cargos ativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CategoriaResponse>> getCategoriasComCargosAtivos() {
        List<CategoriaResponse> categorias = categoriaService.getCategoriasComCargosAtivos();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/com-cargos-disponiveis")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Categorias com cargos disponíveis", description = "Lista categorias com cargos disponíveis para eleições")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CategoriaResponse>> getCategoriasComCargosDisponiveis() {
        List<CategoriaResponse> categorias = categoriaService.getCategoriasComCargosDisponiveis();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Buscar por nome", description = "Busca categorias por nome (busca parcial)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CategoriaResponse>> searchCategoriasByNome(
            @Parameter(description = "Nome para busca") @RequestParam String nome) {
        List<CategoriaResponse> categorias = categoriaService.getCategoriasByNome(nome);
        return ResponseEntity.ok(categorias);
    }

    // === OPERAÇÕES DE ORDEM ===

    @GetMapping("/proxima-ordem")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Próxima ordem disponível", description = "Retorna a próxima ordem de exibição disponível")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordem retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Integer> getProximaOrdemExibicao() {
        Integer ordem = categoriaService.getProximaOrdemExibicao();
        return ResponseEntity.ok(ordem);
    }

    @PostMapping("/reorganizar-ordens")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Reorganizar ordens", description = "Reorganiza automaticamente as ordens de exibição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reorganização realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Void> reorganizarOrdens() {
        categoriaService.reorganizarOrdens();
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/alterar-ordem")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Alterar ordem", description = "Altera a ordem de exibição de uma categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordem alterada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "409", description = "Ordem já em uso")
    })
    public ResponseEntity<CategoriaResponse> alterarOrdem(
            @PathVariable UUID id,
            @Parameter(description = "Nova ordem de exibição") @RequestParam Integer novaOrdem) {
        CategoriaResponse response = categoriaService.alterarOrdem(id, novaOrdem);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/mover-para-cima")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Mover para cima", description = "Move categoria uma posição para cima")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria movida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "400", description = "Categoria já está na primeira posição")
    })
    public ResponseEntity<CategoriaResponse> moverParaCima(@PathVariable UUID id) {
        CategoriaResponse response = categoriaService.moverParaCima(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/mover-para-baixo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Mover para baixo", description = "Move categoria uma posição para baixo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria movida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "400", description = "Categoria já está na última posição")
    })
    public ResponseEntity<CategoriaResponse> moverParaBaixo(@PathVariable UUID id) {
        CategoriaResponse response = categoriaService.moverParaBaixo(id);
        return ResponseEntity.ok(response);
    }

    // === VALIDAÇÕES ===

    @GetMapping("/exists/nome")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar se nome existe", description = "Verifica se categoria com nome específico já existe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Boolean> existsCategoriaByNome(
            @Parameter(description = "Nome da categoria") @RequestParam String nome) {
        boolean exists = categoriaService.existsCategoriaByNome(nome);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/disponivel/nome")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar disponibilidade do nome", description = "Verifica se nome está disponível")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Boolean> isNomeDisponivel(
            @Parameter(description = "Nome da categoria") @RequestParam String nome) {
        boolean disponivel = categoriaService.isNomeDisponivel(nome);
        return ResponseEntity.ok(disponivel);
    }

    @GetMapping("/{id}/can-delete")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar se pode deletar", description = "Verifica se uma categoria pode ser removida")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    public ResponseEntity<Boolean> canDeleteCategoria(@PathVariable UUID id) {
        boolean canDelete = categoriaService.canDeleteCategoria(id);
        return ResponseEntity.ok(canDelete);
    }

    // === ESTATÍSTICAS ===

    @GetMapping("/stats/total")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de categorias", description = "Retorna o total de categorias cadastradas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Long> getTotalCategorias() {
        long total = categoriaService.getTotalCategorias();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/ativas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de categorias ativas", description = "Retorna o total de categorias ativas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Long> getTotalCategoriasAtivas() {
        long total = categoriaService.getTotalCategoriasAtivas();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/com-cargos-disponiveis")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total com cargos disponíveis", description = "Retorna total de categorias com cargos disponíveis")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Long> getTotalCategoriasComCargosDisponiveis() {
        long total = categoriaService.getTotalCategoriasComCargosDisponiveis();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/gerais")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Estatísticas gerais", description = "Retorna estatísticas gerais das categorias")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Map<String, Object>> getEstatisticasGerais() {
        Map<String, Object> stats = categoriaService.getEstatisticasGerais();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/cargos-por-categoria")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Estatísticas de cargos", description = "Retorna estatísticas de cargos por categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Map<String, Map<String, Long>>> getEstatisticasCargosPorCategoria() {
        Map<String, Map<String, Long>> stats = categoriaService.getEstatisticasCargosPorCategoria();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/hierarquias")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Distribuição hierarquias", description = "Retorna distribuição de hierarquias por categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Distribuição retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Map<String, Map<String, Long>>> getDistribuicaoHierarquias() {
        Map<String, Map<String, Long>> stats = categoriaService.getDistribuicaoHierarquias();
        return ResponseEntity.ok(stats);
    }

    // === RELATÓRIOS ===

    @GetMapping("/relatorios/mais-utilizadas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Categorias mais utilizadas", description = "Lista categorias com mais cargos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CategoriaResponse>> getCategoriasMaisUtilizadas() {
        List<CategoriaResponse> categorias = categoriaService.getCategoriasMaisUtilizadas();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/relatorios/recentes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Categorias recentes", description = "Lista categorias criadas recentemente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CategoriaResponse>> getCategoriasRecentes() {
        List<CategoriaResponse> categorias = categoriaService.getCategoriasRecentes();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/relatorios/completo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Relatório completo", description = "Gera relatório completo das categorias")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<Map<String, Object>>> getRelatorioCompleto() {
        List<Map<String, Object>> relatorio = categoriaService.getRelatorioCompleto();
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/relatorios/por-periodo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Categorias por período", description = "Lista categorias por período de criação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CategoriaResponse>> getCategoriasPorPeriodo(
            @Parameter(description = "Data início") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data fim") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<CategoriaResponse> categorias = categoriaService.getCategoriasPorPeriodo(inicio, fim);
        return ResponseEntity.ok(categorias);
    }

    // === OPERAÇÕES EM LOTE ===

    @PatchMapping("/desativar-multiplas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desativar múltiplas", description = "Desativa múltiplas categorias")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categorias desativadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<CategoriaResponse>> desativarCategorias(@RequestBody List<UUID> ids) {
        List<CategoriaResponse> categorias = categoriaService.desativarCategorias(ids);
        return ResponseEntity.ok(categorias);
    }

    @DeleteMapping("/deletar-multiplas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Deletar múltiplas", description = "Remove múltiplas categorias")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categorias removidas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "400", description = "Alguma categoria não pode ser removida")
    })
    public ResponseEntity<Void> deleteCategorias(@RequestBody List<UUID> ids) {
        categoriaService.deleteCategorias(ids);
        return ResponseEntity.noContent().build();
    }

    // === OPERAÇÕES ADMINISTRATIVAS ===

    @PostMapping("/admin/limpar-vazias")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Limpar categorias vazias", description = "Remove categorias vazias e inativas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Limpeza realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Integer> limparCategoriasVazias() {
        int quantidadeRemovida = categoriaService.limparCategoriasVazias();
        return ResponseEntity.ok(quantidadeRemovida);
    }

    @PostMapping("/admin/sincronizar-stats")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Sincronizar estatísticas", description = "Sincroniza estatísticas das categorias")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sincronização realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Void> sincronizarEstatisticas() {
        categoriaService.sincronizarEstatisticas();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/verificar-inconsistencias")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar inconsistências", description = "Verifica e reporta inconsistências nas categorias")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<String>> verificarECorrigirInconsistencias() {
        List<String> inconsistencias = categoriaService.verificarECorrigirInconsistencias();
        return ResponseEntity.ok(inconsistencias);
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
        log.error("Erro interno no controller de categorias", e);
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