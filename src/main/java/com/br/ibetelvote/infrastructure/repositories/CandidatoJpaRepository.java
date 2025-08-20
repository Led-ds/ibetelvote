package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Candidato;
import com.br.ibetelvote.domain.repositories.CandidatoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidatoJpaRepository extends JpaRepository<Candidato, UUID>, CandidatoRepository {

    // === OPERAÇÕES BÁSICAS ===
    // Métodos básicos herdados do JpaRepository

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

    // === CONSULTAS POR NÚMERO ===
    Optional<Candidato> findByNumeroCandidato(String numeroCandidato);
    Optional<Candidato> findByNumeroCandidatoAndEleicaoId(String numeroCandidato, UUID eleicaoId);
    boolean existsByNumeroCandidatoAndEleicaoId(String numeroCandidato, UUID eleicaoId);

    // === CONSULTAS PARA VALIDAÇÃO ===
    boolean existsByMembroIdAndEleicaoId(UUID membroId, UUID eleicaoId);
    boolean existsByMembroIdAndCargoPretendidoIdAndEleicaoId(UUID membroId, UUID cargoId, UUID eleicaoId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Candidato c WHERE c.numeroCandidato = :numeroCandidato AND c.eleicaoId = :eleicaoId AND c.id != :candidatoId")
    boolean existsByNumeroCandidatoAndEleicaoIdAndIdNot(@Param("numeroCandidato") String numeroCandidato,
                                                        @Param("eleicaoId") UUID eleicaoId,
                                                        @Param("candidatoId") UUID candidatoId);

    // === CONSULTAS CUSTOMIZADAS ===

    /**
     * Busca candidatos elegíveis (ativos, aprovados e aptos para votação)
     */
    @Query("SELECT c FROM Candidato c JOIN c.membro m JOIN c.cargoPretendido cp JOIN c.eleicao e WHERE " +
            "c.eleicaoId = :eleicaoId AND c.ativo = true AND c.aprovado = true AND " +
            "m.ativo = true AND cp.ativo = true ORDER BY c.nomeCandidato")
    List<Candidato> findCandidatosElegiveis(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca candidatos por cargo em eleição específica ordenados por votos
     */
    @Query("SELECT c FROM Candidato c WHERE c.cargoPretendidoId = :cargoId AND c.eleicaoId = :eleicaoId " +
            "ORDER BY SIZE(c.votos) DESC, c.nomeCandidato ASC")
    List<Candidato> findByCargoPretendidoIdAndEleicaoIdOrderByVotosDesc(@Param("cargoId") UUID cargoId,
                                                                        @Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca candidatos com candidatura completa
     */
    @Query("SELECT c FROM Candidato c WHERE c.eleicaoId = :eleicaoId AND c.ativo = true AND " +
            "c.nomeCandidato IS NOT NULL AND c.descricaoCandidatura IS NOT NULL AND c.propostas IS NOT NULL " +
            "ORDER BY c.nomeCandidato")
    List<Candidato> findCandidatosComCandidaturaCompleta(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca candidatos pendentes de aprovação
     */
    @Query("SELECT c FROM Candidato c WHERE c.eleicaoId = :eleicaoId AND c.ativo = true AND c.aprovado = false " +
            "ORDER BY c.createdAt ASC")
    List<Candidato> findCandidatosPendentesAprovacao(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca candidatos por nome (busca parcial)
     */
    @Query("SELECT c FROM Candidato c WHERE UPPER(c.nomeCandidato) LIKE UPPER(CONCAT('%', :nome, '%')) " +
            "ORDER BY c.nomeCandidato")
    List<Candidato> findByNomeCandidatoContainingIgnoreCase(@Param("nome") String nome);

    /**
     * Busca candidatos com mais votos por cargo
     */
    @Query("SELECT c FROM Candidato c WHERE c.cargoPretendidoId = :cargoId AND c.eleicaoId = :eleicaoId AND " +
            "c.ativo = true AND c.aprovado = true ORDER BY SIZE(c.votos) DESC, c.nomeCandidato ASC")
    List<Candidato> findTopCandidatosPorCargo(@Param("cargoId") UUID cargoId,
                                              @Param("eleicaoId") UUID eleicaoId,
                                              @Param("limite") int limite);

    /**
     * Conta candidatos ativos por eleição e cargo
     */
    @Query("SELECT COUNT(c) FROM Candidato c WHERE c.cargoPretendidoId = :cargoId AND c.eleicaoId = :eleicaoId AND " +
            "c.ativo = true AND c.aprovado = true")
    long countCandidatosAtivosPorCargoEEleicao(@Param("cargoId") UUID cargoId, @Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca candidatos sem número definido
     */
    @Query("SELECT c FROM Candidato c WHERE c.eleicaoId = :eleicaoId AND c.ativo = true AND " +
            "(c.numeroCandidato IS NULL OR c.numeroCandidato = '') ORDER BY c.nomeCandidato")
    List<Candidato> findCandidatosSemNumero(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca últimos candidatos cadastrados
     */
    @Query("SELECT c FROM Candidato c ORDER BY c.createdAt DESC")
    List<Candidato> findUltimosCandidatosCadastrados(@Param("limite") int limite);

    // === CONSULTAS PARA ESTATÍSTICAS ===

    /**
     * Conta candidatos por status em uma eleição
     */
    @Query("SELECT c.aprovado, COUNT(c) FROM Candidato c WHERE c.eleicaoId = :eleicaoId AND c.ativo = true " +
            "GROUP BY c.aprovado")
    List<Object[]> countCandidatosPorStatusNaEleicao(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Conta candidatos por cargo em uma eleição
     */
    @Query("SELECT cp.nome, COUNT(c) FROM Candidato c JOIN c.cargoPretendido cp WHERE c.eleicaoId = :eleicaoId AND " +
            "c.ativo = true GROUP BY cp.nome ORDER BY cp.nome")
    List<Object[]> countCandidatosPorCargoNaEleicao(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca ranking de candidatos por cargo (ordenado por votos)
     */
    @Query("SELECT c FROM Candidato c WHERE c.cargoPretendidoId = :cargoId AND c.eleicaoId = :eleicaoId AND " +
            "c.ativo = true AND c.aprovado = true ORDER BY SIZE(c.votos) DESC, c.nomeCandidato ASC")
    List<Candidato> findRankingCandidatosPorCargo(@Param("cargoId") UUID cargoId, @Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca candidatos com foto de campanha
     */
    @Query("SELECT c FROM Candidato c WHERE c.eleicaoId = :eleicaoId AND c.ativo = true AND " +
            "c.fotoCampanhaData IS NOT NULL ORDER BY c.nomeCandidato")
    List<Candidato> findCandidatosComFoto(@Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca candidatos sem foto de campanha
     */
    @Query("SELECT c FROM Candidato c WHERE c.eleicaoId = :eleicaoId AND c.ativo = true AND " +
            "c.fotoCampanhaData IS NULL ORDER BY c.nomeCandidato")
    List<Candidato> findCandidatosSemFoto(@Param("eleicaoId") UUID eleicaoId);

    // === CONSULTAS DE VALIDAÇÃO DE REGRAS ===

    /**
     * Verifica se membro já é candidato em eleição ativa
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Candidato c JOIN c.eleicao e WHERE " +
            "c.membroId = :membroId AND e.ativa = true AND c.ativo = true")
    boolean membroJaEhCandidatoEmEleicaoAtiva(@Param("membroId") UUID membroId);

    /**
     * Conta candidaturas do membro por eleição
     */
    @Query("SELECT COUNT(c) FROM Candidato c WHERE c.membroId = :membroId AND c.eleicaoId = :eleicaoId")
    long countCandidaturasMembroPorEleicao(@Param("membroId") UUID membroId, @Param("eleicaoId") UUID eleicaoId);

    /**
     * Busca candidatos elegíveis para votação (todos os critérios atendidos)
     */
    @Query("SELECT c FROM Candidato c JOIN c.membro m JOIN c.cargoPretendido cp JOIN c.eleicao e WHERE " +
            "e.ativa = true AND c.ativo = true AND c.aprovado = true AND m.ativo = true AND cp.ativo = true " +
            "ORDER BY cp.nome, c.nomeCandidato")
    List<Candidato> findTodosCandidatosElegiveisParaVotacao();
}