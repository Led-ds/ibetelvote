package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Eleicao;
import com.br.ibetelvote.domain.repositories.EleicaoRepository;
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
public interface EleicaoJpaRepository extends JpaRepository<Eleicao, UUID>, EleicaoRepository {

    // === CONSULTAS BÁSICAS HERDADAS DO JPA ===
    List<Eleicao> findByAtivaTrue();
    List<Eleicao> findByAtivaFalse();
    Page<Eleicao> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    long countByAtivaTrue();
    List<Eleicao> findByDataInicioBetween(LocalDateTime inicio, LocalDateTime fim);

    // === CONSULTAS COM JOINS OTIMIZADOS ===

    @Query("SELECT e FROM Eleicao e LEFT JOIN FETCH e.candidatos c WHERE e.id = :id")
    Optional<Eleicao> findByIdWithCandidatos(@Param("id") UUID id);

    @Query("SELECT e FROM Eleicao e " +
            "LEFT JOIN FETCH e.candidatos c " +
            "LEFT JOIN FETCH e.votos v " +
            "WHERE e.id = :id")
    Optional<Eleicao> findByIdWithCandidatosAndVotos(@Param("id") UUID id);

    @Query("SELECT DISTINCT e FROM Eleicao e " +
            "LEFT JOIN FETCH e.candidatos c " +
            "WHERE c.aprovado = true")
    List<Eleicao> findAllWithCandidatosAprovados();

    // === CONSULTAS POR STATUS ===

    @Query("SELECT e FROM Eleicao e WHERE e.ativa = true ORDER BY e.dataInicio DESC")
    Optional<Eleicao> findEleicaoAtiva();

    @Query("SELECT e FROM Eleicao e " +
            "LEFT JOIN FETCH e.candidatos c " +
            "WHERE e.ativa = true AND c.aprovado = true " +
            "ORDER BY e.dataInicio DESC")
    Optional<Eleicao> findEleicaoAtivaComCandidatos();

    // === CONSULTAS POR PERÍODO ===

    @Query("SELECT e FROM Eleicao e WHERE " +
            "e.ativa = true AND :now BETWEEN e.dataInicio AND e.dataFim " +
            "ORDER BY e.dataInicio ASC")
    List<Eleicao> findEleicoesAbertas(@Param("now") LocalDateTime now);

    default List<Eleicao> findEleicoesAbertas() {
        return findEleicoesAbertas(LocalDateTime.now());
    }

    @Query("SELECT e FROM Eleicao e WHERE e.dataFim < :now ORDER BY e.dataFim DESC")
    List<Eleicao> findEleicoesEncerradas(@Param("now") LocalDateTime now);

    default List<Eleicao> findEleicoesEncerradas() {
        return findEleicoesEncerradas(LocalDateTime.now());
    }

    @Query("SELECT e FROM Eleicao e WHERE e.dataInicio > :now ORDER BY e.dataInicio ASC")
    List<Eleicao> findEleicoesFuturas(@Param("now") LocalDateTime now);

    default List<Eleicao> findEleicoesFuturas() {
        return findEleicoesFuturas(LocalDateTime.now());
    }

    @Query("SELECT e FROM Eleicao e WHERE " +
            "e.ativa = true AND " +
            "((e.dataInicio BETWEEN :inicio AND :fim) OR " +
            "(e.dataFim BETWEEN :inicio AND :fim) OR " +
            "(e.dataInicio <= :inicio AND e.dataFim >= :fim))")
    List<Eleicao> findEleicoesAtivasNoPeriodo(@Param("inicio") LocalDateTime inicio,
                                              @Param("fim") LocalDateTime fim);

    // === CONSULTAS ESPECÍFICAS ===

    @Query("SELECT e FROM Eleicao e ORDER BY e.createdAt DESC LIMIT :limit")
    List<Eleicao> findRecentEleicoes(@Param("limit") int limit);

    @Query("SELECT DISTINCT e FROM Eleicao e " +
            "JOIN e.candidatos c " +
            "WHERE c.aprovado = true " +
            "ORDER BY e.dataInicio DESC")
    List<Eleicao> findEleicoesComCandidatosAprovados();

