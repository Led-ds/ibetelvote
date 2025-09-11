package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Membro;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de domínio para MembroRepository.
 * Contém apenas métodos específicos de negócio.
 * Métodos CRUD básicos são fornecidos automaticamente pelo JpaRepository.
 * Lógicas complexas são implementadas via Specifications no Service.
 */
public interface MembroRepository {

    // === CONSULTAS ESPECÍFICAS POR ATRIBUTOS ÚNICOS ===
    Optional<Membro> findByEmail(String email);
    Optional<Membro> findByCpf(String cpf);
    Optional<Membro> findByUserId(UUID userId);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    boolean existsByUserId(UUID userId);

    // === CONSULTAS ESPECÍFICAS POR STATUS ===
    List<Membro> findByAtivoTrue();
    List<Membro> findByAtivoFalse();
    long countByAtivo(Boolean ativo);

    // === CONSULTAS ESPECÍFICAS POR RELACIONAMENTOS ===
    List<Membro> findByCargoAtualId(UUID cargoId);
    long countByCargoAtualId(UUID cargoId);

    // === CONSULTAS ORDENADAS ESPECÍFICAS ===
    List<Membro> findAllByOrderByNomeAsc();
    List<Membro> findByAtivoTrueOrderByNomeAsc();

    // === VALIDAÇÕES ESPECÍFICAS DE NEGÓCIO ===
    boolean existsByEmailAndIdNot(String email, UUID id);
    boolean existsByCpfAndIdNot(String cpf, UUID id);

    // === BUSCA ESPECÍFICA ===
    List<Membro> findByNomeContainingIgnoreCase(String nome);

}