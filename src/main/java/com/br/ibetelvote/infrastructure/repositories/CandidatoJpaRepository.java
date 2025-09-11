package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Candidato;
import com.br.ibetelvote.domain.repositories.CandidatoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository para Candidato - Versão Simplificada.
 * Implementa apenas os métodos essenciais da interface de domínio.
 * Métodos CRUD básicos são fornecidos automaticamente pelo JpaRepository.
 * Complexidade migrada para CandidatoSpecifications + CandidatoService.
 */
@Repository
public interface CandidatoJpaRepository extends JpaRepository<Candidato, UUID>,
        JpaSpecificationExecutor<Candidato>,
        CandidatoRepository {

    // === IMPLEMENTAÇÃO DOS MÉTODOS DA INTERFACE DOMAIN ===
    @Override
    List<Candidato> findByEleicaoId(UUID eleicaoId);

    @Override
    List<Candidato> findByCargoPretendidoId(UUID cargoId);

    @Override
    List<Candidato> findByMembroId(UUID membroId);

    @Override
    List<Candidato> findByAtivoTrue();

    @Override
    List<Candidato> findByAprovadoTrue();

    @Override
    List<Candidato> findByAprovadoFalse();

    @Override
    Optional<Candidato> findByNumeroCandidatoAndEleicaoId(String numeroCandidato, UUID eleicaoId);

    @Override
    boolean existsByNumeroCandidatoAndEleicaoId(String numeroCandidato, UUID eleicaoId);

    @Override
    boolean existsByMembroIdAndEleicaoId(UUID membroId, UUID eleicaoId);

    @Override
    boolean existsByMembroIdAndCargoPretendidoIdAndEleicaoId(UUID membroId, UUID cargoId, UUID eleicaoId);

    @Override
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Candidato c WHERE c.numeroCandidato = :numeroCandidato AND c.eleicaoId = :eleicaoId AND c.id != :candidatoId")
    boolean existsByNumeroCandidatoAndEleicaoIdAndIdNot(@Param("numeroCandidato") String numeroCandidato,
                                                        @Param("eleicaoId") UUID eleicaoId,
                                                        @Param("candidatoId") UUID candidatoId);

    @Override
    long countByEleicaoId(UUID eleicaoId);

    @Override
    long countByCargoPretendidoId(UUID cargoId);

    @Override
    long countByAtivo(Boolean ativo);

    @Override
    long countByAprovado(Boolean aprovado);

}