    // === VALIDAÇÕES DE NEGÓCIO ===

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Eleicao e WHERE " +
            "e.ativa = true AND " +
            "((e.dataInicio BETWEEN :inicio AND :fim) OR " +
            "(e.dataFim BETWEEN :inicio AND :fim) OR " +
            "(e.dataInicio <= :inicio AND e.dataFim >= :fim)) " +
            "AND (:excludeId IS NULL OR e.id != :excludeId)")
    boolean existeEleicaoAtivaNoMesmoPeriodo(@Param("inicio") LocalDateTime inicio,
                                             @Param("fim") LocalDateTime fim,
                                             @Param("excludeId") UUID excludeId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Eleicao e WHERE e.ativa = true")
    boolean existeEleicaoAtiva();

    // === ESTATÍSTICAS ===

    @Query("SELECT COUNT(e) FROM Eleicao e WHERE e.dataFim < :now")
    long countEleicoesEncerradas(@Param("now") LocalDateTime now);

    default long countEleicoesEncerradas() {
        return countEleicoesEncerradas(LocalDateTime.now());
    }

    @Query("SELECT COUNT(e) FROM Eleicao e WHERE e.dataInicio > :now")
    long countEleicoesFuturas(@Param("now") LocalDateTime now);

    default long countEleicoesFuturas() {
        return countEleicoesFuturas(LocalDateTime.now());
    }

    @Query("SELECT COUNT(DISTINCT e) FROM Eleicao e " +
            "JOIN e.candidatos c " +
            "WHERE c.aprovado = true")
    long countEleicoesComCandidatos();

    @Query("SELECT COUNT(DISTINCT c.cargoPretendido) FROM Eleicao e " +
            "JOIN e.candidatos c " +
            "WHERE e.id = :eleicaoId AND c.aprovado = true")
    int countCargosComCandidatosNaEleicao(@Param("eleicaoId") UUID eleicaoId);

    @Query("SELECT COUNT(c) FROM Eleicao e " +
            "JOIN e.candidatos c " +
            "WHERE e.id = :eleicaoId AND c.aprovado = true")
    int countCandidatosAprovadosNaEleicao(@Param("eleicaoId") UUID eleicaoId);

    // === CONSULTAS PARA RELATÓRIOS ===

    @Query("SELECT e FROM Eleicao e WHERE " +
            "e.dataInicio >= :inicio AND e.dataFim <= :fim " +
            "ORDER BY e.dataInicio DESC")
    List<Eleicao> findEleicoesParaRelatorio(@Param("inicio") LocalDateTime inicio,
                                            @Param("fim") LocalDateTime fim);

    @Query("SELECT e FROM Eleicao e WHERE " +
            "e.totalElegiveis > 0 AND e.totalVotantes > 0 " +
            "ORDER BY (CAST(e.totalVotantes AS double) / CAST(e.totalElegiveis AS double)) DESC " +
            "LIMIT :limit")
    List<Eleicao> findEleicoesComMaiorParticipacao(@Param("limit") int limit);

    // === CONSULTAS DE APOIO PARA VALIDAÇÕES ===

    @Query("SELECT e FROM Eleicao e " +
            "WHERE e.ativa = true AND " +
            "EXISTS (SELECT 1 FROM Candidato c WHERE c.eleicao = e AND c.aprovado = true) " +
            "AND :now BETWEEN e.dataInicio AND e.dataFim")
    List<Eleicao> findEleicoesAbertasComCandidatos(@Param("now") LocalDateTime now);

    default List<Eleicao> findEleicoesAbertasComCandidatos() {
        return findEleicoesAbertasComCandidatos(LocalDateTime.now());
    }

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Eleicao e " +
            "WHERE e.id = :eleicaoId AND " +
            "EXISTS (SELECT 1 FROM Candidato c WHERE c.eleicao = e AND c.aprovado = true)")
    boolean eleicaoTemCandidatosAprovados(@Param("eleicaoId") UUID eleicaoId);

    @Query("SELECT e FROM Eleicao e " +
            "WHERE e.ativa = false AND " +
            "NOT EXISTS (SELECT 1 FROM Candidato c WHERE c.eleicao = e) AND " +
            "NOT EXISTS (SELECT 1 FROM Voto v WHERE v.eleicao = e)")
    List<Eleicao> findEleicoesVaziasParaLimpeza();
}