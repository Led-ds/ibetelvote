package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Eleicao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EleicaoRepository {
    Page<Eleicao> findAll(Pageable pageable);
    void deleteById(UUID id);
    boolean existsById(UUID id);
    long count();

    // Consultas por status
    List<Eleicao> findByAtivaTrue();
    List<Eleicao> findByAtivaFalse();
    Optional<Eleicao> findEleicaoAtiva();

    // Consultas por data
    List<Eleicao> findEleicoesAbertas();
    List<Eleicao> findEleicoesEncerradas();
    List<Eleicao> findEleicoesFuturas();
    List<Eleicao> findByDataInicioBetween(LocalDateTime inicio, LocalDateTime fim);

    // Consultas específicas
    Page<Eleicao> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    List<Eleicao> findRecentEleicoes(int limit);

    // Estatísticas
    long countByAtivaTrue();
    long countEleicoesEncerradas();
    long countEleicoesFuturas();
}