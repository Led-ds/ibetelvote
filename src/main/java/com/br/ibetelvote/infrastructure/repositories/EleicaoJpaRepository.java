package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Eleicao;
import com.br.ibetelvote.domain.repositories.EleicaoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository para Eleicao - Versão Simplificada.
 * Implementa apenas os métodos essenciais da interface de domínio.
 * Métodos CRUD básicos são fornecidos automaticamente pelo JpaRepository.
 * Complexidade migrada para EleicaoSpecifications + EleicaoService.
 */
@Repository
public interface EleicaoJpaRepository extends JpaRepository<Eleicao, UUID>,
        JpaSpecificationExecutor<Eleicao>,
        EleicaoRepository {

    // === IMPLEMENTAÇÃO DOS MÉTODOS DA INTERFACE DOMAIN ===
    @Override
    List<Eleicao> findByAtivaTrue();

    @Override
    List<Eleicao> findByAtivaFalse();

    @Override
    @Query("SELECT e FROM Eleicao e WHERE e.ativa = true ORDER BY e.dataInicio DESC")
    Optional<Eleicao> findEleicaoAtiva();

    @Override
    long countByAtivaTrue();

    @Override
    List<Eleicao> findByDataInicioBetween(LocalDateTime inicio, LocalDateTime fim);

    @Override
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Eleicao e WHERE e.ativa = true")
    boolean existeEleicaoAtiva();

    @Override
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Eleicao e WHERE " +
            "e.ativa = true AND " +
            "((e.dataInicio BETWEEN :inicio AND :fim) OR " +
            "(e.dataFim BETWEEN :inicio AND :fim) OR " +
            "(e.dataInicio <= :inicio AND e.dataFim >= :fim)) " +
            "AND (:excludeId IS NULL OR e.id != :excludeId)")
    boolean existeEleicaoAtivaNoMesmoPeriodo(@Param("inicio") LocalDateTime inicio,
                                             @Param("fim") LocalDateTime fim,
                                             @Param("excludeId") UUID excludeId);


    @Query("SELECT COUNT(e) FROM Eleicao e WHERE e.dataFim < :now")
    long countEleicoesEncerradas(@Param("now") LocalDateTime now);

    @Override
    default long countEleicoesEncerradas() {
        return countEleicoesEncerradas(LocalDateTime.now());
    }


    @Query("SELECT COUNT(e) FROM Eleicao e WHERE e.dataInicio > :now")
    long countEleicoesFuturas(@Param("now") LocalDateTime now);

    @Override
    default long countEleicoesFuturas() {
        return countEleicoesFuturas(LocalDateTime.now());
    }

}