package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de domínio para CargoRepository.
 * Contém apenas métodos específicos de negócio.
 * Métodos CRUD básicos são fornecidos automaticamente pelo JpaRepository.
 * Lógicas complexas são implementadas via Specifications no Service.
 */
public interface CargoRepository {

    // === CONSULTAS ESPECÍFICAS POR ATRIBUTOS ===

    Optional<Cargo> findByNome(String nome);
    boolean existsByNome(String nome);
    List<Cargo> findByAtivoTrue();
    List<Cargo> findByDisponivelEleicaoTrue();
    List<Cargo> findByHierarquia(HierarquiaCargo hierarquia);

    // === CONSULTAS ORDENADAS ESPECÍFICAS ===

    List<Cargo> findAllByOrderByNomeAsc();
    List<Cargo> findByAtivoTrueOrderByNomeAsc();

    // === VALIDAÇÕES ESPECÍFICAS DE NEGÓCIO ===

    boolean existsByNomeAndIdNot(String nome, UUID id);
    boolean canDeleteCargo(UUID id);

    // === SUPORTE PARA SPECIFICATIONS ===

    Optional<String> findNomeById(UUID cargoId);
    Optional<HierarquiaCargo> findHierarquiaById(UUID cargoId);

}