package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Voto;
import com.br.ibetelvote.domain.entities.enums.TipoVoto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VotoRepository {

    // === OPERAÇÕES BÁSICAS ===
    void deleteById(UUID id);
    long count();

    // === CONSULTAS POR ELEIÇÃO ===
    List<Voto> findByEleicaoId(UUID eleicaoId);
    Page<Voto> findByEleicaoId(UUID eleicaoId, Pageable pageable);
    long countByEleicaoId(UUID eleicaoId);
    List<Voto> findByEleicaoIdOrderByDataVotoAsc(UUID eleicaoId);

    // === CONSULTAS POR CARGO PRETENDIDO ===
    List<Voto> findByCargoPretendidoId(UUID cargoPretendidoId);
    Page<Voto> findByCargoPretendidoId(UUID cargoPretendidoId, Pageable pageable);
    long countByCargoPretendidoId(UUID cargoPretendidoId);
    List<Voto> findByEleicaoIdAndCargoPretendidoId(UUID eleicaoId, UUID cargoPretendidoId);

    // === CONSULTAS POR CANDIDATO ===
    List<Voto> findByCandidatoId(UUID candidatoId);
    Page<Voto> findByCandidatoId(UUID candidatoId, Pageable pageable);
    long countByCandidatoId(UUID candidatoId);
    List<Voto> findByCandidatoIdOrderByDataVotoAsc(UUID candidatoId);

    // === CONSULTAS POR MEMBRO ===
    List<Voto> findByMembroId(UUID membroId);
    Page<Voto> findByMembroId(UUID membroId, Pageable pageable);
    long countByMembroId(UUID membroId);
    List<Voto> findByEleicaoIdAndMembroId(UUID eleicaoId, UUID membroId);

    // === VALIDAÇÕES DE VOTO ÚNICO ===
    Optional<Voto> findByMembroIdAndCargoPretendidoIdAndEleicaoId(UUID membroId, UUID cargoPretendidoId, UUID eleicaoId);
    boolean existsByMembroIdAndCargoPretendidoIdAndEleicaoId(UUID membroId, UUID cargoPretendidoId, UUID eleicaoId);
    boolean existsByMembroIdAndEleicaoId(UUID membroId, UUID eleicaoId);

    // === CONSULTAS POR TIPO DE VOTO (USANDO ENUM) ===
    List<Voto> findByTipoVoto(TipoVoto tipoVoto);
    List<Voto> findByEleicaoIdAndTipoVoto(UUID eleicaoId, TipoVoto tipoVoto);
    List<Voto> findByCargoPretendidoIdAndTipoVoto(UUID cargoPretendidoId, TipoVoto tipoVoto);

    long countByTipoVoto(TipoVoto tipoVoto);
    long countByEleicaoIdAndTipoVoto(UUID eleicaoId, TipoVoto tipoVoto);
    long countByCargoPretendidoIdAndTipoVoto(UUID cargoPretendidoId, TipoVoto tipoVoto);

    // === CONSULTAS POR PERÍODO ===
    List<Voto> findByDataVotoBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Voto> findByEleicaoIdAndDataVotoBetween(UUID eleicaoId, LocalDateTime inicio, LocalDateTime fim);
    Page<Voto> findByDataVotoBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    // === CONSULTAS POR HASH E IP (PARA AUDITORIA) ===
    List<Voto> findByHashVotoContaining(String hashFragment);
    List<Voto> findByIpOrigemContaining(String ipPattern);

    // === MÉTODOS DE CONVENIÊNCIA COM DEFAULT (NÃO PRECISAM SER IMPLEMENTADOS) ===

    default List<Voto> findVotosBrancos() {
        return findByTipoVoto(TipoVoto.BRANCO);
    }

    default List<Voto> findVotosNulos() {
        return findByTipoVoto(TipoVoto.NULO);
    }

    default List<Voto> findVotosValidos() {
        return findByTipoVoto(TipoVoto.CANDIDATO);
    }

    default List<Voto> findVotosValidosByEleicao(UUID eleicaoId) {
        return findByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.CANDIDATO);
    }

    default List<Voto> findVotosValidosByCargoPretendido(UUID cargoPretendidoId) {
        return findByCargoPretendidoIdAndTipoVoto(cargoPretendidoId, TipoVoto.CANDIDATO);
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

    default long countVotosValidosByEleicao(UUID eleicaoId) {
        return countByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.CANDIDATO);
    }

    default long countVotosValidosByCargoPretendido(UUID cargoPretendidoId) {
        return countByCargoPretendidoIdAndTipoVoto(cargoPretendidoId, TipoVoto.CANDIDATO);
    }

    // === MÉTODOS COMPLEXOS (IMPLEMENTADOS NO JPAREPO COM @QUERY) ===
    // Estes métodos devem ser implementados no VotoJpaRepository com anotações @Query

    long countDistinctMembroByEleicaoId(UUID eleicaoId);
    List<Object[]> countVotosByCandidatoAndCargo(UUID eleicaoId);
    List<Object[]> countVotosByHora(UUID eleicaoId);
    List<Object[]> findRankingCandidatosPorVotos(UUID eleicaoId, UUID cargoPretendidoId);
    List<Object[]> findVotosSuspeitos(UUID eleicaoId);
    List<Object[]> countVotosPorIpOrigem(UUID eleicaoId);
    List<Object[]> getParticipacaoPorCargoMembro(UUID eleicaoId);

    List<Voto> findVotosParaAuditoria(UUID eleicaoId);
    List<Voto> findVotosComDadosIncompletos();
    List<Voto> findVotosComHashDuplicado();
    List<Voto> findUltimosVotosRegistrados(int limite);
    List<Voto> findVotosSequenciaisRapidos(UUID eleicaoId, int segundosIntervalo);

    boolean existsVotoDuplicado(UUID membroId, UUID candidatoId, UUID eleicaoId);

    // === MÉTODOS DEPRECATED PARA COMPATIBILIDADE ===

    /**
     * @deprecated Usar findByTipoVoto(TipoVoto.BRANCO)
     */
    @Deprecated
    default List<Voto> findByVotoBrancoTrue() {
        return findByTipoVoto(TipoVoto.BRANCO);
    }

    /**
     * @deprecated Usar findByTipoVoto(TipoVoto.NULO)
     */
    @Deprecated
    default List<Voto> findByVotoNuloTrue() {
        return findByTipoVoto(TipoVoto.NULO);
    }

    /**
     * @deprecated Usar findByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.BRANCO)
     */
    @Deprecated
    default List<Voto> findByEleicaoIdAndVotoBrancoTrue(UUID eleicaoId) {
        return findByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.BRANCO);
    }

    /**
     * @deprecated Usar findByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.NULO)
     */
    @Deprecated
    default List<Voto> findByEleicaoIdAndVotoNuloTrue(UUID eleicaoId) {
        return findByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.NULO);
    }

    /**
     * @deprecated Usar findByCargoPretendidoIdAndTipoVoto(cargoPretendidoId, TipoVoto.BRANCO)
     */
    @Deprecated
    default List<Voto> findByCargoPretendidoIdAndVotoBrancoTrue(UUID cargoPretendidoId) {
        return findByCargoPretendidoIdAndTipoVoto(cargoPretendidoId, TipoVoto.BRANCO);
    }

    /**
     * @deprecated Usar findByCargoPretendidoIdAndTipoVoto(cargoPretendidoId, TipoVoto.NULO)
     */
    @Deprecated
    default List<Voto> findByCargoPretendidoIdAndVotoNuloTrue(UUID cargoPretendidoId) {
        return findByCargoPretendidoIdAndTipoVoto(cargoPretendidoId, TipoVoto.NULO);
    }

    /**
     * @deprecated Usar countByTipoVoto(TipoVoto.BRANCO)
     */
    @Deprecated
    default long countByVotoBrancoTrue() {
        return countByTipoVoto(TipoVoto.BRANCO);
    }

    /**
     * @deprecated Usar countByTipoVoto(TipoVoto.NULO)
     */
    @Deprecated
    default long countByVotoNuloTrue() {
        return countByTipoVoto(TipoVoto.NULO);
    }

    /**
     * @deprecated Usar countByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.BRANCO)
     */
    @Deprecated
    default long countByEleicaoIdAndVotoBrancoTrue(UUID eleicaoId) {
        return countByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.BRANCO);
    }

    /**
     * @deprecated Usar countByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.NULO)
     */
    @Deprecated
    default long countByEleicaoIdAndVotoNuloTrue(UUID eleicaoId) {
        return countByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.NULO);
    }

    /**
     * @deprecated Usar countByCargoPretendidoIdAndTipoVoto(cargoPretendidoId, TipoVoto.BRANCO)
     */
    @Deprecated
    default long countByCargoPretendidoIdAndVotoBrancoTrue(UUID cargoPretendidoId) {
        return countByCargoPretendidoIdAndTipoVoto(cargoPretendidoId, TipoVoto.BRANCO);
    }

    /**
     * @deprecated Usar countByCargoPretendidoIdAndTipoVoto(cargoPretendidoId, TipoVoto.NULO)
     */
    @Deprecated
    default long countByCargoPretendidoIdAndVotoNuloTrue(UUID cargoPretendidoId) {
        return countByCargoPretendidoIdAndTipoVoto(cargoPretendidoId, TipoVoto.NULO);
    }



}