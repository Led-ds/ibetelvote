package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Candidato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidatoRepository {
    Page<Candidato> findAll(Pageable pageable);
    void deleteById(UUID id);
    boolean existsById(UUID id);
    long count();

    // Consultas por eleição e cargo
    List<Candidato> findByEleicaoId(UUID eleicaoId);
    List<Candidato> findByCargoId(UUID cargoId);
    List<Candidato> findByEleicaoIdAndCargoId(UUID eleicaoId, UUID cargoId);

    // Consultas por membro
    List<Candidato> findByMembroId(UUID membroId);
    Optional<Candidato> findByMembroIdAndCargoId(UUID membroId, UUID cargoId);
    boolean existsByMembroIdAndCargoId(UUID membroId, UUID cargoId);

    // Consultas por status
    List<Candidato> findByAtivoTrue();
    List<Candidato> findByAprovadoTrue();
    List<Candidato> findByAprovadoFalse();
    List<Candidato> findByAtivoTrueAndAprovadoTrue();

    // Consultas específicas
    Optional<Candidato> findByNumeroCandidatoAndEleicaoId(String numero, UUID eleicaoId);
    boolean existsByNumeroCandidatoAndEleicaoId(String numero, UUID eleicaoId);
    List<Candidato> findCandidatosAprovadosByCargoId(UUID cargoId);

    // Estatísticas
    long countByEleicaoId(UUID eleicaoId);
    long countByCargoId(UUID cargoId);
    long countByAprovadoTrue();
}