package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.repositories.CargoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CargoJpaRepository extends JpaRepository<Cargo, UUID>, CargoRepository {

    // Métodos básicos herdados do JpaRepository

    // Implementação dos métodos específicos
    List<Cargo> findByEleicaoId(UUID eleicaoId);
    List<Cargo> findByEleicaoIdOrderByOrdemVotacao(UUID eleicaoId);
    long countByEleicaoId(UUID eleicaoId);
    Optional<Cargo> findByNomeAndEleicaoId(String nome, UUID eleicaoId);
    List<Cargo> findByObrigatorioTrue();
    List<Cargo> findByPermiteVotoBrancoTrue();
    boolean existsByNomeAndEleicaoId(String nome, UUID eleicaoId);
}