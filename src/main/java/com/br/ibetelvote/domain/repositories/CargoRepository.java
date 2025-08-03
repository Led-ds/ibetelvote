package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Cargo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CargoRepository {
    Cargo save(Cargo cargo);
    Page<Cargo> findAll(Pageable pageable);
    void deleteById(UUID id);
    boolean existsById(UUID id);
    long count();

    // Consultas por eleição
    List<Cargo> findByEleicaoId(UUID eleicaoId);
    List<Cargo> findByEleicaoIdOrderByOrdemVotacao(UUID eleicaoId);
    long countByEleicaoId(UUID eleicaoId);

    // Consultas específicas
    Optional<Cargo> findByNomeAndEleicaoId(String nome, UUID eleicaoId);
    List<Cargo> findByObrigatorioTrue();
    List<Cargo> findByPermiteVotoBrancoTrue();
    boolean existsByNomeAndEleicaoId(String nome, UUID eleicaoId);
}