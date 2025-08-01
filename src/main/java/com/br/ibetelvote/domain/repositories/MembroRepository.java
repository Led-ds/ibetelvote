package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Membro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface MembroRepository {
    Membro save(Membro membro);
    Optional<Membro> findById(UUID id);
    Optional<Membro> findByEmail(String email);
    Optional<Membro> findByUserId(UUID userId);
    Page<Membro> findAll(Pageable pageable);
    Page<Membro> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Membro> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<Membro> findByAtivoTrue(Pageable pageable);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    void deleteById(UUID id);
    long count();
    long countByAtivoTrue();
}