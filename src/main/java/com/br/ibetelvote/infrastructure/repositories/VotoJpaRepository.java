package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Voto;
import com.br.ibetelvote.domain.repositories.VotoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VotoJpaRepository extends JpaRepository<Voto, UUID>, VotoRepository {

    // === OPERAÇÕES BÁSICAS ===
    // Métodos básicos herdados do JpaRepository

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
    List<Voto> findByEleicaoIdAndVotoBrancoTrue(UUID eleicaoId);
    List<Voto> findByEleicaoIdAndVotoNuloTrue(UUID eleicaoId);

    // === CONSULTAS POR CARGO E TIPO ===
    List<Voto> findByCargoPretendidoIdAndVotoBrancoTrue(UUID cargoPretendidoId);
    List<Voto> findByCargoPretendidoIdAndVotoNuloTrue(UUID cargoPretendidoId);

    // === CONSULTAS POR PERÍODO ===
    List<Voto> findByDataVotoBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Voto> findByEleicaoIdAndDataVotoBetween(UUID eleicaoId, LocalDateTime inicio, LocalDateTime fim);
    Page<Voto> findByDataVotoBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    // === ESTATÍSTICAS GERAIS ===
    long countByVotoBrancoTrue();
    long countByVotoNuloTrue();

    // === ESTATÍSTICAS POR ELEIÇÃO ===
    long countByEleicaoIdAndVotoBrancoTrue(UUID eleicaoId);
    long countByEleicaoIdAndVotoNuloTrue(UUID eleicaoId);

    // === ESTATÍSTICAS POR CARGO PRETENDIDO ===
    long countByCargoPretendidoIdAndVotoBrancoTrue(UUID cargoPretendidoId);
    long countByCargoPretendidoIdAndVotoNuloTrue(UUID cargoPretendidoId);

    // === CONSULTAS CUSTOMIZADAS COM @Query ===

    /**
     * Busca votos válidos (têm candidato e não são branco/nulo)
     */
    @Query("SELECT v FROM Voto v WHERE v.candidatoId IS NOT NULL AND v.votoBranco = false AND v.votoNulo = false")
    List<Voto> findVotosValidos();

    /**
     * Busca votos válidos por eleição
     */
    @Query("SELECT v FROM Voto v WHERE v.eleicaoId = :eleicaoId AND v.candidatoId IS NOT NULL AND v.votoBranco = false AND v.votoNulo = false")
    List<Voto> findVotosValidosByEleicao(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca votos válidos por cargo pretendido
     */
    @Query("SELECT v FROM Voto v WHERE v.cargoPretendidoId = :cargoPretendidoId AND v.candidatoId IS NOT NULL AND v.votoBranco = false AND v.votoNulo = false")
    List<Voto> findVotosValidosByCargoPretendido(@Param("cargoPretendidoId") UUID cargoPretendidoId);

    /**
     * Conta votos válidos
     */
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.candidatoId IS NOT NULL AND v.votoBranco = false AND v.votoNulo = false")
    long countVotosValidos();

    /**
     * Conta votos válidos por eleição
     */
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.eleicaoId = :eleicaoId AND v.candidatoId IS NOT NULL AND v.votoBranco = false AND v.votoNulo = false")
    long countVotosValidosByEleicao(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Conta votos válidos por cargo pretendido
     */
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.cargoPretendidoId = :cargoPretendidoId AND v.candidatoId IS NOT NULL AND v.votoBranco = false AND v.votoNulo = false")
    long countVotosValidosByCargoPretendido(@Param("cargoPretendidoId") UUID cargoPretendidoId);

    // === RELATÓRIOS E CONSULTAS CUSTOMIZADAS ===

    /**
     * Conta votos por candidato em uma eleição específica
     */
    @Query("SELECT c.nomeCandidato, cp.nome, COUNT(v) FROM Voto v " +
            "JOIN Candidato c ON v.candidatoId = c.id " +
            "JOIN Cargo cp ON v.cargoPretendidoId = cp.id " +
            "WHERE v.eleicaoId = :eleicaoId AND v.candidatoId IS NOT NULL " +
            "GROUP BY c.id, c.nomeCandidato, cp.id, cp.nome " +
            "ORDER BY cp.nome, COUNT(v) DESC")
    List<Object[]> countVotosByCandidatoAndCargoPretendido(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Conta votos por hora em uma eleição
     */
    @Query("SELECT HOUR(v.dataVoto), COUNT(v) FROM Voto v " +
            "WHERE v.eleicaoId = :eleicaoId " +
            "GROUP BY HOUR(v.dataVoto) " +
            "ORDER BY HOUR(v.dataVoto)")
    List<Object[]> countVotosByHora(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca ranking de candidatos por número de votos
     */
    @Query("SELECT c.nomeCandidato, c.numeroCandidato, COUNT(v) as totalVotos FROM Voto v " +
            "JOIN Candidato c ON v.candidatoId = c.id " +
            "WHERE v.eleicaoId = :eleicaoId AND v.cargoPretendidoId = :cargoPretendidoId AND v.candidatoId IS NOT NULL " +
            "GROUP BY c.id, c.nomeCandidato, c.numeroCandidato " +
            "ORDER BY totalVotos DESC, c.nomeCandidato ASC")
    List<Object[]> findRankingCandidatosPorVotos(@Param("eleicaoId") UUID eleicaoId,
                                                 @Param("cargoPretendidoId") UUID cargoPretendidoId);

    /**
     * Busca distribuição de votos por tipo
     */
    @Query("SELECT " +
            "SUM(CASE WHEN v.candidatoId IS NOT NULL AND v.votoBranco = false AND v.votoNulo = false THEN 1 ELSE 0 END) as votosValidos, " +
            "SUM(CASE WHEN v.votoBranco = true THEN 1 ELSE 0 END) as votosBranco, " +
            "SUM(CASE WHEN v.votoNulo = true THEN 1 ELSE 0 END) as votosNulo, " +
            "COUNT(v) as totalVotos " +
            "FROM Voto v WHERE v.eleicaoId = :eleicaoId")
    List<Object[]> findDistribuicaoVotosPorTipo(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca progresso da votação ao longo do tempo
     */
    @Query("SELECT DATE(v.dataVoto), HOUR(v.dataVoto), COUNT(v) FROM Voto v " +
            "WHERE v.eleicaoId = :eleicaoId " +
            "GROUP BY DATE(v.dataVoto), HOUR(v.dataVoto) " +
            "ORDER BY DATE(v.dataVoto), HOUR(v.dataVoto)")
    List<Object[]> findProgressoVotacao(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Conta votantes únicos por eleição
     */
    @Query("SELECT COUNT(DISTINCT v.membroId) FROM Voto v WHERE v.eleicaoId = :eleicaoId")
    long countVotantesUnicosByEleicao(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca últimos votos registrados
     */
    @Query("SELECT v FROM Voto v ORDER BY v.dataVoto DESC")
    List<Voto> findUltimosVotosRegistrados(@Param("limite") int limite);

    /**
     * Busca votos por range de hash (para auditoria)
     */
    @Query("SELECT v FROM Voto v WHERE v.hashVoto LIKE %:hashFragment%")
    List<Voto> findByHashVotoContaining(@Param("hashFragment") String hashFragment);

    /**
     * Busca votos com dados incompletos (para limpeza)
     */
    @Query("SELECT v FROM Voto v WHERE v.hashVoto IS NULL OR v.ipOrigem IS NULL OR v.userAgent IS NULL")
    List<Voto> findVotosComDadosIncompletos();

    /**
     * Busca votos por IP de origem (para análise de segurança)
     */
    @Query("SELECT v FROM Voto v WHERE v.ipOrigem LIKE %:ipPattern%")
    List<Voto> findByIpOrigemContaining(@Param("ipPattern") String ipPattern);

    // === CONSULTAS PARA AUDITORIA ===

    /**
     * Busca votos para auditoria (sem dados sensíveis)
     */
    @Query("SELECT v FROM Voto v WHERE v.eleicaoId = :eleicaoId ORDER BY v.dataVoto")
    List<Voto> findVotosParaAuditoria(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Conta total de votantes distintos por eleição
     */
    @Query("SELECT COUNT(DISTINCT v.membroId) FROM Voto v WHERE v.eleicaoId = :eleicaoId")
    long countDistinctMembroByEleicaoId(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Verifica integridade dos votos por hash
     */
    @Query("SELECT v FROM Voto v WHERE v.hashVoto IN (" +
            "SELECT v2.hashVoto FROM Voto v2 GROUP BY v2.hashVoto HAVING COUNT(v2.hashVoto) > 1)")
    List<Voto> findVotosComHashDuplicado();

    /**
     * Busca votos suspeitos (mesmo IP, user agent, etc.)
     */
    @Query("SELECT v.ipOrigem, v.userAgent, COUNT(v) FROM Voto v " +
            "WHERE v.eleicaoId = :eleicaoId " +
            "GROUP BY v.ipOrigem, v.userAgent " +
            "HAVING COUNT(v) > 5 " +
            "ORDER BY COUNT(v) DESC")
    List<Object[]> findVotosSuspeitos(@Param("eleicaoId") UUID eleicaoId);

    // === CONSULTAS ESPECÍFICAS PARA RELATÓRIOS ===

    /**
     * Busca resumo de votação por cargo pretendido
     */
    @Query("SELECT cp.nome, " +
            "COUNT(v) as totalVotos, " +
            "SUM(CASE WHEN v.candidatoId IS NOT NULL AND v.votoBranco = false AND v.votoNulo = false THEN 1 ELSE 0 END) as votosValidos, " +
            "SUM(CASE WHEN v.votoBranco = true THEN 1 ELSE 0 END) as votosBranco, " +
            "SUM(CASE WHEN v.votoNulo = true THEN 1 ELSE 0 END) as votosNulo " +
            "FROM Voto v JOIN Cargo cp ON v.cargoPretendidoId = cp.id " +
            "WHERE v.eleicaoId = :eleicaoId " +
            "GROUP BY cp.id, cp.nome " +
            "ORDER BY cp.nome")
    List<Object[]> getResumoVotacaoPorCargo(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca participação por cargo atual do membro
     */
    @Query("SELECT m.nomeCargoAtual, COUNT(DISTINCT v.membroId) " +
            "FROM Voto v JOIN Membro m ON v.membroId = m.id " +
            "WHERE v.eleicaoId = :eleicaoId " +
            "GROUP BY m.cargoAtualId, m.nomeCargoAtual " +
            "ORDER BY m.nomeCargoAtual")
    List<Object[]> getParticipacaoPorCargoMembro(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca tendências de votação por período
     */
    @Query("SELECT DATE(v.dataVoto), COUNT(v) FROM Voto v " +
            "WHERE v.eleicaoId = :eleicaoId AND v.dataVoto BETWEEN :inicio AND :fim " +
            "GROUP BY DATE(v.dataVoto) " +
            "ORDER BY DATE(v.dataVoto)")
    List<Object[]> getTendenciasVotacaoPorPeriodo(@Param("eleicaoId") UUID eleicaoId,
                                                  @Param("inicio") LocalDateTime inicio,
                                                  @Param("fim") LocalDateTime fim);

    // === MÉTODOS DE VALIDAÇÃO E SEGURANÇA ===

    /**
     * Verifica se existe voto duplicado para o mesmo candidato
     */
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Voto v " +
            "WHERE v.membroId = :membroId AND v.candidatoId = :candidatoId AND v.eleicaoId = :eleicaoId")
    boolean existsVotoDuplicado(@Param("membroId") UUID membroId,
                                @Param("candidatoId") UUID candidatoId,
                                @Param("eleicaoId") UUID eleicaoId);

    /**
     * Conta votos por IP para detectar possíveis irregularidades
     */
    @Query("SELECT v.ipOrigem, COUNT(v) FROM Voto v " +
            "WHERE v.eleicaoId = :eleicaoId " +
            "GROUP BY v.ipOrigem " +
            "HAVING COUNT(v) > 3 " +
            "ORDER BY COUNT(v) DESC")
    List<Object[]> countVotosPorIpOrigem(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca votos registrados muito próximos no tempo (possível automação)
     */
    @Query("SELECT v FROM Voto v WHERE v.eleicaoId = :eleicaoId AND " +
            "EXISTS (SELECT v2 FROM Voto v2 WHERE v2.membroId = v.membroId AND v2.id != v.id AND " +
            "ABS(TIMESTAMPDIFF(SECOND, v.dataVoto, v2.dataVoto)) <= :segundosIntervalo)")
    List<Voto> findVotosSequenciaisRapidos(@Param("eleicaoId") UUID eleicaoId,
                                           @Param("segundosIntervalo") int segundosIntervalo);

    // === COMPATIBILIDADE TEMPORÁRIA ===

    /**
     * @deprecated Usar findByCargoPretendidoId
     */
    @Deprecated
    @Query("SELECT v FROM Voto v WHERE v.cargoPretendidoId = :cargoId")
    List<Voto> findByCargoId(@Param("cargoId") UUID cargoId);

    /**
     * @deprecated Usar countByCargoPretendidoId
     */
    @Deprecated
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.cargoPretendidoId = :cargoId")
    long countByCargoId(@Param("cargoId") UUID cargoId);

    /**
     * @deprecated Usar findByEleicaoIdAndCargoPretendidoId
     */
    @Deprecated
    @Query("SELECT v FROM Voto v WHERE v.eleicaoId = :eleicaoId AND v.cargoPretendidoId = :cargoId")
    List<Voto> findByEleicaoIdAndCargoId(@Param("eleicaoId") UUID eleicaoId, @Param("cargoId") UUID cargoId);
}