package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Candidato;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de domínio para CandidatoRepository.
 * Contém apenas métodos específicos de negócio.
 * Métodos CRUD básicos são fornecidos automaticamente pelo JpaRepository.
 * Lógicas complexas são implementadas via Specifications no Service.
 */
public interface CandidatoRepository {

    // === CONSULTAS ESPECÍFICAS POR RELACIONAMENTOS ===
    List<Candidato> findByEleicaoId(UUID eleicaoId);
    List<Candidato> findByCargoPretendidoId(UUID cargoId);
    List<Candidato> findByMembroId(UUID membroId);

    // === CONSULTAS ESPECÍFICAS POR STATUS ===
    List<Candidato> findByAtivoTrue();
    List<Candidato> findByAprovadoTrue();
    List<Candidato> findByAprovadoFalse();

    // === CONSULTAS POR NÚMERO DE CANDIDATO ===
    Optional<Candidato> findByNumeroCandidatoAndEleicaoId(String numeroCandidato, UUID eleicaoId);
    boolean existsByNumeroCandidatoAndEleicaoId(String numeroCandidato, UUID eleicaoId);

    // === VALIDAÇÕES ESPECÍFICAS DE NEGÓCIO ===
    boolean existsByMembroIdAndEleicaoId(UUID membroId, UUID eleicaoId);
    boolean existsByMembroIdAndCargoPretendidoIdAndEleicaoId(UUID membroId, UUID cargoId, UUID eleicaoId);
    boolean existsByNumeroCandidatoAndEleicaoIdAndIdNot(String numeroCandidato, UUID eleicaoId, UUID candidatoId);

    // === CONTADORES ESPECÍFICOS ===
    long countByEleicaoId(UUID eleicaoId);
    long countByCargoPretendidoId(UUID cargoId);
    long countByAtivo(Boolean ativo);
    long countByAprovado(Boolean aprovado);

}