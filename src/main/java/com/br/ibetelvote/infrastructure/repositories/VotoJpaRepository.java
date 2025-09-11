package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Voto;
import com.br.ibetelvote.domain.entities.enums.TipoVoto;
import com.br.ibetelvote.domain.repositories.VotoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository para Voto - Versão Simplificada.
 * Implementa apenas os métodos essenciais da interface de domínio.
 * Métodos CRUD básicos são fornecidos automaticamente pelo JpaRepository.
 * Complexidade migrada para VotoSpecifications + VotoService.
 */
@Repository
public interface VotoJpaRepository extends JpaRepository<Voto, UUID>,
        JpaSpecificationExecutor<Voto>,
        VotoRepository {

    // === IMPLEMENTAÇÃO DOS MÉTODOS DA INTERFACE DOMAIN ===
    @Override
    List<Voto> findByEleicaoId(UUID eleicaoId);

    @Override
    List<Voto> findByMembroId(UUID membroId);

    @Override
    List<Voto> findByCargoPretendidoId(UUID cargoPretendidoId);

    @Override
    List<Voto> findByCandidatoId(UUID candidatoId);

    @Override
    List<Voto> findByTipoVoto(TipoVoto tipoVoto);

    @Override
    List<Voto> findByEleicaoIdAndTipoVoto(UUID eleicaoId, TipoVoto tipoVoto);

    @Override
    long countByTipoVoto(TipoVoto tipoVoto);

    @Override
    long countByEleicaoIdAndTipoVoto(UUID eleicaoId, TipoVoto tipoVoto);

    @Override
    boolean existsByMembroIdAndEleicaoId(UUID membroId, UUID eleicaoId);

    @Override
    boolean existsByMembroIdAndCargoPretendidoIdAndEleicaoId(UUID membroId, UUID cargoPretendidoId, UUID eleicaoId);

    @Override
    Optional<Voto> findByMembroIdAndCargoPretendidoIdAndEleicaoId(UUID membroId, UUID cargoPretendidoId, UUID eleicaoId);

    @Override
    long countByEleicaoId(UUID eleicaoId);

    @Override
    long countByCargoPretendidoId(UUID cargoPretendidoId);

    @Override
    long countByCandidatoId(UUID candidatoId);

    @Override
    long countByMembroId(UUID membroId);

    @Override
    List<Voto> findByDataVotoBetween(LocalDateTime inicio, LocalDateTime fim);

    @Override
    List<Voto> findByEleicaoIdAndDataVotoBetween(UUID eleicaoId, LocalDateTime inicio, LocalDateTime fim);

}