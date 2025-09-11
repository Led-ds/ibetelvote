package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Eleicao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de domínio para EleicaoRepository.
 * Contém apenas métodos específicos de negócio.
 * Métodos CRUD básicos são fornecidos automaticamente pelo JpaRepository.
 * Lógicas complexas são implementadas via Specifications no Service.
 */
public interface EleicaoRepository {

    // === CONSULTAS ESPECÍFICAS POR STATUS ===
    List<Eleicao> findByAtivaTrue();
    List<Eleicao> findByAtivaFalse();
    Optional<Eleicao> findEleicaoAtiva();
    long countByAtivaTrue();

    // === CONSULTAS ESPECÍFICAS POR PERÍODO ===
    List<Eleicao> findByDataInicioBetween(LocalDateTime inicio, LocalDateTime fim);

    // === VALIDAÇÕES ESPECÍFICAS DE NEGÓCIO ===
    boolean existeEleicaoAtiva();
    boolean existeEleicaoAtivaNoMesmoPeriodo(LocalDateTime inicio, LocalDateTime fim, UUID excludeId);

    // === CONTADORES ESPECÍFICOS ===
    long countEleicoesEncerradas();
    long countEleicoesFuturas();

}