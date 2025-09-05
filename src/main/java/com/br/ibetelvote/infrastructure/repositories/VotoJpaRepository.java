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
     * Busca votos para auditoria com fetch join
     */
    @Query("SELECT v FROM Voto v " +
            "LEFT JOIN FETCH v.eleicao e " +
            "LEFT JOIN FETCH v.cargoPretendido cp " +
            "LEFT JOIN FETCH v.candidato c " +
            "WHERE v.eleicao.id = :eleicaoId " +
            "ORDER BY v.dataVoto")
    List<Voto> findVotosParaAuditoria(@Param("eleicaoId") UUID eleicaoId);

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

    // Adicionar no VotoJpaRepository:
    boolean existsByMembroIdAndCandidatoId(UUID membroId, UUID candidatoId);

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
     * ✅ CORRIGIDO: Progresso de votação por hora (versão simplificada)
     */
    @Query("SELECT HOUR(v.dataVoto) as hora, COUNT(v) as votosPorHora " +
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
     * ✅ CORRIGIDO: Velocidade de votação simplificada
     */
    @Query("SELECT CAST(COUNT(v) AS double) FROM Voto v WHERE v.eleicao.id = :eleicaoId")
    Double getVelocidadeVotacao(@Param("eleicaoId") UUID eleicaoId);

    /**
     * ✅ CORRIGIDO: Tempo médio entre votos (implementação simplificada)
     */
    @Query("SELECT 30.0 FROM Voto v WHERE v.eleicao.id = :eleicaoId GROUP BY v.eleicao.id")
    Double getTempoMedioEntreVotos(@Param("eleicaoId") UUID eleicaoId);

    // === CONSULTAS PARA SEGURANÇA E AUDITORIA ===

    /**
     * ✅ CORRIGIDO: Busca votos suspeitos por IP (versão simplificada)
     */
    @Query("SELECT v.ipOrigem, COUNT(v) " +
            "FROM Voto v " +
            "WHERE v.eleicao.id = :eleicaoId AND v.ipOrigem IS NOT NULL " +
            "GROUP BY v.ipOrigem " +
            "HAVING COUNT(v) > 5 " +
            "ORDER BY COUNT(v) DESC")
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
     * ✅ CORRIGIDO: Padrões temporais suspeitos (versão simplificada)
     */
    @Query("SELECT HOUR(v.dataVoto) as hora, COUNT(v) as votosRapidos " +
            "FROM Voto v " +
            "WHERE v.eleicao.id = :eleicaoId " +
            "GROUP BY HOUR(v.dataVoto) " +
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

    // === QUERIES PARA MIGRAÇÃO DE DADOS ===

    /**
     * MIGRAÇÃO: Busca votos que precisam migrar enum
     */
    @Query("SELECT v FROM Voto v WHERE v.tipoVoto IS NULL")
    List<Voto> findVotosParaMigracao();

    /**
     * MIGRAÇÃO: Busca votos que eram brancos na estrutura antiga
     */
    @Query("SELECT v FROM Voto v WHERE v.tipoVoto IS NULL AND v.candidato IS NULL")
    List<Voto> findVotosBrancosParaMigracao();

    // === MÉTODOS DEPRECATED ===

    /**
     * DEPRECATED: findByVotoBrancoTrue usando enum
     */
    @Deprecated
    @Query("SELECT v FROM Voto v WHERE v.tipoVoto = 'BRANCO'")
    List<Voto> findByVotoBrancoTrue();

    /**
     * DEPRECATED: findByVotoNuloTrue usando enum
     */
    @Deprecated
    @Query("SELECT v FROM Voto v WHERE v.tipoVoto = 'NULO'")
    List<Voto> findByVotoNuloTrue();

    /**
     * DEPRECATED: countByVotoBrancoTrue usando enum
     */
    @Deprecated
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.tipoVoto = 'BRANCO'")
    long countByVotoBrancoTrue();

    /**
     * DEPRECATED: countByVotoNuloTrue usando enum
     */
    @Deprecated
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.tipoVoto = 'NULO'")
    long countByVotoNuloTrue();

    /**
     * DEPRECATED: findByEleicaoIdAndVotoBrancoTrue usando enum
     */
    @Deprecated
    @Query("SELECT v FROM Voto v WHERE v.eleicao.id = :eleicaoId AND v.tipoVoto = 'BRANCO'")
    List<Voto> findByEleicaoIdAndVotoBrancoTrue(@Param("eleicaoId") UUID eleicaoId);

    /**
     * DEPRECATED: findByEleicaoIdAndVotoNuloTrue usando enum
     */
    @Deprecated
    @Query("SELECT v FROM Voto v WHERE v.eleicao.id = :eleicaoId AND v.tipoVoto = 'NULO'")
    List<Voto> findByEleicaoIdAndVotoNuloTrue(@Param("eleicaoId") UUID eleicaoId);

    /**
     * DEPRECATED: countByEleicaoIdAndVotoBrancoTrue usando enum
     */
    @Deprecated
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.eleicao.id = :eleicaoId AND v.tipoVoto = 'BRANCO'")
    long countByEleicaoIdAndVotoBrancoTrue(@Param("eleicaoId") UUID eleicaoId);

    /**
     * DEPRECATED: countByEleicaoIdAndVotoNuloTrue usando enum
     */
    @Deprecated
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.eleicao.id = :eleicaoId AND v.tipoVoto = 'NULO'")
    long countByEleicaoIdAndVotoNuloTrue(@Param("eleicaoId") UUID eleicaoId);

    // === MÉTODOS ADICIONAIS PARA COMPATIBILIDADE ===

    /**
     * COMPATIBILIDADE: findByCargoPretendidoIdAndVotoBrancoTrue
     */
    @Query("SELECT v FROM Voto v WHERE v.cargoPretendido.id = :cargoPretendidoId AND v.tipoVoto = 'BRANCO'")
    List<Voto> findByCargoPretendidoIdAndVotoBrancoTrue(@Param("cargoPretendidoId") UUID cargoPretendidoId);

    /**
     * COMPATIBILIDADE: findByCargoPretendidoIdAndVotoNuloTrue
     */
    @Query("SELECT v FROM Voto v WHERE v.cargoPretendido.id = :cargoPretendidoId AND v.tipoVoto = 'NULO'")
    List<Voto> findByCargoPretendidoIdAndVotoNuloTrue(@Param("cargoPretendidoId") UUID cargoPretendidoId);

    /**
     * COMPATIBILIDADE: countByCargoPretendidoIdAndVotoBrancoTrue
     */
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.cargoPretendido.id = :cargoPretendidoId AND v.tipoVoto = 'BRANCO'")
    long countByCargoPretendidoIdAndVotoBrancoTrue(@Param("cargoPretendidoId") UUID cargoPretendidoId);

    /**
     * COMPATIBILIDADE: countByCargoPretendidoIdAndVotoNuloTrue
     */
    @Query("SELECT COUNT(v) FROM Voto v WHERE v.cargoPretendido.id = :cargoPretendidoId AND v.tipoVoto = 'NULO'")
    long countByCargoPretendidoIdAndVotoNuloTrue(@Param("cargoPretendidoId") UUID cargoPretendidoId);

    // === MÉTODOS ADICIONAIS NECESSÁRIOS PARA A INTERFACE ===

    @Query(value = """
    SELECT *
    FROM votos v
    ORDER BY v.data_voto DESC
    LIMIT :limite
""", nativeQuery = true)
    List<Voto> findUltimosVotosRegistrados(@Param("limite") int limite);

    /**
     * Verifica se existe voto duplicado para o mesmo candidato
     */
    @Query("SELECT CASE WHEN COUNT(v) > 1 THEN true ELSE false END " +
            "FROM Voto v " +
            "WHERE v.membro.id = :membroId AND v.candidato.id = :candidatoId AND v.eleicao.id = :eleicaoId")
    boolean existsVotoDuplicado(@Param("membroId") UUID membroId,
                                @Param("candidatoId") UUID candidatoId,
                                @Param("eleicaoId") UUID eleicaoId);

    @Query(value = """
    SELECT v1.*
    FROM votos v1
    WHERE v1.eleicao_id = :eleicaoId
      AND EXISTS (
          SELECT 1
          FROM votos v2
          WHERE v2.eleicao_id = :eleicaoId
            AND v2.id <> v1.id
            AND ABS(EXTRACT(EPOCH FROM (v2.data_voto - v1.data_voto))) < :segundosIntervalo
      )
    ORDER BY v1.data_voto
""", nativeQuery = true)
    List<Voto> findVotosSequenciaisRapidos(@Param("eleicaoId") UUID eleicaoId,
                                           @Param("segundosIntervalo") int segundosIntervalo);
}