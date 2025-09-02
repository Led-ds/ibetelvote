package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.voto.dto.ValidarVotacaoResponse;
import com.br.ibetelvote.application.voto.dto.VotarRequest;
import com.br.ibetelvote.application.voto.dto.VotoFilterRequest;
import com.br.ibetelvote.application.voto.dto.VotoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface VotoService {

    // === OPERAÇÃO PRINCIPAL ===
    List<VotoResponse> votar(UUID membroId, VotarRequest request, String ipOrigem, String userAgent);

    // === CONSULTAS POR MEMBRO ===
    List<VotoResponse> getVotosByMembroId(UUID membroId);
    boolean membroJaVotou(UUID membroId, UUID eleicaoId);
    boolean membroJaVotouNoCargo(UUID membroId, UUID cargoPretendidoId, UUID eleicaoId);

    // === CONSULTAS POR ELEIÇÃO ===
    List<VotoResponse> getVotosByEleicaoId(UUID eleicaoId);
    Page<VotoResponse> getVotosByEleicaoPaginados(UUID eleicaoId, Pageable pageable);
    long getTotalVotosByEleicao(UUID eleicaoId);

    // === CONSULTAS POR CARGO PRETENDIDO ===
    List<VotoResponse> getVotosByCargoPretendidoId(UUID cargoPretendidoId);
    long getTotalVotosByCargoPretendido(UUID cargoPretendidoId);

    // === CONSULTAS POR CANDIDATO ===
    List<VotoResponse> getVotosByCandidatoId(UUID candidatoId);
    long getTotalVotosByCandidato(UUID candidatoId);

    // === RELATÓRIOS E ESTATÍSTICAS ===
    Map<String, Long> getEstatisticasVotacao(UUID eleicaoId);
    Map<String, Long> getEstatisticasPorCargo(UUID cargoPretendidoId);
    List<Map<String, Object>> getResultadosPorCandidato(UUID eleicaoId);
    List<Map<String, Object>> getRankingCandidatosPorCargo(UUID eleicaoId, UUID cargoPretendidoId);
    List<Map<String, Object>> getProgressoVotacaoPorHora(UUID eleicaoId);
    Map<String, Object> getResumoVotacaoDetalhado(UUID eleicaoId);

    // === VALIDAÇÕES MELHORADAS ===
    boolean isEleicaoDisponivelParaVotacao(UUID eleicaoId);
    boolean isMembroElegivelParaVotar(UUID membroId);
    ValidarVotacaoResponse validarVotacaoCompleta(UUID membroId, VotarRequest request);
    List<String> validarVotacao(UUID membroId, VotarRequest request);

    // === AUDITORIA E SEGURANÇA ===
    long getTotalVotosValidos();
    long getTotalVotosBranco();
    long getTotalVotosNulo();
    List<VotoResponse> getVotosParaAuditoria(UUID eleicaoId);
    Map<String, Object> getAnaliseSeguranca(UUID eleicaoId);

    // === MÉTODOS NOVOS ===
    Page<VotoResponse> buscarVotosComFiltros(VotoFilterRequest filtros, Pageable pageable);
    boolean validarIntegridadeVotacao(UUID eleicaoId);
    Map<String, Object> getMetricasTempoReal(UUID eleicaoId);
}