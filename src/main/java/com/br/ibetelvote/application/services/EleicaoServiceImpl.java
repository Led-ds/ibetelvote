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

    @Override
    @CacheEvict(value = {"eleicoes", "eleicao-stats", "eleicao-ativa"}, allEntries = true)
    public EleicaoResponse createEleicao(CreateEleicaoRequest request) {
        log.info("Criando nova eleição: {}", request.getNome());

        // Validar datas
        validarDatas(request.getDataInicio(), request.getDataFim());

        // Verificar conflito de período com eleições ativas
        if (existeEleicaoAtivaNoMesmoPeriodo(request.getDataInicio(), request.getDataFim(), null)) {
            throw new IllegalArgumentException("Já existe uma eleição ativa no mesmo período");
        }

        Eleicao eleicao = eleicaoMapper.toEntity(request);
        eleicao.normalizarNome();
        eleicao.validarDados();

        Eleicao savedEleicao = eleicaoRepository.save(eleicao);

        log.info("Eleição criada com sucesso - ID: {}, Nome: {}", savedEleicao.getId(), savedEleicao.getNome());
        return eleicaoMapper.toResponse(savedEleicao);
    }

    @Override
    @Cacheable(value = "eleicoes", key = "#id")
    @Transactional(readOnly = true)
    public EleicaoResponse getEleicaoById(UUID id) {
        log.debug("Buscando eleição por ID: {}", id);

        Eleicao eleicao = eleicaoRepository.findByIdWithCandidatos(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        return eleicaoMapper.toResponse(eleicao);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EleicaoListResponse> getAllEleicoes(EleicaoFilterRequest filter) {
        log.debug("Listando eleições com filtros: {}", filter);

        Pageable pageable = createPageable(filter);
        Page<Eleicao> eleicoesPage = aplicarFiltros(filter, pageable);

        return eleicoesPage.map(eleicaoMapper::toListResponse);
    }

    @Override
    @CacheEvict(value = {"eleicoes", "eleicao-stats", "eleicao-ativa"}, allEntries = true)
    public EleicaoResponse updateEleicao(UUID id, UpdateEleicaoRequest request) {
        log.info("Atualizando eleição ID: {}", id);

        Eleicao eleicao = eleicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));


        if (eleicao.isVotacaoAberta()) {
            throw new IllegalStateException("Não é possível atualizar eleição com votação em andamento");
        }

        // Validar datas se foram fornecidas
        LocalDateTime novaDataInicio = request.getDataInicio() != null ? request.getDataInicio() : eleicao.getDataInicio();
        LocalDateTime novaDataFim = request.getDataFim() != null ? request.getDataFim() : eleicao.getDataFim();

        if (request.getDataInicio() != null || request.getDataFim() != null) {
            validarDatas(novaDataInicio, novaDataFim);

            // Verificar conflito com outras eleições ativas
            if (existeEleicaoAtivaNoMesmoPeriodo(novaDataInicio, novaDataFim, id)) {
                throw new IllegalArgumentException("Já existe uma eleição ativa no período informado");
            }
        }

        eleicaoMapper.updateEntityFromRequest(request, eleicao);
        Eleicao updatedEleicao = eleicaoRepository.save(eleicao);

        log.info("Eleição atualizada com sucesso - ID: {}", updatedEleicao.getId());
        return eleicaoMapper.toResponse(updatedEleicao);
    }

    @Override
    @CacheEvict(value = {"eleicoes", "eleicao-stats", "eleicao-ativa"}, allEntries = true)
    public void deleteEleicao(UUID id) {
        log.info("Removendo eleição ID: {}", id);

        Eleicao eleicao = eleicaoRepository.findByIdWithCandidatosAndVotos(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        if (eleicao.isVotacaoAberta()) {
            throw new IllegalStateException("Não é possível remover eleição com votação em andamento");
        }

        if (eleicao.getTotalVotosContabilizados() > 0) {
            throw new IllegalStateException("Não é possível remover eleição que já possui votos");
        }

        eleicaoRepository.delete(eleicao);
        log.info("Eleição removida com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = {"eleicoes", "eleicao-stats", "eleicao-ativa"}, allEntries = true)
    public void ativarEleicao(UUID id) {
        log.info("Ativando eleição ID: {}", id);

        Eleicao eleicao = eleicaoRepository.findByIdWithCandidatos(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        eleicaoRepository.findEleicaoAtivaComCandidatos().ifPresent(eleicaoAtiva -> {
            if (!eleicaoAtiva.getId().equals(id)) {
                throw new IllegalStateException("Já existe uma eleição ativa com candidatos. Desative-a antes de ativar outra.");
            }
        });

        // Verificar conflito de período
        if (existeEleicaoAtivaNoMesmoPeriodo(eleicao.getDataInicio(), eleicao.getDataFim(), id)) {
            throw new IllegalStateException("Há conflito de período com outra eleição ativa");
        }

        eleicao.activate();
        eleicaoRepository.save(eleicao);

        log.info("Eleição ativada com sucesso - ID: {}, Total candidatos aprovados: {}",
                id, eleicao.getTotalCandidatosAprovados());
    }

    @Override
    @CacheEvict(value = {"eleicoes", "eleicao-stats", "eleicao-ativa"}, allEntries = true)
    public void desativarEleicao(UUID id) {
        log.info("Desativando eleição ID: {}", id);

        Eleicao eleicao = eleicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        eleicao.deactivate();
        eleicaoRepository.save(eleicao);

        log.info("Eleição desativada com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = {"eleicoes", "eleicao-stats", "eleicao-ativa"}, allEntries = true)
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

    @Override
    @Cacheable(value = "eleicao-ativa")
    @Transactional(readOnly = true)
    public EleicaoResponse getEleicaoAtiva() {
        log.debug("Buscando eleição ativa");

        Eleicao eleicao = eleicaoRepository.findEleicaoAtivaComCandidatos()
                .orElseThrow(() -> new IllegalArgumentException("Nenhuma eleição ativa encontrada"));

        return eleicaoMapper.toResponse(eleicao);
    }

    @Override
    @Cacheable(value = "eleicoes-abertas")
    @Transactional(readOnly = true)
    public List<EleicaoListResponse> getEleicoesAbertas() {
        log.debug("Buscando eleições abertas");

        List<Eleicao> eleicoes = eleicaoRepository.findEleicoesAbertasComCandidatos();
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

    @Override
    @Transactional(readOnly = true)
    public List<EleicaoListResponse> getEleicoesComCandidatosAprovados() {
        log.debug("Buscando eleições com candidatos aprovados");

        List<Eleicao> eleicoes = eleicaoRepository.findEleicoesComCandidatosAprovados();
        return eleicaoMapper.toListResponseList(eleicoes);
    }


    @Override
    @Transactional(readOnly = true)
    public boolean canActivateEleicao(UUID id) {
        Eleicao eleicao = eleicaoRepository.findByIdWithCandidatos(id)
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

    @Override
    @Transactional(readOnly = true)
    public EleicaoValidacaoResponse validarEleicaoParaAtivacao(UUID id) {
        log.debug("Validando eleição para ativação - ID: {}", id);

        Eleicao eleicao = eleicaoRepository.findByIdWithCandidatos(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        return eleicaoMapper.toValidacaoResponse(eleicao);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEleicaoAtivaNoMesmoPeriodo(LocalDateTime inicio, LocalDateTime fim, UUID excludeId) {
        return eleicaoRepository.existeEleicaoAtivaNoMesmoPeriodo(inicio, fim, excludeId);
    }


    @Override
    @CacheEvict(value = {"eleicoes", "eleicao-stats"}, allEntries = true)
    public EleicaoResponse updateConfiguracoes(UUID id, EleicaoConfigRequest request) {
        log.info("Atualizando configurações da eleição ID: {}", id);

        Eleicao eleicao = eleicaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        if (eleicao.isVotacaoAberta()) {
            throw new IllegalStateException("Não é possível alterar configurações durante a votação");
        }

        eleicao.updateConfiguracoes(
                request.getPermiteVotoBranco(),
                request.getPermiteVotoNulo(),
                request.getExibeResultadosParciais(),
                request.getTotalElegiveis()
        );

        Eleicao updatedEleicao = eleicaoRepository.save(eleicao);
        log.info("Configurações atualizadas com sucesso - ID: {}", id);

        return eleicaoMapper.toResponse(updatedEleicao);
    }


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

    @Override
    @Cacheable(value = "eleicao-stats", key = "#id")
    @Transactional(readOnly = true)
    public EleicaoStatsResponse getEstatisticasEleicao(UUID id) {
        log.debug("Buscando estatísticas da eleição ID: {}", id);

        Eleicao eleicao = eleicaoRepository.findByIdWithCandidatosAndVotos(id)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + id));

        return eleicaoMapper.toStatsResponse(eleicao);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EleicaoListResponse> buscarEleicoesComFiltros(EleicaoFilterRequest filter) {
        log.debug("Buscando eleições com filtros avançados: {}", filter);

        // Implementar busca avançada baseada nos filtros
        Pageable pageable = createPageable(filter);
        Page<Eleicao> eleicoes = aplicarFiltros(filter, pageable);

        return eleicaoMapper.toListResponseList(eleicoes.getContent());
    }

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

    private Page<Eleicao> aplicarFiltros(EleicaoFilterRequest filter, Pageable pageable) {
        if (hasFilters(filter)) {
            if (filter.getNome() != null) {
                return eleicaoRepository.findByNomeContainingIgnoreCase(filter.getNome(), pageable);
            }
            // Implementar outros filtros conforme necessário
            return eleicaoRepository.findAll(pageable);
        } else {
            return eleicaoRepository.findAll(pageable);
        }
    }

    private boolean hasFilters(EleicaoFilterRequest filter) {
        return filter.getNome() != null ||
                filter.getAtiva() != null ||
                filter.getStatus() != null ||
                filter.getTemCandidatos() != null ||
                filter.getTemCandidatosAprovados() != null;
    }
}