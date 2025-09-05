package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.eleicao.dto.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EleicaoService {

    // === OPERAÇÕES BÁSICAS ===
    EleicaoResponse createEleicao(CreateEleicaoRequest request);
    EleicaoResponse getEleicaoById(UUID id);
    Page<EleicaoListResponse> getAllEleicoes(EleicaoFilterRequest filter);
    EleicaoResponse updateEleicao(UUID id, UpdateEleicaoRequest request);
    void deleteEleicao(UUID id);

    EleicaoResponse buscarDetalhada(UUID eleicaoId);

    // === OPERAÇÕES DE CONTROLE ===
    EleicaoResponse  ativarEleicao(UUID id);
    EleicaoResponse  desativarEleicao(UUID id);
    void encerrarEleicao(UUID id);

    // === CONSULTAS ESPECÍFICAS ===
    EleicaoResponse getEleicaoAtiva();
    List<EleicaoListResponse> getEleicoesAbertas();
    List<EleicaoListResponse> getEleicoesEncerradas();
    List<EleicaoListResponse> getEleicoesFuturas();
    List<EleicaoListResponse> getRecentEleicoes(int limit);

    // === VALIDAÇÕES ===
    boolean canActivateEleicao(UUID id);
    boolean isEleicaoAberta(UUID id);
    EleicaoValidacaoResponse validarEleicaoParaAtivacao(UUID id);

    // === CONFIGURAÇÕES ===
    EleicaoResponse updateConfiguracoes(UUID id, EleicaoConfigRequest request);

    // === ESTATÍSTICAS ===
    long getTotalEleicoes();
    long getTotalEleicoesAtivas();
    long getTotalEleicoesEncerradas();
    long getTotalEleicoesFuturas();
    EleicaoStatsResponse getEstatisticasEleicao(UUID id);

    // === CONSULTAS AVANÇADAS ===
    List<EleicaoListResponse> buscarEleicoesComFiltros(EleicaoFilterRequest filter);
    List<EleicaoListResponse> getEleicoesComCandidatosAprovados();
    boolean existeEleicaoAtivaNoMesmoPeriodo(LocalDateTime inicio, LocalDateTime fim, UUID excludeId);
}