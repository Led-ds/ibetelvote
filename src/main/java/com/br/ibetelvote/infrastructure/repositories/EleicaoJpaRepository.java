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
    List<Eleicao> findByAtivaTrue();
    List<Eleicao> findByAtivaFalse();
    Page<Eleicao> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    long countByAtivaTrue();

    @Query("SELECT e FROM Eleicao e WHERE e.ativa = true ORDER BY e.dataInicio DESC")
    Optional<Eleicao> findEleicaoAtiva();

    @Query("SELECT e FROM Eleicao e WHERE e.ativa = true AND :now BETWEEN e.dataInicio AND e.dataFim")
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

    List<Eleicao> findByDataInicioBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT e FROM Eleicao e ORDER BY e.createdAt DESC LIMIT :limit")
    List<Eleicao> findRecentEleicoes(@Param("limit") int limit);

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
}