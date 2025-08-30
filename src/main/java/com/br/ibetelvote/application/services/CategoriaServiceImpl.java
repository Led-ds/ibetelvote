package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.categoria.dto.CategoriaBasicInfo;
import com.br.ibetelvote.application.categoria.dto.CategoriaResponse;
import com.br.ibetelvote.application.categoria.dto.CreateCategoriaRequest;
import com.br.ibetelvote.application.categoria.dto.UpdateCategoriaRequest;
import com.br.ibetelvote.application.mapper.CategoriaMapper;
import com.br.ibetelvote.domain.entities.Categoria;
import com.br.ibetelvote.domain.services.CategoriaService;
import com.br.ibetelvote.infrastructure.repositories.CategoriaJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaJpaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    // === OPERAÇÕES BÁSICAS ===

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public CategoriaResponse createCategoria(CreateCategoriaRequest request) {
        log.info("Criando nova categoria: {}", request.getNome());

        // Validar dados
        validarDadosCategoria(request.getNome(), request.getDescricao(), request.getOrdemExibicao());

        // Verificar se nome já existe
        if (existsCategoriaByNome(request.getNome())) {
            throw new IllegalArgumentException("Já existe uma categoria com o nome: " + request.getNome());
        }

        // Definir próxima ordem se não foi informada
        if (request.getOrdemExibicao() == null || request.getOrdemExibicao() == 0) {
            request.setOrdemExibicao(getProximaOrdemExibicao());
        }

        // Verificar se ordem já está em uso
        if (categoriaRepository.existsByOrdemExibicao(request.getOrdemExibicao())) {
            throw new IllegalArgumentException("Ordem de exibição " + request.getOrdemExibicao() + " já está em uso");
        }

        Categoria categoria = categoriaMapper.toEntity(request);
        Categoria savedCategoria = categoriaRepository.save(categoria);

        log.info("Categoria criada com sucesso - ID: {}, Nome: {}", savedCategoria.getId(), savedCategoria.getNome());
        return categoriaMapper.toResponse(savedCategoria);
    }

    @Override
    @Cacheable(value = "categorias", key = "#id")
    @Transactional(readOnly = true)
    public CategoriaResponse getCategoriaById(UUID id) {
        log.debug("Buscando categoria por ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + id));

        return categoriaMapper.toResponse(categoria);
    }

    @Override
    @Cacheable(value = "categorias-page")
    @Transactional(readOnly = true)
    public Page<CategoriaResponse> getAllCategorias(Pageable pageable) {
        log.debug("Buscando todas as categorias com paginação");

        Page<Categoria> categorias = categoriaRepository.findAll(pageable);
        return categorias.map(categoriaMapper::toResponse);
    }

    @Override
    @Cacheable(value = "categorias-all")
    @Transactional(readOnly = true)
    public List<CategoriaResponse> getAllCategorias() {
        log.debug("Buscando todas as categorias");

        List<Categoria> categorias = categoriaRepository.findAllByOrderByOrdemExibicaoAsc();
        return categoriaMapper.toResponseList(categorias);
    }

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public CategoriaResponse updateCategoria(UUID id, UpdateCategoriaRequest request) {
        log.info("Atualizando categoria ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + id));

        // Validar nome se foi alterado
        if (request.getNome() != null && !request.getNome().equals(categoria.getNome())) {
            validarDadosCategoria(request.getNome(), request.getDescricao(), request.getOrdemExibicao());

            if (!isNomeDisponivelParaAtualizacao(request.getNome(), id)) {
                throw new IllegalArgumentException("Já existe uma categoria com o nome: " + request.getNome());
            }
        }

        // Validar ordem se foi alterada
        if (request.getOrdemExibicao() != null &&
                !Objects.equals(request.getOrdemExibicao(), categoria.getOrdemExibicao())) {

            if (!isOrdemDisponivelParaAtualizacao(request.getOrdemExibicao(), id)) {
                throw new IllegalArgumentException("Ordem de exibição " + request.getOrdemExibicao() + " já está em uso");
            }
        }

        categoriaMapper.updateEntityFromRequest(request, categoria);
        Categoria updatedCategoria = categoriaRepository.save(categoria);

        log.info("Categoria atualizada com sucesso - ID: {}", updatedCategoria.getId());
        return categoriaMapper.toResponse(updatedCategoria);
    }

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public void deleteCategoria(UUID id) {
        log.info("Removendo categoria ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + id));

        // Validar se pode ser removida
        validarIntegridadeParaRemocao(id);

        categoriaRepository.delete(categoria);
        log.info("Categoria removida com sucesso - ID: {}", id);
    }

    // === OPERAÇÕES DE STATUS ===

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public CategoriaResponse ativarCategoria(UUID id) {
        log.info("Ativando categoria ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + id));

        categoria.ativar();
        Categoria savedCategoria = categoriaRepository.save(categoria);

        log.info("Categoria ativada com sucesso - ID: {}", savedCategoria.getId());
        return categoriaMapper.toResponse(savedCategoria);
    }

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public CategoriaResponse desativarCategoria(UUID id) {
        log.info("Desativando categoria ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + id));

        categoria.desativar();
        Categoria savedCategoria = categoriaRepository.save(categoria);

        log.info("Categoria desativada com sucesso - ID: {}", savedCategoria.getId());
        return categoriaMapper.toResponse(savedCategoria);
    }

    // === CONSULTAS ESPECÍFICAS ===

    @Override
    @Cacheable(value = "categorias-ativas")
    @Transactional(readOnly = true)
    public List<CategoriaResponse> getCategoriasAtivas() {
        log.debug("Buscando categorias ativas");

        List<Categoria> categorias = categoriaRepository.findByAtivoTrueOrderByOrdemExibicaoAsc();
        return categoriaMapper.toResponseList(categorias);
    }

    @Override
    @Cacheable(value = "categorias-ativas-page")
    @Transactional(readOnly = true)
    public Page<CategoriaResponse> getCategoriasAtivas(Pageable pageable) {
        log.debug("Buscando categorias ativas com paginação");

        Page<Categoria> categorias = categoriaRepository.findByAtivoTrueOrderByOrdemExibicaoAsc(pageable);
        return categorias.map(categoriaMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> getCategoriasInativas() {
        log.debug("Buscando categorias inativas");

        List<Categoria> categorias = categoriaRepository.findByAtivoFalse();
        return categoriaMapper.toResponseList(categorias);
    }

    @Override
    @Cacheable(value = "categorias-ordem")
    @Transactional(readOnly = true)
    public List<CategoriaResponse> getCategoriasOrderByExibicao() {
        log.debug("Buscando categorias ordenadas por exibição");

        List<Categoria> categorias = categoriaRepository.findAllByOrderByOrdemExibicaoAsc();
        return categoriaMapper.toResponseList(categorias);
    }

    @Override
    @Cacheable(value = "categorias-selecao")
    @Transactional(readOnly = true)
    public List<CategoriaBasicInfo> getCategoriasParaSelecao() {
        log.debug("Buscando categorias para seleção");

        List<Categoria> categorias = categoriaRepository.findCategoriasParaSelecao();
        return categoriaMapper.toBasicInfoList(categorias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> getCategoriasByNome(String nome) {
        log.debug("Buscando categorias por nome: {}", nome);

        List<Categoria> categorias = categoriaRepository.findByNomeContainingIgnoreCase(nome);
        return categoriaMapper.toResponseList(categorias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> getCategoriasComCargos() {
        log.debug("Buscando categorias com cargos");

        List<Categoria> categorias = categoriaRepository.findCategoriasComCargos();
        return categoriaMapper.toResponseList(categorias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> getCategoriasSemCargos() {
        log.debug("Buscando categorias sem cargos");

        List<Categoria> categorias = categoriaRepository.findCategoriasSemCargos();
        return categoriaMapper.toResponseList(categorias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> getCategoriasComCargosAtivos() {
        log.debug("Buscando categorias com cargos ativos");

        List<Categoria> categorias = categoriaRepository.findCategoriasComCargosAtivos();
        return categoriaMapper.toResponseList(categorias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> getCategoriasComCargosDisponiveis() {
        log.debug("Buscando categorias com cargos disponíveis");

        List<Categoria> categorias = categoriaRepository.findCategoriasComCargosDisponiveis();
        return categoriaMapper.toResponseList(categorias);
    }

    // === VALIDAÇÕES ===

    @Override
    @Transactional(readOnly = true)
    public boolean existsCategoriaByNome(String nome) {
        return categoriaRepository.existsByNome(nome);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNomeDisponivel(String nome) {
        return !existsCategoriaByNome(nome);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNomeDisponivelParaAtualizacao(String nome, UUID categoriaId) {
        return !categoriaRepository.existsByNomeAndIdNot(nome, categoriaId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteCategoria(UUID id) {
        return categoriaRepository.canDeleteCategoria(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrdemExibicaoDisponivel(Integer ordem) {
        return !categoriaRepository.existsByOrdemExibicao(ordem);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrdemDisponivelParaAtualizacao(Integer ordem, UUID categoriaId) {
        return !categoriaRepository.existsByOrdemExibicaoAndIdNot(ordem, categoriaId);
    }

    // === OPERAÇÕES DE ORDEM ===

    @Override
    @Transactional(readOnly = true)
    public Integer getProximaOrdemExibicao() {
        return categoriaRepository.findNextOrdemExibicao();
    }

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public void reorganizarOrdens() {
        log.info("Reorganizando ordens de exibição das categorias");

        List<Categoria> categorias = categoriaRepository.findAllByOrderByOrdemExibicaoAsc();

        for (int i = 0; i < categorias.size(); i++) {
            Categoria categoria = categorias.get(i);
            categoria.setOrdemExibicao(i + 1);
            categoriaRepository.save(categoria);
        }

        log.info("Reorganização concluída para {} categorias", categorias.size());
    }

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public CategoriaResponse alterarOrdem(UUID id, Integer novaOrdem) {
        log.info("Alterando ordem da categoria ID: {} para ordem: {}", id, novaOrdem);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + id));

        // Verificar se nova ordem está disponível
        if (!isOrdemDisponivelParaAtualizacao(novaOrdem, id)) {
            throw new IllegalArgumentException("Ordem " + novaOrdem + " já está em uso");
        }

        categoria.setOrdemExibicao(novaOrdem);
        Categoria savedCategoria = categoriaRepository.save(categoria);

        return categoriaMapper.toResponse(savedCategoria);
    }

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public CategoriaResponse moverParaCima(UUID id) {
        log.info("Movendo categoria para cima - ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + id));

        Integer ordemAtual = categoria.getOrdemExibicao();
        if (ordemAtual == null || ordemAtual <= 1) {
            throw new IllegalArgumentException("Categoria já está na primeira posição");
        }

        // Trocar com categoria anterior
        Optional<Categoria> categoriaAnterior = categoriaRepository.findByOrdemExibicao(ordemAtual - 1);
        if (categoriaAnterior.isPresent()) {
            categoriaAnterior.get().setOrdemExibicao(ordemAtual);
            categoriaRepository.save(categoriaAnterior.get());
        }

        categoria.setOrdemExibicao(ordemAtual - 1);
        Categoria savedCategoria = categoriaRepository.save(categoria);

        return categoriaMapper.toResponse(savedCategoria);
    }

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public CategoriaResponse moverParaBaixo(UUID id) {
        log.info("Movendo categoria para baixo - ID: {}", id);

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + id));

        Integer ordemAtual = categoria.getOrdemExibicao();
        if (ordemAtual == null) {
            throw new IllegalArgumentException("Categoria não possui ordem definida");
        }

        // Verificar se não é a última
        long totalCategorias = categoriaRepository.count();
        if (ordemAtual >= totalCategorias) {
            throw new IllegalArgumentException("Categoria já está na última posição");
        }

        // Trocar com categoria posterior
        Optional<Categoria> categoriaPosterior = categoriaRepository.findByOrdemExibicao(ordemAtual + 1);
        if (categoriaPosterior.isPresent()) {
            categoriaPosterior.get().setOrdemExibicao(ordemAtual);
            categoriaRepository.save(categoriaPosterior.get());
        }

        categoria.setOrdemExibicao(ordemAtual + 1);
        Categoria savedCategoria = categoriaRepository.save(categoria);

        return categoriaMapper.toResponse(savedCategoria);
    }

    // === ESTATÍSTICAS ===

    @Override
    @Cacheable(value = "categoria-stats-total")
    @Transactional(readOnly = true)
    public long getTotalCategorias() {
        return categoriaRepository.count();
    }

    @Override
    @Cacheable(value = "categoria-stats-ativas")
    @Transactional(readOnly = true)
    public long getTotalCategoriasAtivas() {
        return categoriaRepository.countByAtivo(true);
    }

    @Override
    @Cacheable(value = "categoria-stats-inativas")
    @Transactional(readOnly = true)
    public long getTotalCategoriasInativas() {
        return categoriaRepository.countByAtivo(false);
    }

    @Override
    @Cacheable(value = "categoria-stats-com-cargos")
    @Transactional(readOnly = true)
    public long getTotalCategoriasComCargos() {
        return categoriaRepository.findCategoriasComCargos().size();
    }

    @Override
    @Cacheable(value = "categoria-stats-com-cargos-disponiveis")
    @Transactional(readOnly = true)
    public long getTotalCategoriasComCargosDisponiveis() {
        return categoriaRepository.countCategoriasAtivasComCargosDisponiveis();
    }

    @Override
    @Cacheable(value = "categoria-stats-gerais")
    @Transactional(readOnly = true)
    public Map<String, Object> getEstatisticasGerais() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", getTotalCategorias());
        stats.put("ativas", getTotalCategoriasAtivas());
        stats.put("inativas", getTotalCategoriasInativas());
        stats.put("comCargos", getTotalCategoriasComCargos());
        stats.put("comCargosDisponiveis", getTotalCategoriasComCargosDisponiveis());
        stats.put("percentualAtivas", calculatePercentual(getTotalCategoriasAtivas(), getTotalCategorias()));
        stats.put("percentualComCargos", calculatePercentual(getTotalCategoriasComCargos(), getTotalCategorias()));

        return stats;
    }

    @Override
    @Cacheable(value = "categoria-stats-cargos")
    @Transactional(readOnly = true)
    public Map<String, Map<String, Long>> getEstatisticasCargosPorCategoria() {
        List<Object[]> stats = categoriaRepository.getEstatisticasCategorias();

        return stats.stream().collect(Collectors.toMap(
                row -> (String) row[0], // nome categoria
                row -> {
                    Map<String, Long> categoriaStats = new HashMap<>();
                    categoriaStats.put("total", ((Number) row[1]).longValue());
                    categoriaStats.put("ativos", ((Number) row[2]).longValue());
                    categoriaStats.put("disponiveis", ((Number) row[3]).longValue());
                    return categoriaStats;
                }
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Map<String, Long>> getDistribuicaoHierarquias() {
        List<Object[]> stats = categoriaRepository.getEstatisticasPorHierarquia();

        return stats.stream().collect(Collectors.groupingBy(
                row -> (String) row[0], // nome categoria
                Collectors.toMap(
                        row -> row[1].toString(), // hierarquia
                        row -> ((Number) row[2]).longValue() // count
                )
        ));
    }

    // === RELATÓRIOS ===

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> getCategoriasMaisUtilizadas() {
        log.debug("Buscando categorias mais utilizadas");

        List<Categoria> categorias = categoriaRepository.findCategoriasComMaisCargos();
        return categoriaMapper.toResponseList(categorias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> getCategoriasRecentes() {
        log.debug("Buscando categorias criadas recentemente");

        List<Categoria> categorias = categoriaRepository.findTop10ByOrderByCreatedAtDesc();
        return categoriaMapper.toResponseList(categorias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> getCategoriasNaoRemoviveis() {
        log.debug("Buscando categorias que não podem ser removidas");

        List<Categoria> categorias = categoriaRepository.findCategoriasNaoRemovíveis();
        return categoriaMapper.toResponseList(categorias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRelatorioCompleto() {
        log.debug("Gerando relatório completo das categorias");

        List<Categoria> categorias = categoriaRepository.findAllOrderByPrecedencia();

        return categorias.stream().map(categoria -> {
            Map<String, Object> relatorio = new HashMap<>();
            relatorio.put("id", categoria.getId());
            relatorio.put("nome", categoria.getNome());
            relatorio.put("descricao", categoria.getDescricao());
            relatorio.put("ordem", categoria.getOrdemExibicao());
            relatorio.put("ativo", categoria.isAtiva());
            relatorio.put("totalCargos", categoria.getTotalCargos());
            relatorio.put("cargosAtivos", categoria.getTotalCargosAtivos());
            relatorio.put("cargosDisponiveis", categoria.getTotalCargosDisponiveis());
            relatorio.put("temInformacoesCompletas", categoria.temInformacoesCompletas());
            relatorio.put("podeSerRemovida", categoria.podeSerRemovida());
            relatorio.put("criadaEm", categoria.getCreatedAt());
            relatorio.put("atualizadaEm", categoria.getUpdatedAt());
            return relatorio;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> getCategoriasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        log.debug("Buscando categorias por período: {} - {}", inicio, fim);

        List<Categoria> categorias = categoriaRepository.findCategoriasComCargosNoPeriodo(inicio, fim);
        return categoriaMapper.toResponseList(categorias);
    }

    // === OPERAÇÕES EM LOTE ===

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public List<CategoriaResponse> ativarCategorias(List<UUID> ids) {
        log.info("Ativando múltiplas categorias: {}", ids.size());

        List<Categoria> categorias = categoriaRepository.findAllById(ids);

        categorias.forEach(Categoria::ativar);
        List<Categoria> savedCategorias = categoriaRepository.saveAll(categorias);

        return categoriaMapper.toResponseList(savedCategorias);
    }

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public List<CategoriaResponse> desativarCategorias(List<UUID> ids) {
        log.info("Desativando múltiplas categorias: {}", ids.size());

        List<Categoria> categorias = categoriaRepository.findAllById(ids);

        categorias.forEach(Categoria::desativar);
        List<Categoria> savedCategorias = categoriaRepository.saveAll(categorias);

        return categoriaMapper.toResponseList(savedCategorias);
    }

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public void deleteCategorias(List<UUID> ids) {
        log.info("Removendo múltiplas categorias: {}", ids.size());

        // Validar cada categoria individualmente
        for (UUID id : ids) {
            validarIntegridadeParaRemocao(id);
        }

        categoriaRepository.deleteAllById(ids);
        log.info("Categorias removidas com sucesso: {}", ids.size());
    }

    // === VALIDAÇÃO DE DADOS ===

    @Override
    public void validarDadosCategoria(String nome, String descricao, Integer ordem) {
        if (!Categoria.isNomeValido(nome)) {
            throw new IllegalArgumentException("Nome da categoria inválido. Deve ter entre 3 e 100 caracteres.");
        }

        if (descricao != null && descricao.length() > 1000) {
            throw new IllegalArgumentException("Descrição da categoria deve ter no máximo 1000 caracteres.");
        }

        if (ordem != null && !Categoria.isOrdemValida(ordem)) {
            throw new IllegalArgumentException("Ordem de exibição deve ser positiva.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validarEstadoCategoria(UUID id) {
        if (!categoriaRepository.existsById(id)) {
            throw new IllegalArgumentException("Categoria não encontrada com ID: " + id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validarIntegridadeParaRemocao(UUID id) {
        if (!canDeleteCategoria(id)) {
            throw new IllegalStateException("Categoria não pode ser removida pois possui cargos associados");
        }
    }

    // === OPERAÇÕES ADMINISTRATIVAS ===

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public int limparCategoriasVazias() {
        log.info("Limpando categorias vazias e inativas");

        List<Categoria> categoriasVazias = categoriaRepository.findCategoriasSemCargos()
                .stream()
                .filter(categoria -> !categoria.isAtiva())
                .toList();

        int quantidadeRemovida = categoriasVazias.size();
        categoriaRepository.deleteAll(categoriasVazias);

        log.info("Limpeza concluída: {} categorias removidas", quantidadeRemovida);
        return quantidadeRemovida;
    }

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public void reorganizarTodasAsOrdens() {
        log.info("Reorganizando todas as ordens de exibição");
        reorganizarOrdens();
    }

    @Override
    @CacheEvict(value = "categorias", allEntries = true)
    public void sincronizarEstatisticas() {
        log.info("Sincronizando estatísticas das categorias");
        // Implementação futura se necessário
        log.info("Sincronização de estatísticas concluída");
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> verificarECorrigirInconsistencias() {
        List<String> inconsistencias = new ArrayList<>();

        // Verificar ordens duplicadas
        List<Integer> ordensEmUso = categoriaRepository.findAllOrdensEmUso();
        Set<Integer> ordensUnicas = new HashSet<>(ordensEmUso);
        if (ordensEmUso.size() != ordensUnicas.size()) {
            inconsistencias.add("Ordens de exibição duplicadas detectadas");
        }

        // Verificar gaps na numeração
        for (int i = 1; i < ordensUnicas.size(); i++) {
            if (!ordensUnicas.contains(i)) {
                inconsistencias.add("Gap na numeração de ordens: " + i);
            }
        }

        // Verificar categorias sem nome válido
        List<Categoria> todas = categoriaRepository.findAll();
        long categoriasInvalidas = todas.stream()
                .filter(c -> !Categoria.isNomeValido(c.getNome()))
                .count();
        if (categoriasInvalidas > 0) {
            inconsistencias.add("Categorias com nomes inválidos: " + categoriasInvalidas);
        }

        return inconsistencias;
    }

    // === MÉTODOS AUXILIARES ===

    private double calculatePercentual(long valor, long total) {
        if (total == 0) return 0.0;
        return (double) valor / total * 100;
    }
}