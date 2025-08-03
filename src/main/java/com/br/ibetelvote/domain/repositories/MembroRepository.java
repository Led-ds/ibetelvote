package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Membro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembroRepository {
    Membro save(Membro membro);
    Optional<Membro> findById(UUID id);
    Optional<Membro> findByEmail(String email);
    Optional<Membro> findByUserId(UUID userId);
    Page<Membro> findAll(Pageable pageable);
    void deleteById(UUID id);
    boolean existsById(UUID id);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    boolean existsByUserId(UUID userId);
    long count();

    // Consultas por Status
    Page<Membro> findByAtivoTrue(Pageable pageable);
    long countByAtivoTrue();

    // Consultas por filtros
    Page<Membro> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Membro> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<Membro> findByCargoContainingIgnoreCase(String cargo, Pageable pageable);

    // Consultas específicas
    List<Membro> findByUserIdIsNull(); // Membros sem usuário
    List<Membro> findByUserIdIsNotNull(); // Membros com usuário
    List<Membro> findByFotoIsNull(); // Membros sem foto
    List<Membro> findMembrosWithIncompleteProfile();

    // Consultas com filtros dinâmicos
    Page<Membro> findByFilters(String nome, String email, String cargo, Boolean ativo, Boolean hasUser, Pageable pageable);
}