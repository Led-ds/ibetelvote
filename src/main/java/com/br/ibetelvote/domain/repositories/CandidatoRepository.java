package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Candidato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidatoRepository {

    // === OPERAÇÕES BÁSICAS ===
    void deleteById(UUID id);
    long count();


    // === CONSULTAS POR ELEIÇÃO ===
    List<Candidato> findByEleicaoId(UUID eleicaoId);
    Page<Candidato> findByEleicaoId(UUID eleicaoId, Pageable pageable);
    long countByEleicaoId(UUID eleicaoId);
    List<Candidato> findByEleicaoIdOrderByNomeCandidatoAsc(UUID eleicaoId);

    // === CONSULTAS POR CARGO PRETENDIDO ===
    List<Candidato> findByCargoPretendidoId(UUID cargoId);
    Page<Candidato> findByCargoPretendidoId(UUID cargoId, Pageable pageable);
    long countByCargoPretendidoId(UUID cargoId);
    List<Candidato> findByCargoPretendidoIdOrderByNomeCandidatoAsc(UUID cargoId);

    // === CONSULTAS POR MEMBRO ===
    List<Candidato> findByMembroId(UUID membroId);
    Page<Candidato> findByMembroId(UUID membroId, Pageable pageable);
    long countByMembroId(UUID membroId);
    Optional<Candidato> findByMembroIdAndEleicaoId(UUID membroId, UUID eleicaoId);

    // === CONSULTAS POR STATUS ===
    List<Candidato> findByAtivoTrue();
    List<Candidato> findByAtivoFalse();
    Page<Candidato> findByAtivo(Boolean ativo, Pageable pageable);
    long countByAtivo(Boolean ativo);

    List<Candidato> findByAprovadoTrue();
    List<Candidato> findByAprovadoFalse();
    Page<Candidato> findByAprovado(Boolean aprovado, Pageable pageable);
    long countByAprovado(Boolean aprovado);

    // === CONSULTAS COMBINADAS ===
    List<Candidato> findByEleicaoIdAndCargoPretendidoId(UUID eleicaoId, UUID cargoId);
    List<Candidato> findByEleicaoIdAndAtivoTrue(UUID eleicaoId);
    List<Candidato> findByEleicaoIdAndAprovadoTrue(UUID eleicaoId);
    List<Candidato> findByEleicaoIdAndAtivoTrueAndAprovadoTrue(UUID eleicaoId);

    // === MÉTODOS FALTANTES ADICIONADOS ===
    List<Candidato> findByCargoPretendidoIdAndEleicaoIdAndAtivoTrueAndAprovadoTrue(UUID cargoId, UUID eleicaoId);
    List<Candidato> findByCargoPretendidoIdAndAtivoTrueAndAprovadoTrue(UUID cargoId);
    long countByEleicaoIdAndAtivoTrue(UUID eleicaoId);
    long countByEleicaoIdAndAprovadoTrue(UUID eleicaoId);
    long countByEleicaoIdAndAprovadoFalse(UUID eleicaoId);
    long countByEleicaoIdAndAtivoFalse(UUID eleicaoId);
    List<Candidato> findByEleicaoIdAndAprovadoFalse(UUID eleicaoId);
    List<Candidato> findByEleicaoIdAndAtivoFalse(UUID eleicaoId);

    // === CONSULTAS POR NÚMERO ===
    Optional<Candidato> findByNumeroCandidato(String numeroCandidato);
    Optional<Candidato> findByNumeroCandidatoAndEleicaoId(String numeroCandidato, UUID eleicaoId);
    boolean existsByNumeroCandidatoAndEleicaoId(String numeroCandidato, UUID eleicaoId);

    // === CONSULTAS PARA VALIDAÇÃO ===
    boolean existsByMembroIdAndEleicaoId(UUID membroId, UUID eleicaoId);
    boolean existsByMembroIdAndCargoPretendidoIdAndEleicaoId(UUID membroId, UUID cargoId, UUID eleicaoId);
    boolean existsByNumeroCandidatoAndEleicaoIdAndIdNot(String numeroCandidato, UUID eleicaoId, UUID candidatoId);

    // === CONSULTAS CUSTOMIZADAS ===
    List<Candidato> findCandidatosElegiveis(UUID eleicaoId);
    List<Candidato> findByCargoPretendidoIdAndEleicaoIdOrderByVotosDesc(UUID cargoId, UUID eleicaoId);
    List<Candidato> findCandidatosComCandidaturaCompleta(UUID eleicaoId);
    List<Candidato> findCandidatosPendentesAprovacao(UUID eleicaoId);
    List<Candidato> findByNomeCandidatoContainingIgnoreCase(String nome);
    List<Candidato> findTopCandidatosPorCargo(UUID cargoId, UUID eleicaoId, int limite);
    long countCandidatosAtivosPorCargoEEleicao(UUID cargoId, UUID eleicaoId);
    List<Candidato> findCandidatosSemNumero(UUID eleicaoId);
    List<Candidato> findUltimosCandidatosCadastrados(int limite);
    List<Object[]> countCandidatosPorStatusNaEleicao(UUID eleicaoId);
    List<Object[]> countCandidatosPorCargoNaEleicao(UUID eleicaoId);
    List<Candidato> findRankingCandidatosPorCargo(UUID cargoId, UUID eleicaoId);
    List<Candidato> findCandidatosComFoto(UUID eleicaoId);
    List<Candidato> findCandidatosSemFoto(UUID eleicaoId);
    boolean membroJaEhCandidatoEmEleicaoAtiva(UUID membroId);
    long countCandidaturasMembroPorEleicao(UUID membroId, UUID eleicaoId);
    List<Candidato> findTodosCandidatosElegiveisParaVotacao();
}