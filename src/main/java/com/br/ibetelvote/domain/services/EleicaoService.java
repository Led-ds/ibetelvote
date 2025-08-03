package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.eleicao.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface EleicaoService {

    // === OPERAÇÕES BÁSICAS ===
    EleicaoResponse createEleicao(CreateEleicaoRequest request);
    EleicaoResponse getEleicaoById(UUID id);
    Page<EleicaoListResponse> getAllEleicoes(EleicaoFilterRequest filter);
    EleicaoResponse updateEleicao(UUID id, UpdateEleicaoRequest request);
    void deleteEleicao(UUID id);

    // === OPERAÇÕES DE CONTROLE ===
    void ativarEleicao(UUID id);
    void desativarEleicao(UUID id);
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

    // === ESTATÍSTICAS ===
    long getTotalEleicoes();
    long getTotalEleicoesAtivas();
    long getTotalEleicoesEncerradas();
    long getTotalEleicoesFuturas();
}