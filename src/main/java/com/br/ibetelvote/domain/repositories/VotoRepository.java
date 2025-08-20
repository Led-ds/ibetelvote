package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Voto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VotoRepository {

    void deleteById(UUID id);
    long count();

    // === CONSULTAS POR ELEIÇÃO ===
    List<Voto> findByEleicaoId(UUID eleicaoId);
    Page<Voto> findByEleicaoId(UUID eleicaoId, Pageable pageable);
    long countByEleicaoId(UUID eleicaoId);
    List<Voto> findByEleicaoIdOrderByDataVotoAsc(UUID eleicaoId);

    // === CONSULTAS POR CARGO PRETENDIDO ===
    List<Voto> findByCargoPretendidoId(UUID cargoPretendidoId);
    Page<Voto> findByCargoPretendidoId(UUID cargoPretendidoId, Pageable pageable);
    long countByCargoPretendidoId(UUID cargoPretendidoId);
    List<Voto> findByEleicaoIdAndCargoPretendidoId(UUID eleicaoId, UUID cargoPretendidoId);

    // === CONSULTAS POR CANDIDATO ===
    List<Voto> findByCandidatoId(UUID candidatoId);
    Page<Voto> findByCandidatoId(UUID candidatoId, Pageable pageable);
    long countByCandidatoId(UUID candidatoId);
    List<Voto> findByCandidatoIdOrderByDataVotoAsc(UUID candidatoId);

    // === CONSULTAS POR MEMBRO ===
    List<Voto> findByMembroId(UUID membroId);
    Page<Voto> findByMembroId(UUID membroId, Pageable pageable);
    long countByMembroId(UUID membroId);
    List<Voto> findByEleicaoIdAndMembroId(UUID eleicaoId, UUID membroId);

    // === VALIDAÇÕES DE VOTO ÚNICO ===
    Optional<Voto> findByMembroIdAndCargoPretendidoIdAndEleicaoId(UUID membroId, UUID cargoPretendidoId, UUID eleicaoId);
    boolean existsByMembroIdAndCargoPretendidoIdAndEleicaoId(UUID membroId, UUID cargoPretendidoId, UUID eleicaoId);
    boolean existsByMembroIdAndEleicaoId(UUID membroId, UUID eleicaoId);

    // === CONSULTAS POR TIPO DE VOTO ===
    List<Voto> findByVotoBrancoTrue();
    List<Voto> findByVotoNuloTrue();
    List<Voto> findVotosValidos();
    List<Voto> findByEleicaoIdAndVotoBrancoTrue(UUID eleicaoId);
    List<Voto> findByEleicaoIdAndVotoNuloTrue(UUID eleicaoId);
    List<Voto> findVotosValidosByEleicao(UUID eleicaoId);

    // === CONSULTAS POR CARGO E TIPO ===
    List<Voto> findByCargoPretendidoIdAndVotoBrancoTrue(UUID cargoPretendidoId);
    List<Voto> findByCargoPretendidoIdAndVotoNuloTrue(UUID cargoPretendidoId);
    List<Voto> findVotosValidosByCargoPretendido(UUID cargoPretendidoId);

    // === CONSULTAS POR PERÍODO ===
    List<Voto> findByDataVotoBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Voto> findByEleicaoIdAndDataVotoBetween(UUID eleicaoId, LocalDateTime inicio, LocalDateTime fim);
    Page<Voto> findByDataVotoBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    // === ESTATÍSTICAS GERAIS ===
    long countByVotoBrancoTrue();
    long countByVotoNuloTrue();
    long countVotosValidos();

    // === ESTATÍSTICAS POR ELEIÇÃO ===
    long countByEleicaoIdAndVotoBrancoTrue(UUID eleicaoId);
    long countByEleicaoIdAndVotoNuloTrue(UUID eleicaoId);
    long countVotosValidosByEleicao(UUID eleicaoId);

    // === ESTATÍSTICAS POR CARGO PRETENDIDO ===
    long countByCargoPretendidoIdAndVotoBrancoTrue(UUID cargoPretendidoId);
    long countByCargoPretendidoIdAndVotoNuloTrue(UUID cargoPretendidoId);
    long countVotosValidosByCargoPretendido(UUID cargoPretendidoId);

    // === RELATÓRIOS E CONSULTAS CUSTOMIZADAS ===

    /**
     * Conta votos por candidato em uma eleição específica
     */
    List<Object[]> countVotosByCandidatoAndCargoPretendido(UUID eleicaoId);

    /**
     * Conta votos por hora em uma eleição
     */
    List<Object[]> countVotosByHora(UUID eleicaoId);

    /**
     * Busca ranking de candidatos por número de votos
     */
    List<Object[]> findRankingCandidatosPorVotos(UUID eleicaoId, UUID cargoPretendidoId);

    /**
     * Busca distribuição de votos por tipo
     */
    List<Object[]> findDistribuicaoVotosPorTipo(UUID eleicaoId);

    /**
     * Busca progresso da votação ao longo do tempo
     */
    List<Object[]> findProgressoVotacao(UUID eleicaoId);

    /**
     * Conta votos únicos por membro em uma eleição
     */
    long countVotantesUnicosByEleicao(UUID eleicaoId);

    /**
     * Busca últimos votos registrados
     */
    List<Voto> findUltimosVotosRegistrados(int limite);

    /**
     * Busca votos por range de hash (para auditoria)
     */
    List<Voto> findByHashVotoContaining(String hashFragment);

    /**
     * Busca votos com dados incompletos (para limpeza)
     */
    List<Voto> findVotosComDadosIncompletos();

    /**
     * Busca votos por IP de origem (para análise de segurança)
     */
    List<Voto> findByIpOrigemContaining(String ipPattern);

    // === CONSULTAS PARA AUDITORIA ===

    /**
     * Busca votos para auditoria (sem dados sensíveis)
     */
    List<Voto> findVotosParaAuditoria(UUID eleicaoId);

    /**
     * Conta total de votantes distintos por eleição
     */
    long countDistinctMembroByEleicaoId(UUID eleicaoId);

    /**
     * Verifica integridade dos votos por hash
     */
    List<Voto> findVotosComHashDuplicado();

    /**
     * Busca votos suspeitos (mesmo IP, user agent, etc.)
     */
    List<Object[]> findVotosSuspeitos(UUID eleicaoId);

    // === CONSULTAS ESPECÍFICAS PARA RELATÓRIOS ===

    /**
     * Busca resumo de votação por cargo pretendido
     */
    List<Object[]> getResumoVotacaoPorCargo(UUID eleicaoId);

    /**
     * Busca participação por cargo atual do membro
     */
    List<Object[]> getParticipacaoPorCargoMembro(UUID eleicaoId);

    /**
     * Busca tendências de votação por período
     */
    List<Object[]> getTendenciasVotacaoPorPeriodo(UUID eleicaoId, LocalDateTime inicio, LocalDateTime fim);

    // === MÉTODOS DE VALIDAÇÃO E SEGURANÇA ===

    /**
     * Verifica se existe voto duplicado para o mesmo candidato
     */
    boolean existsVotoDuplicado(UUID membroId, UUID candidatoId, UUID eleicaoId);

    /**
     * Conta votos por IP para detectar possíveis irregularidades
     */
    List<Object[]> countVotosPorIpOrigem(UUID eleicaoId);

    /**
     * Busca votos registrados muito próximos no tempo (possível automação)
     */
    List<Voto> findVotosSequenciaisRapidos(UUID eleicaoId, int segundosIntervalo);

    // === COMPATIBILIDADE TEMPORÁRIA ===

    /**
     * @deprecated Usar findByCargoPretendidoId
     */
    @Deprecated
    List<Voto> findByCargoId(UUID cargoId);

    /**
     * @deprecated Usar countByCargoPretendidoId
     */
    @Deprecated
    long countByCargoId(UUID cargoId);

    /**
     * @deprecated Usar findByEleicaoIdAndCargoPretendidoId
     */
    @Deprecated
    List<Voto> findByEleicaoIdAndCargoId(UUID eleicaoId, UUID cargoId);
}