package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Cargo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CargoRepository {

    // === OPERAÇÕES BÁSICAS ===
    List<Cargo> findAll();
    Page<Cargo> findAll(Pageable pageable);
    void deleteById(UUID id);
    long count();

    // === CONSULTAS POR NOME ===
    Optional<Cargo> findByNome(String nome);
    boolean existsByNome(String nome);
    List<Cargo> findByNomeContainingIgnoreCase(String nome);

    // === CONSULTAS POR STATUS ===
    List<Cargo> findByAtivoTrue();
    List<Cargo> findByAtivoFalse();
    Page<Cargo> findByAtivo(Boolean ativo, Pageable pageable);
    long countByAtivo(Boolean ativo);

    // === CONSULTAS ORDENADAS ===
    List<Cargo> findAllByOrderByNomeAsc();
    List<Cargo> findByAtivoTrueOrderByNomeAsc();
    Page<Cargo> findByAtivoTrueOrderByNomeAsc(Pageable pageable);

    // === CONSULTAS PARA VALIDAÇÃO ===
    boolean existsByNomeAndIdNot(String nome, UUID id);

    // === CONSULTAS PARA RELATÓRIOS ===
    List<Cargo> findTop10ByOrderByCreatedAtDesc();
}