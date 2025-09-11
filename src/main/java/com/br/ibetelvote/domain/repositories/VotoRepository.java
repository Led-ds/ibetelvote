package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Voto;
import com.br.ibetelvote.domain.entities.enums.TipoVoto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de domínio para VotoRepository.
 * Contém apenas métodos específicos de negócio.
 * Métodos CRUD básicos são fornecidos automaticamente pelo JpaRepository.
 * Lógicas complexas são implementadas via Specifications no Service.
 */
public interface VotoRepository {

    // === CONSULTAS ESPECÍFICAS POR RELACIONAMENTOS ===
    List<Voto> findByEleicaoId(UUID eleicaoId);
    List<Voto> findByMembroId(UUID membroId);
    List<Voto> findByCargoPretendidoId(UUID cargoPretendidoId);
    List<Voto> findByCandidatoId(UUID candidatoId);

    // === CONSULTAS ESPECÍFICAS POR TIPO DE VOTO ===
    List<Voto> findByTipoVoto(TipoVoto tipoVoto);
    List<Voto> findByEleicaoIdAndTipoVoto(UUID eleicaoId, TipoVoto tipoVoto);
    long countByTipoVoto(TipoVoto tipoVoto);
    long countByEleicaoIdAndTipoVoto(UUID eleicaoId, TipoVoto tipoVoto);

    // === VALIDAÇÕES ESPECÍFICAS DE VOTO ÚNICO ===
    boolean existsByMembroIdAndEleicaoId(UUID membroId, UUID eleicaoId);
    boolean existsByMembroIdAndCargoPretendidoIdAndEleicaoId(UUID membroId, UUID cargoPretendidoId, UUID eleicaoId);
    Optional<Voto> findByMembroIdAndCargoPretendidoIdAndEleicaoId(UUID membroId, UUID cargoPretendidoId, UUID eleicaoId);

    // === CONTADORES ESPECÍFICOS ===
    long countByEleicaoId(UUID eleicaoId);
    long countByCargoPretendidoId(UUID cargoPretendidoId);
    long countByCandidatoId(UUID candidatoId);
    long countByMembroId(UUID membroId);

    // === CONSULTAS ESPECÍFICAS POR PERÍODO ===
    List<Voto> findByDataVotoBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Voto> findByEleicaoIdAndDataVotoBetween(UUID eleicaoId, LocalDateTime inicio, LocalDateTime fim);

    // === MÉTODOS DE CONVENIÊNCIA (DEFAULT) ===
    default List<Voto> findVotosBrancos() {
        return findByTipoVoto(TipoVoto.BRANCO);
    }

    default List<Voto> findVotosNulos() {
        return findByTipoVoto(TipoVoto.NULO);
    }

    default List<Voto> findVotosValidos() {
        return findByTipoVoto(TipoVoto.CANDIDATO);
    }

    default long countVotosBrancos() {
        return countByTipoVoto(TipoVoto.BRANCO);
    }

    default long countVotosNulos() {
        return countByTipoVoto(TipoVoto.NULO);
    }

    default long countVotosValidos() {
        return countByTipoVoto(TipoVoto.CANDIDATO);
    }
}