package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Voto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VotoRepository {
    Optional<Voto> findById(UUID id);
    Page<Voto> findAll(Pageable pageable);
    void deleteById(UUID id);
    boolean existsById(UUID id);
    long count();

    // Consultas por eleição, cargo e candidato
    List<Voto> findByEleicaoId(UUID eleicaoId);
    List<Voto> findByCargoId(UUID cargoId);
    List<Voto> findByCandidatoId(UUID candidatoId);
    List<Voto> findByEleicaoIdAndCargoId(UUID eleicaoId, UUID cargoId);

    // ADICIONADO: Método que estava faltando
    List<Voto> findByEleicaoIdAndMembroId(UUID eleicaoId, UUID membroId);

    // Consultas por membro
    List<Voto> findByMembroId(UUID membroId);
    Optional<Voto> findByMembroIdAndCargoIdAndEleicaoId(UUID membroId, UUID cargoId, UUID eleicaoId);
    boolean existsByMembroIdAndCargoIdAndEleicaoId(UUID membroId, UUID cargoId, UUID eleicaoId);

    // Consultas por tipo de voto
    List<Voto> findByVotoBrancoTrue();
    List<Voto> findByVotoNuloTrue();
    List<Voto> findVotosValidos();

    // Consultas por período
    List<Voto> findByDataVotoBetween(LocalDateTime inicio, LocalDateTime fim);

    // Estatísticas
    long countByEleicaoId(UUID eleicaoId);
    long countByCargoId(UUID cargoId);
    long countByCandidatoId(UUID candidatoId);
    long countByVotoBrancoTrue();
    long countByVotoNuloTrue();
    long countVotosValidos();

    // Relatórios
    List<Object[]> countVotosByCandidatoAndCargo(UUID eleicaoId);
    List<Object[]> countVotosByHora(UUID eleicaoId);
}