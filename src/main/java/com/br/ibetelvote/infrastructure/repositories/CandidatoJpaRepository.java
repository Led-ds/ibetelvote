package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Candidato;
import com.br.ibetelvote.domain.repositories.CandidatoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidatoJpaRepository extends JpaRepository<Candidato, UUID>, CandidatoRepository {

    // Métodos básicos herdados do JpaRepository

    // Implementação dos métodos específicos
    List<Candidato> findByEleicaoId(UUID eleicaoId);
    List<Candidato> findByCargoId(UUID cargoId);
    List<Candidato> findByEleicaoIdAndCargoId(UUID eleicaoId, UUID cargoId);
    List<Candidato> findByMembroId(UUID membroId);
    Optional<Candidato> findByMembroIdAndCargoId(UUID membroId, UUID cargoId);
    boolean existsByMembroIdAndCargoId(UUID membroId, UUID cargoId);
    List<Candidato> findByAtivoTrue();
    List<Candidato> findByAprovadoTrue();
    List<Candidato> findByAprovadoFalse();
    List<Candidato> findByAtivoTrueAndAprovadoTrue();
    Optional<Candidato> findByNumeroCandidatoAndEleicaoId(String numero, UUID eleicaoId);
    boolean existsByNumeroCandidatoAndEleicaoId(String numero, UUID eleicaoId);
    long countByEleicaoId(UUID eleicaoId);
    long countByCargoId(UUID cargoId);
    long countByAprovadoTrue();

    @Query("SELECT c FROM Candidato c WHERE c.cargoId = :cargoId AND c.ativo = true AND c.aprovado = true ORDER BY c.nomeCandidato")
    List<Candidato> findCandidatosAprovadosByCargoId(@Param("cargoId") UUID cargoId);
}