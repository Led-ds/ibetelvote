package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.eleicao.dto.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface VotoService {

    // === OPERAÇÃO PRINCIPAL ===
    List<VotoResponse> votar(UUID membroId, VotarRequest request, String ipOrigem, String userAgent);

    // === CONSULTAS POR MEMBRO ===
    List<VotoResponse> getVotosByMembroId(UUID membroId);
    boolean membroJaVotou(UUID membroId, UUID eleicaoId);
    boolean membroJaVotouNoCargo(UUID membroId, UUID cargoId, UUID eleicaoId);

    // === CONSULTAS POR ELEIÇÃO ===
    List<VotoResponse> getVotosByEleicaoId(UUID eleicaoId);
    long getTotalVotosByEleicao(UUID eleicaoId);

    // === CONSULTAS POR CARGO ===
    List<VotoResponse> getVotosByCargoId(UUID cargoId);
    long getTotalVotosByCargo(UUID cargoId);

    // === CONSULTAS POR CANDIDATO ===
    List<VotoResponse> getVotosByCandidatoId(UUID candidatoId);
    long getTotalVotosByCandidato(UUID candidatoId);

    // === RELATÓRIOS E ESTATÍSTICAS ===
    Map<String, Long> getEstatisticasVotacao(UUID eleicaoId);
    Map<String, Long> getEstatisticasPorCargo(UUID cargoId);
    List<Map<String, Object>> getResultadosPorCandidato(UUID eleicaoId);
    List<Map<String, Object>> getProgressoVotacaoPorHora(UUID eleicaoId);

    // === VALIDAÇÕES ===
    boolean isEleicaoDisponivelParaVotacao(UUID eleicaoId);
    boolean isMembroElegivelParaVotar(UUID membroId);
    List<String> validarVotacao(UUID membroId, VotarRequest request);

    // === AUDITORIA ===
    long getTotalVotosValidos();
    long getTotalVotosBranco();
    long getTotalVotosNulo();
    List<VotoResponse> getVotosParaAuditoria(UUID eleicaoId); // Sem dados sensíveis
}