package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Voto;
import com.br.ibetelvote.domain.repositories.VotoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VotoJpaRepository extends JpaRepository<Voto, UUID>, VotoRepository {

    // Métodos básicos herdados do JpaRepository

    // Implementação dos métodos específicos
    List<Voto> findByEleicaoId(UUID eleicaoId);
    List<Voto> findByCargoId(UUID cargoId);
    List<Voto> findByCandidatoId(UUID candidatoId);
    List<Voto> findByEleicaoIdAndCargoId(UUID eleicaoId, UUID cargoId);

    // ADICIONADO: Método que estava faltando
    List<Voto> findByEleicaoIdAndMembroId(UUID eleicaoId, UUID membroId);

    List<Voto> findByMembroId(UUID membroId);
    Optional<Voto> findByMembroIdAndCargoIdAndEleicaoId(UUID membroId, UUID cargoId, UUID eleicaoId);
    boolean existsByMembroIdAndCargoIdAndEleicaoId(UUID membroId, UUID cargoId, UUID eleicaoId);
    List<Voto> findByVotoBrancoTrue();
    List<Voto> findByVotoNuloTrue();
    List<Voto> findByDataVotoBetween(LocalDateTime inicio, LocalDateTime fim);
    long countByEleicaoId(UUID eleicaoId);
    long countByCargoId(UUID cargoId);
    long countByCandidatoId(UUID candidatoId);
    long countByVotoBrancoTrue();
    long countByVotoNuloTrue();

    @Query("SELECT v FROM Voto v WHERE v.candidatoId IS NOT NULL AND v.votoBranco = false AND v.votoNulo = false")
    List<Voto> findVotosValidos();

    @Query("SELECT COUNT(v) FROM Voto v WHERE v.candidatoId IS NOT NULL AND v.votoBranco = false AND v.votoNulo = false")
    long countVotosValidos();

    @Query("SELECT c.nomeCandidato, COUNT(v) FROM Voto v " +
            "JOIN Candidato c ON v.candidatoId = c.id " +
            "WHERE v.eleicaoId = :eleicaoId AND v.candidatoId IS NOT NULL " +
            "GROUP BY c.id, c.nomeCandidato " +
            "ORDER BY COUNT(v) DESC")
    List<Object[]> countVotosByCandidatoAndCargo(@Param("eleicaoId") UUID eleicaoId);

    @Query("SELECT HOUR(v.dataVoto), COUNT(v) FROM Voto v " +
            "WHERE v.eleicaoId = :eleicaoId " +
            "GROUP BY HOUR(v.dataVoto) " +
            "ORDER BY HOUR(v.dataVoto)")
    List<Object[]> countVotosByHora(@Param("eleicaoId") UUID eleicaoId);
}