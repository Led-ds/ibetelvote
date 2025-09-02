package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.application.voto.dto.VotoFilterRequest;
import com.br.ibetelvote.domain.entities.Voto;
import com.br.ibetelvote.domain.entities.enums.TipoVoto;
import com.br.ibetelvote.domain.repositories.VotoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VotoJpaRepository extends JpaRepository<Voto, UUID>,
        VotoRepository,
        JpaSpecificationExecutor<Voto> {

    // === OPERAÇÕES BÁSICAS OTIMIZADAS ===

    /**
     * Busca votos por eleição com fetch join das entidades relacionadas
     */
    @Query("SELECT v FROM Voto v " +
            "LEFT JOIN FETCH v.membro m " +
            "LEFT JOIN FETCH v.eleicao e " +
            "LEFT JOIN FETCH v.cargoPretendido cp " +
            "LEFT JOIN FETCH v.candidato c " +
            "WHERE v.eleicao.id = :eleicaoId " +
            "ORDER BY v.dataVoto DESC")
    List<Voto> findByEleicaoIdWithEntities(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca votos por eleição com paginação e fetch join
     */
    @Query(value = "SELECT v FROM Voto v " +
            "LEFT JOIN FETCH v.membro m " +
            "LEFT JOIN FETCH v.eleicao e " +
            "LEFT JOIN FETCH v.cargoPretendido cp " +
            "LEFT JOIN FETCH v.candidato c " +
            "WHERE v.eleicao.id = :eleicaoId",
            countQuery = "SELECT COUNT(v) FROM Voto v WHERE v.eleicao.id = :eleicaoId")
    Page<Voto> findByEleicaoIdWithEntities(@Param("eleicaoId") UUID eleicaoId, Pageable pageable);

    /**
     * Busca votos por membro com fetch join
     */
    @Query("SELECT v FROM Voto v " +
            "LEFT JOIN FETCH v.membro m " +
            "LEFT JOIN FETCH v.eleicao e " +
            "LEFT JOIN FETCH v.cargoPretendido cp " +
            "LEFT JOIN FETCH v.candidato c " +
            "WHERE v.membro.id = :membroId " +
            "ORDER BY v.dataVoto DESC")
    List<Voto> findByMembroIdWithEntities(@Param("membroId") UUID membroId);

    /**
     * Busca votos por cargo pretendido com fetch join
     */
    @Query("SELECT v FROM Voto v " +
            "LEFT JOIN FETCH v.membro m " +
            "LEFT JOIN FETCH v.eleicao e " +
            "LEFT JOIN FETCH v.cargoPretendido cp " +
            "LEFT JOIN FETCH v.candidato c " +
            "WHERE v.cargoPretendido.id = :cargoPretendidoId " +
            "ORDER BY v.dataVoto DESC")
    List<Voto> findByCargoPretendidoIdWithEntities(@Param("cargoPretendidoId") UUID cargoPretendidoId);

    /**
     * Busca votos por candidato com fetch join
     */
    @Query("SELECT v FROM Voto v " +
            "LEFT JOIN FETCH v.membro m " +
            "LEFT JOIN FETCH v.eleicao e " +
            "LEFT JOIN FETCH v.cargoPretendido cp " +
            "LEFT JOIN FETCH v.candidato c " +
            "WHERE v.candidato.id = :candidatoId " +
            "ORDER BY v.dataVoto DESC")
    List<Voto> findByCandidatoIdWithEntities(@Param("candidatoId") UUID candidatoId);

    /**
     * Busca votos para auditoria (sem dados sensíveis) com fetch join
     */
    @Query("SELECT v FROM Voto v " +
            "LEFT JOIN FETCH v.eleicao e " +
            "LEFT JOIN FETCH v.cargoPretendido cp " +
            "LEFT JOIN FETCH v.candidato c " +
            "WHERE v.eleicao.id = :eleicaoId " +
            "ORDER BY v.dataVoto")
    List<Voto> findByEleicaoIdWithEntitiesForAudit(@Param("eleicaoId") UUID eleicaoId);

    // === CONSULTAS BASEADAS EM RELACIONAMENTOS ===

    /**
     * Verifica se membro já votou na eleição
     */
    boolean existsByMembroIdAndEleicaoId(UUID membroId, UUID eleicaoId);

    /**
     * Verifica se membro já votou no cargo específico da eleição
     */
    boolean existsByMembroIdAndCargoPretendidoIdAndEleicaoId(UUID membroId, UUID cargoPretendidoId, UUID eleicaoId);

    /**
     * Busca voto específico por membro, cargo e eleição
     */
    Optional<Voto> findByMembroIdAndCargoPretendidoIdAndEleicaoId(UUID membroId, UUID cargoPretendidoId, UUID eleicaoId);

    // === CONSULTAS POR TIPO DE VOTO (ENUM) ===

    /**
     * Busca votos por tipo usando enum
     */
    List<Voto> findByTipoVoto(TipoVoto tipoVoto);

    /**
     * Conta votos por tipo usando enum
     */
    long countByTipoVoto(TipoVoto tipoVoto);

    /**
     * Busca votos por eleição e tipo
     */
    List<Voto> findByEleicaoIdAndTipoVoto(UUID eleicaoId, TipoVoto tipoVoto);

    /**
     * Conta votos por eleição e tipo
     */
    long countByEleicaoIdAndTipoVoto(UUID eleicaoId, TipoVoto tipoVoto);

    /**
     * Busca votos por cargo pretendido e tipo
     */
    List<Voto> findByCargoPretendidoIdAndTipoVoto(UUID cargoPretendidoId, TipoVoto tipoVoto);

    /**
     * Conta votos por cargo pretendido e tipo
     */
    long countByCargoPretendidoIdAndTipoVoto(UUID cargoPretendidoId, TipoVoto tipoVoto);

    // === CONSULTAS BÁSICAS POR ENTIDADE ===

    /**
     * Conta votos por eleição
     */
    long countByEleicaoId(UUID eleicaoId);

    /**
     * Conta votos por cargo pretendido
     */
    long countByCargoPretendidoId(UUID cargoPretendidoId);

    /**
     * Conta votos por candidato
     */
    long countByCandidatoId(UUID candidatoId);

    /**
     * Conta membros únicos que votaram na eleição
     */
    @Query("SELECT COUNT(DISTINCT v.membro.id) FROM Voto v WHERE v.eleicao.id = :eleicaoId")
    long countDistinctMembroByEleicaoId(@Param("eleicaoId") UUID eleicaoId);

    // === CONSULTAS PARA RELATÓRIOS E ESTATÍSTICAS ===

    /**
     * Conta votos por candidato e cargo em uma eleição
     */
    @Query("SELECT v.candidato.id, v.candidato.nomeCandidato, v.cargoPretendido.id, " +
            "v.cargoPretendido.nome, COUNT(v) " +
            "FROM Voto v " +
            "WHERE v.eleicao.id = :eleicaoId AND v.tipoVoto = 'CANDIDATO' " +
            "GROUP BY v.candidato.id, v.candidato.nomeCandidato, " +
            "v.cargoPretendido.id, v.cargoPretendido.nome " +
            "ORDER BY v.cargoPretendido.nome, COUNT(v) DESC")
    List<Object[]> countVotosByCandidatoAndCargo(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Ranking de candidatos por cargo com percentuais
     */
    @Query("SELECT v.candidato.id, v.candidato.nomeCandidato, v.candidato.numeroCandidato, " +
            "COUNT(v) as totalVotos, " +
            "(COUNT(v) * 100.0 / (SELECT COUNT(v2) FROM Voto v2 " +
            "WHERE v2.eleicao.id = :eleicaoId AND v2.cargoPretendido.id = :cargoPretendidoId)) as percentual " +
            "FROM Voto v " +
            "WHERE v.eleicao.id = :eleicaoId AND v.cargoPretendido.id = :cargoPretendidoId " +
            "AND v.tipoVoto = 'CANDIDATO' " +
            "GROUP BY v.candidato.id, v.candidato.nomeCandidato, v.candidato.numeroCandidato " +
            "ORDER BY totalVotos DESC, v.candidato.nomeCandidato")
    List<Object[]> findRankingCandidatosPorVotos(@Param("eleicaoId") UUID eleicaoId,
                                                 @Param("cargoPretendidoId") UUID cargoPretendidoId);

    /**
     * Progresso de votação por hora com votos acumulados
     */
    @Query("SELECT HOUR(v.dataVoto) as hora, COUNT(v) as votosPorHora, " +
            "(SELECT COUNT(v2) FROM Voto v2 WHERE v2.eleicao.id = :eleicaoId " +
            "AND v2.dataVoto <= v.dataVoto) as votosAcumulados " +
            "FROM Voto v " +
            "WHERE v.eleicao.id = :eleicaoId " +
            "GROUP BY HOUR(v.dataVoto) " +
            "ORDER BY hora")
    List<Object[]> countVotosByHora(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Participação por cargo atual do membro com percentuais
     */
    @Query("SELECT COALESCE(v.membro.cargoAtual.nome, 'Sem Cargo') as cargoMembro, " +
            "COUNT(DISTINCT v.membro.id) as quantidadeVotantes, " +
            "(COUNT(DISTINCT v.membro.id) * 100.0 / " +
            "(SELECT COUNT(DISTINCT v2.membro.id) FROM Voto v2 WHERE v2.eleicao.id = :eleicaoId)) as percentual " +
            "FROM Voto v " +
            "WHERE v.eleicao.id = :eleicaoId " +
            "GROUP BY v.membro.cargoAtual.id, v.membro.cargoAtual.nome " +
            "ORDER BY quantidadeVotantes DESC")
    List<Object[]> getParticipacaoPorCargoMembro(@Param("eleicaoId") UUID eleicaoId);

    // === CONSULTAS PARA MÉTRICAS EM TEMPO REAL ===

    /**
     * Conta votos do último minuto
     */
    @Query("SELECT COUNT(v) FROM Voto v " +
            "WHERE v.eleicao.id = :eleicaoId " +
            "AND v.dataVoto >= :umMinutoAtras")
    long countVotosUltimoMinuto(@Param("eleicaoId") UUID eleicaoId,
                                @Param("umMinutoAtras") LocalDateTime umMinutoAtras);

    /**
     * Conta votos da última hora
     */
    @Query("SELECT COUNT(v) FROM Voto v " +
            "WHERE v.eleicao.id = :eleicaoId " +
            "AND v.dataVoto >= :umaHoraAtras")
    long countVotosUltimaHora(@Param("eleicaoId") UUID eleicaoId,
                              @Param("umaHoraAtras") LocalDateTime umaHoraAtras);

    /**
     * Calcula velocidade de votação (votos por minuto)
     */
    @Query("SELECT COUNT(v) / " +
            "GREATEST(1, EXTRACT(EPOCH FROM (MAX(v.dataVoto) - MIN(v.dataVoto))) / 60) " +
            "FROM Voto v WHERE v.eleicao.id = :eleicaoId")
    Double getVelocidadeVotacao(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Calcula tempo médio entre votos
     */
    @Query("SELECT AVG(EXTRACT(EPOCH FROM (v2.dataVoto - v1.dataVoto))) " +
            "FROM Voto v1, Voto v2 " +
            "WHERE v1.eleicao.id = :eleicaoId AND v2.eleicao.id = :eleicaoId " +
            "AND v2.dataVoto > v1.dataVoto " +
            "AND NOT EXISTS (SELECT v3 FROM Voto v3 WHERE v3.eleicao.id = :eleicaoId " +
            "AND v3.dataVoto > v1.dataVoto AND v3.dataVoto < v2.dataVoto)")
    Double getTempoMedioEntreVotos(@Param("eleicaoId") UUID eleicaoId);

    // === CONSULTAS PARA SEGURANÇA E AUDITORIA ===

    /**
     * Busca votos suspeitos por IP
     */
    @Query("SELECT v.ipOrigem, COUNT(v) as totalVotos, " +
            "AVG(EXTRACT(EPOCH FROM (v.dataVoto - " +
            "(SELECT MIN(v2.dataVoto) FROM Voto v2 WHERE v2.ipOrigem = v.ipOrigem " +
            "AND v2.eleicao.id = :eleicaoId)))) as tempoMedio " +
            "FROM Voto v " +
            "WHERE v.eleicao.id = :eleicaoId AND v.ipOrigem IS NOT NULL " +
            "GROUP BY v.ipOrigem " +
            "HAVING COUNT(v) > 5 " +
            "ORDER BY totalVotos DESC")
    List<Object[]> findVotosSuspeitos(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Conta hash duplicados
     */
    @Query("SELECT COUNT(v) FROM Voto v " +
            "WHERE v.eleicao.id = :eleicaoId " +
            "AND v.hashVoto IN (" +
            "SELECT v2.hashVoto FROM Voto v2 " +
            "WHERE v2.eleicao.id = :eleicaoId " +
            "GROUP BY v2.hashVoto HAVING COUNT(v2) > 1)")
    int countHashDuplicados(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca padrões temporais suspeitos
     */
    @Query("SELECT CONCAT(HOUR(v.dataVoto), ':', MINUTE(v.dataVoto)) as intervalo, " +
            "COUNT(v) as votosRapidos " +
            "FROM Voto v " +
            "WHERE v.eleicao.id = :eleicaoId " +
            "GROUP BY HOUR(v.dataVoto), MINUTE(v.dataVoto) " +
            "HAVING COUNT(v) > 20 " +
            "ORDER BY votosRapidos DESC")
    List<Object[]> findPadroesTemporaisSuspeitos(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Distribuição de votos por IP
     */
    @Query("SELECT v.ipOrigem, COUNT(v) " +
            "FROM Voto v " +
            "WHERE v.eleicao.id = :eleicaoId AND v.ipOrigem IS NOT NULL " +
            "GROUP BY v.ipOrigem " +
            "ORDER BY COUNT(v) DESC")
    List<Object[]> countVotosPorIpOrigem(@Param("eleicaoId") UUID eleicaoId);

    // === CONSULTAS COM FILTROS DINÂMICOS ===

    /**
     * Busca votos com filtros usando Specification
     */
    @Query("SELECT v FROM Voto v " +
            "LEFT JOIN FETCH v.membro m " +
            "LEFT JOIN FETCH v.eleicao e " +
            "LEFT JOIN FETCH v.cargoPretendido cp " +
            "LEFT JOIN FETCH v.candidato c " +
            "WHERE (:#{#filtros.eleicaoId} IS NULL OR v.eleicao.id = :#{#filtros.eleicaoId}) " +
            "AND (:#{#filtros.membroId} IS NULL OR v.membro.id = :#{#filtros.membroId}) " +
            "AND (:#{#filtros.cargoPretendidoId} IS NULL OR v.cargoPretendido.id = :#{#filtros.cargoPretendidoId}) " +
            "AND (:#{#filtros.candidatoId} IS NULL OR v.candidato.id = :#{#filtros.candidatoId}) " +
            "AND (:#{#filtros.tipoVoto} IS NULL OR v.tipoVoto = :#{#filtros.tipoVoto}) " +
            "AND (:#{#filtros.dataInicio} IS NULL OR v.dataVoto >= :#{#filtros.dataInicio}) " +
            "AND (:#{#filtros.dataFim} IS NULL OR v.dataVoto <= :#{#filtros.dataFim}) " +
            "AND (:#{#filtros.apenasVotosSeguro} = false OR (v.hashVoto IS NOT NULL AND v.ipOrigem IS NOT NULL))")
    Page<Voto> findWithFilters(@Param("filtros") VotoFilterRequest filtros, Pageable pageable);

    // === MÉTODOS UTILITÁRIOS ===

    /**
     * Busca votos com dados incompletos
     */
    @Query("SELECT v FROM Voto v " +
            "WHERE v.hashVoto IS NULL OR v.ipOrigem IS NULL OR v.userAgent IS NULL " +
            "OR v.membro IS NULL OR v.eleicao IS NULL OR v.cargoPretendido IS NULL")
    List<Voto> findVotosComDadosIncompletos();

    /**
     * Busca votos com hash duplicado
     */
    @Query("SELECT v FROM Voto v " +
            "WHERE v.hashVoto IN (" +
            "SELECT v2.hashVoto FROM Voto v2 " +
            "WHERE v2.hashVoto IS NOT NULL " +
            "GROUP BY v2.hashVoto HAVING COUNT(v2) > 1) " +
            "ORDER BY v.hashVoto, v.dataVoto")
    List<Voto> findVotosComHashDuplicado();

    // === COMPATIBILIDADE COM ESTRUTURA ANTIGA ===

    /**
     * COMPATIBILIDADE: Busca por membroId (UUID)
     */
    @Query("SELECT v FROM Voto v WHERE v.membro.id = :membroId")
    List<Voto> findByMembroId(@Param("membroId") UUID membroId);

    /**
     * COMPATIBILIDADE: Busca por eleicaoId (UUID)
     */
    @Query("SELECT v FROM Voto v WHERE v.eleicao.id = :eleicaoId")
    List<Voto> findByEleicaoId(@Param("eleicaoId") UUID eleicaoId);

    /**
     * COMPATIBILIDADE: Busca por cargoPretendidoId (UUID)
     */
    @Query("SELECT v FROM Voto v WHERE v.cargoPretendido.id = :cargoPretendidoId")
    List<Voto> findByCargoPretendidoId(@Param("cargoPretendidoId") UUID cargoPretendidoId);

    /**
     * COMPATIBILIDADE: Busca por candidatoId (UUID)
     */
    @Query("SELECT v FROM Voto v WHERE v.candidato.id = :candidatoId")
    List<Voto> findByCandidatoId(@Param("candidatoId") UUID candidatoId);

    // === ✅ QUERIES PARA MIGRAÇÃO DE DADOS CORRIGIDAS ===

    /**
     * ✅ MIGRAÇÃO CORRIGIDA: Busca votos que precisam migrar enum
     * (votos que ainda não têm tipoVoto definido)
     */
    @Query("SELECT v FROM Voto v WHERE v.tipoVoto IS NULL")
    List<Voto> findVotosParaMigracao();

    /**
     * ✅ MIGRAÇÃO: Busca votos que eram brancos na estrutura antiga
     * (assumindo que existe campo temporário ou via candidatoId null)
     */
    @Query("SELECT v FROM Voto v WHERE v.tipoVoto IS NULL AND v.candidato IS NULL")
    List<Voto> findVotosBrancosParaMigracao();

    // === ✅ IMPLEMENTAÇÕES CORRETAS DOS MÉTODOS DEPRECATED ===

    /**
     * ✅ DEPRECATED CORRIGIDO: findByVotoBrancoTrue usando enum
     */
    @Deprecated
    @Query("SELECT v FROM Voto v WHERE v.tipoVoto = 'BRANCO'")
    List<Voto> findByVotoBrancoTrue();

    /**
     * ✅ DEPRECATED CORRIGIDO: findByVotoNuloTrue usando enum
     */
    @Deprecated
    @Query("SELECT v FROM Voto v WHERE v.tipoVoto = 'NULO'")
    List<Voto> findByVotoNuloTrue();

    /**
     * ✅ DEPRECATED CORRIGIDO: countByVotoBrancoTrue usando enum
     */
    @Deprecated
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.tipoVoto = 'BRANCO'")
    long countByVotoBrancoTrue();

    /**
     * ✅ DEPRECATED CORRIGIDO: countByVotoNuloTrue usando enum
     */
    @Deprecated
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.tipoVoto = 'NULO'")
    long countByVotoNuloTrue();

    /**
     * ✅ DEPRECATED CORRIGIDO: findByEleicaoIdAndVotoBrancoTrue usando enum
     */
    @Deprecated
    @Query("SELECT v FROM Voto v WHERE v.eleicao.id = :eleicaoId AND v.tipoVoto = 'BRANCO'")
    List<Voto> findByEleicaoIdAndVotoBrancoTrue(@Param("eleicaoId") UUID eleicaoId);

    /**
     * ✅ DEPRECATED CORRIGIDO: findByEleicaoIdAndVotoNuloTrue usando enum
     */
    @Deprecated
    @Query("SELECT v FROM Voto v WHERE v.eleicao.id = :eleicaoId AND v.tipoVoto = 'NULO'")
    List<Voto> findByEleicaoIdAndVotoNuloTrue(@Param("eleicaoId") UUID eleicaoId);

    /**
     * ✅ DEPRECATED CORRIGIDO: countByEleicaoIdAndVotoBrancoTrue usando enum
     */
    @Deprecated
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.eleicao.id = :eleicaoId AND v.tipoVoto = 'BRANCO'")
    long countByEleicaoIdAndVotoBrancoTrue(@Param("eleicaoId") UUID eleicaoId);

    /**
     * ✅ DEPRECATED CORRIGIDO: countByEleicaoIdAndVotoNuloTrue usando enum
     */
    @Deprecated
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.eleicao.id = :eleicaoId AND v.tipoVoto = 'NULO'")
    long countByEleicaoIdAndVotoNuloTrue(@Param("eleicaoId") UUID eleicaoId);
}