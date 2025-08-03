package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.eleicao.dto.*;
import com.br.ibetelvote.application.mapper.EleicaoMapper;
import com.br.ibetelvote.domain.entities.Eleicao;
import com.br.ibetelvote.domain.services.EleicaoService;
import com.br.ibetelvote.infrastructure.repositories.EleicaoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EleicaoServiceImpl implements EleicaoService {

    private final EleicaoJpaRepository eleicaoRepository;
    private final EleicaoMapper eleicaoMapper;

    // === OPERAÇÕES BÁSICAS ===

    @Override
    @CacheEvict(value = {"eleicoes", "eleicao-stats"}, allEntries = true)
    public EleicaoResponse createEleicao(CreateEleicaoRequest request) {
        log.info("Criando nova eleição: {}", request.getNome());

        // Validar datas
        validarDatas(request.getDataInicio(), request.getDataFim());

        Eleicao eleicao = eleicaoMapper.toEntity(request);
        Eleicao savedEleicao = eleicaoRepository.save(eleicao);

        log.info("Eleição criada com sucesso - ID: {}, Nome: {}", savedEleicao.getId(), savedEleicao.getNome());
        return eleicaoMapper.toResponse(savedEleicao);
    }

    @Override
    @Cacheable(value = "eleicoes", key = "#id")
    @Transactional(readOnly = true)
    public EleicaoResponse getEleicaoById(UUID id) {
        log.debug("Buscando eleição por ID: {}", id);

        Eleicao eleicao = eleicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        return eleicaoMapper.toResponse(eleicao);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EleicaoListResponse> getAllEleicoes(EleicaoFilterRequest filter) {
        log.debug("Listando eleições com filtros: {}", filter);

        Pageable pageable = createPageable(filter);

        Page<Eleicao> eleicoesPage;

        if (hasFilters(filter)) {
            // Aplicar filtros específicos
            if (filter.getNome() != null) {
                eleicoesPage = eleicaoRepository.findByNomeContainingIgnoreCase(filter.getNome(), pageable);
            } else {
                eleicoesPage = eleicaoRepository.findAll(pageable);
            }
        } else {
            eleicoesPage = eleicaoRepository.findAll(pageable);
        }

        return eleicoesPage.map(eleicaoMapper::toListResponse);
    }

    @Override
    @CacheEvict(value = {"eleicoes", "eleicao-stats"}, allEntries = true)
    public EleicaoResponse updateEleicao(UUID id, UpdateEleicaoRequest request) {
        log.info("Atualizando eleição ID: {}", id);

        Eleicao eleicao = eleicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        // Validar se pode ser atualizada
        if (eleicao.isVotacaoAberta()) {
            throw new IllegalStateException("Não é possível atualizar eleição com votação em andamento");
        }

        // Validar datas se foram fornecidas
        if (request.getDataInicio() != null && request.getDataFim() != null) {
            validarDatas(request.getDataInicio(), request.getDataFim());
        }

        eleicaoMapper.updateEntityFromRequest(request, eleicao);
        Eleicao updatedEleicao = eleicaoRepository.save(eleicao);

        log.info("Eleição atualizada com sucesso - ID: {}", updatedEleicao.getId());
        return eleicaoMapper.toResponse(updatedEleicao);
    }

    @Override
    @CacheEvict(value = {"eleicoes", "eleicao-stats"}, allEntries = true)
    public void deleteEleicao(UUID id) {
        log.info("Removendo eleição ID: {}", id);

        Eleicao eleicao = eleicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        // Validar se pode ser removida
        if (eleicao.isVotacaoAberta()) {
            throw new IllegalStateException("Não é possível remover eleição com votação em andamento");
        }

        if (eleicao.getTotalVotosContabilizados() > 0) {
            throw new IllegalStateException("Não é possível remover eleição que já possui votos");
        }

        eleicaoRepository.delete(eleicao);
        log.info("Eleição removida com sucesso - ID: {}", id);
    }

    // === OPERAÇÕES DE CONTROLE ===

    @Override
    @CacheEvict(value = {"eleicoes", "eleicao-stats"}, allEntries = true)
    public void ativarEleicao(UUID id) {
        log.info("Ativando eleição ID: {}", id);

        Eleicao eleicao = eleicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        // Verificar se existe outra eleição ativa
        eleicaoRepository.findEleicaoAtiva().ifPresent(eleicaoAtiva -> {
            if (!eleicaoAtiva.getId().equals(id)) {
                throw new IllegalStateException("Já existe uma eleição ativa. Desative-a antes de ativar outra.");
            }
        });

        eleicao.activate(); // Valida automaticamente se pode ser ativada
        eleicaoRepository.save(eleicao);

        log.info("Eleição ativada com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = {"eleicoes", "eleicao-stats"}, allEntries = true)
    public void desativarEleicao(UUID id) {
        log.info("Desativando eleição ID: {}", id);

        Eleicao eleicao = eleicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        eleicao.deactivate();
        eleicaoRepository.save(eleicao);

        log.info("Eleição desativada com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = {"eleicoes", "eleicao-stats"}, allEntries = true)
    public void encerrarEleicao(UUID id) {
        log.info("Encerrando eleição ID: {}", id);

        Eleicao eleicao = eleicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        if (!eleicao.isAtiva()) {
            throw new IllegalStateException("Só é possível encerrar eleição ativa");
        }

        eleicao.encerrar();
        eleicaoRepository.save(eleicao);

        log.info("Eleição encerrada com sucesso - ID: {}", id);
    }

    // === CONSULTAS ESPECÍFICAS ===

    @Override
    @Cacheable(value = "eleicao-ativa")
    @Transactional(readOnly = true)
    public EleicaoResponse getEleicaoAtiva() {
        log.debug("Buscando eleição ativa");

        Eleicao eleicao = eleicaoRepository.findEleicaoAtiva()
                .orElseThrow(() -> new IllegalArgumentException("Nenhuma eleição ativa encontrada"));

        return eleicaoMapper.toResponse(eleicao);
    }

    @Override
    @Cacheable(value = "eleicoes-abertas")
    @Transactional(readOnly = true)
    public List<EleicaoListResponse> getEleicoesAbertas() {
        log.debug("Buscando eleições abertas");

        List<Eleicao> eleicoes = eleicaoRepository.findEleicoesAbertas();
        return eleicaoMapper.toListResponseList(eleicoes);
    }

    @Override
    @Cacheable(value = "eleicoes-encerradas")
    @Transactional(readOnly = true)
    public List<EleicaoListResponse> getEleicoesEncerradas() {
        log.debug("Buscando eleições encerradas");

        List<Eleicao> eleicoes = eleicaoRepository.findEleicoesEncerradas();
        return eleicaoMapper.toListResponseList(eleicoes);
    }

    @Override
    @Cacheable(value = "eleicoes-futuras")
    @Transactional(readOnly = true)
    public List<EleicaoListResponse> getEleicoesFuturas() {
        log.debug("Buscando eleições futuras");

        List<Eleicao> eleicoes = eleicaoRepository.findEleicoesFuturas();
        return eleicaoMapper.toListResponseList(eleicoes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EleicaoListResponse> getRecentEleicoes(int limit) {
        log.debug("Buscando {} eleições recentes", limit);

        List<Eleicao> eleicoes = eleicaoRepository.findRecentEleicoes(limit);
        return eleicaoMapper.toListResponseList(eleicoes);
    }

    // === VALIDAÇÕES ===

    @Override
    @Transactional(readOnly = true)
    public boolean canActivateEleicao(UUID id) {
        Eleicao eleicao = eleicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        return eleicao.podeSerAtivada();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEleicaoAberta(UUID id) {
        Eleicao eleicao = eleicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        return eleicao.isVotacaoAberta();
    }

    // === ESTATÍSTICAS ===

    @Override
    @Cacheable(value = "eleicao-stats", key = "'total'")
    @Transactional(readOnly = true)
    public long getTotalEleicoes() {
        return eleicaoRepository.count();
    }

    @Override
    @Cacheable(value = "eleicao-stats", key = "'total-ativas'")
    @Transactional(readOnly = true)
    public long getTotalEleicoesAtivas() {
        return eleicaoRepository.countByAtivaTrue();
    }

    @Override
    @Cacheable(value = "eleicao-stats", key = "'total-encerradas'")
    @Transactional(readOnly = true)
    public long getTotalEleicoesEncerradas() {
        return eleicaoRepository.countEleicoesEncerradas();
    }

    @Override
    @Cacheable(value = "eleicao-stats", key = "'total-futuras'")
    @Transactional(readOnly = true)
    public long getTotalEleicoesFuturas() {
        return eleicaoRepository.countEleicoesFuturas();
    }

    // === MÉTODOS AUXILIARES ===

    private void validarDatas(LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Data de início e fim são obrigatórias");
        }

        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        }

        if (dataInicio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Data de início deve ser no futuro");
        }

        // Validar duração mínima (1 hora)
        if (dataInicio.plusHours(1).isAfter(dataFim)) {
            throw new IllegalArgumentException("Eleição deve ter duração mínima de 1 hora");
        }
    }

    private Pageable createPageable(EleicaoFilterRequest filter) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(filter.getDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSort()
        );
        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }

    private boolean hasFilters(EleicaoFilterRequest filter) {
        return filter.getNome() != null ||
                filter.getAtiva() != null ||
                filter.getStatus() != null;
    }
}