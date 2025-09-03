package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Categoria;
import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import com.br.ibetelvote.domain.repositories.CargoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CargoJpaRepository extends JpaRepository<Cargo, UUID>, CargoRepository {

    // === CONSULTAS BÁSICAS HERDADAS DO JPA ===
    // findById, findAll, save, delete, etc. são automáticos

    // === CONSULTAS POR NOME ===

    @Query("SELECT c FROM Cargo c WHERE UPPER(TRIM(CAST(c.nome AS string))) = UPPER(TRIM(:nome))")
    Optional<Cargo> findByNome(@Param("nome") String nome);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cargo c WHERE UPPER(TRIM(CAST(c.nome AS string))) = UPPER(TRIM(:nome))")
    boolean existsByNome(@Param("nome") String nome);

    @Query("SELECT c FROM Cargo c WHERE UPPER(CAST(c.nome AS string)) LIKE UPPER(CONCAT('%', :nome, '%'))")
    List<Cargo> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cargo c WHERE UPPER(TRIM(CAST(c.nome AS string))) = UPPER(TRIM(:nome)) AND c.id != :id")
    boolean existsByNomeAndIdNot(@Param("nome") String nome, @Param("id") UUID id);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cargo c WHERE UPPER(CAST(c.nome AS string)) = UPPER(:nome) AND c.ativo = true")
    boolean existsCargoAtivoByNome(@Param("nome") String nome);

    // === CONSULTAS POR STATUS ===

    List<Cargo> findByAtivoTrue();
    List<Cargo> findByAtivoFalse();
    Page<Cargo> findByAtivo(Boolean ativo, Pageable pageable);
    long countByAtivo(Boolean ativo);

    // Disponibilidade eleitoral
    List<Cargo> findByDisponivelEleicaoTrue();
    List<Cargo> findByAtivoTrueAndDisponivelEleicaoTrue();
    long countByDisponivelEleicao(Boolean disponivel);

    // === CONSULTAS ORDENADAS ===

    List<Cargo> findAllByOrderByNomeAsc();
    List<Cargo> findByAtivoTrueOrderByNomeAsc();
    Page<Cargo> findByAtivoTrueOrderByNomeAsc(Pageable pageable);

    // === CONSULTAS POR CATEGORIA ===

    List<Cargo> findByCategoria(Categoria categoria);

    @Query("SELECT c FROM Cargo c WHERE c.categoria.id = :categoriaId")
    List<Cargo> findByCategoriaId(@Param("categoriaId") UUID categoriaId);

    List<Cargo> findByCategoriaAndAtivoTrue(Categoria categoria);

    Page<Cargo> findByCategoria(Categoria categoria, Pageable pageable);

    @Query("SELECT c FROM Cargo c WHERE c.categoria.id = :categoriaId")
    Page<Cargo> findByCategoriaId(@Param("categoriaId") UUID categoriaId, Pageable pageable);


    long countByCategoria(Categoria categoria);

    @Query("SELECT COUNT(c) FROM Cargo c WHERE c.categoria.id = :categoriaId")
    long countByCategoriaId(@Param("categoriaId") UUID categoriaId);

    @Query("SELECT c FROM Cargo c WHERE c.categoria.id = :categoriaId AND c.ativo = true")
    List<Cargo> findByCategoriaIdAndAtivoTrue(@Param("categoriaId") UUID categoriaId);


    // Consultas por categoria com ordenação
    @Query("SELECT c FROM Cargo c WHERE c.categoria = :categoria ORDER BY c.ordemPrecedencia ASC NULLS LAST, c.nome ASC")
    List<Cargo> findByCategoriaOrderByOrdemPrecedenciaAscNomeAsc(@Param("categoria") Categoria categoria);

    @Query("SELECT c FROM Cargo c WHERE c.categoria.id = :categoriaId ORDER BY c.ordemPrecedencia ASC NULLS LAST, c.nome ASC")
    List<Cargo> findByCategoriaIdOrderByOrdemPrecedenciaAscNomeAsc(@Param("categoriaId") UUID categoriaId);

    @Query("SELECT c FROM Cargo c WHERE c.categoria = :categoria AND c.ativo = true ORDER BY c.ordemPrecedencia ASC NULLS LAST, c.nome ASC")
    List<Cargo> findByCategoriaAndAtivoTrueOrderByOrdemPrecedenciaAscNomeAsc(@Param("categoria") Categoria categoria);

    // === CONSULTAS POR HIERARQUIA ===

    List<Cargo> findByHierarquia(HierarquiaCargo hierarquia);
    List<Cargo> findByHierarquiaAndAtivoTrue(HierarquiaCargo hierarquia);
    Page<Cargo> findByHierarquia(HierarquiaCargo hierarquia, Pageable pageable);
    long countByHierarquia(HierarquiaCargo hierarquia);

    // Hierarquia com categoria
    List<Cargo> findByCategoriaAndHierarquia(Categoria categoria, HierarquiaCargo hierarquia);

    @Query("SELECT c FROM Cargo c WHERE c.categoria.id = :categoriaId AND c.hierarquia = :hierarquia")
    List<Cargo> findByCategoriaIdAndHierarquia(@Param("categoriaId") UUID categoriaId, @Param("hierarquia") HierarquiaCargo hierarquia);


    // Múltiplas hierarquias
    List<Cargo> findByHierarquiaIn(List<HierarquiaCargo> hierarquias);
    List<Cargo> findByHierarquiaInAndAtivoTrue(List<HierarquiaCargo> hierarquias);

    // === CONSULTAS POR ORDEM DE PRECEDÊNCIA ===

    List<Cargo> findByOrdemPrecedenciaBetween(Integer ordemMin, Integer ordemMax);
    Optional<Cargo> findByCategoriaAndOrdemPrecedencia(Categoria categoria, Integer ordemPrecedencia);
    boolean existsByCategoriaAndOrdemPrecedencia(Categoria categoria, Integer ordemPrecedencia);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cargo c WHERE c.categoria.id = :categoriaId AND c.ordemPrecedencia = :ordemPrecedencia")
    boolean existsByCategoriaIdAndOrdemPrecedencia(@Param("categoriaId") UUID categoriaId, @Param("ordemPrecedencia") Integer ordemPrecedencia);

    boolean existsByCategoriaAndOrdemPrecedenciaAndIdNot(Categoria categoria, Integer ordemPrecedencia, UUID id);

    @Query("SELECT c FROM Cargo c WHERE c.categoria.id = :categoriaId AND c.ordemPrecedencia = :ordemPrecedencia")
    Optional<Cargo> findByCategoriaIdAndOrdemPrecedencia(@Param("categoriaId") UUID categoriaId,
                                                         @Param("ordemPrecedencia") Integer ordemPrecedencia);

    // Próxima ordem disponível
    @Query("SELECT COALESCE(MAX(c.ordemPrecedencia), 0) + 1 FROM Cargo c WHERE c.categoria = :categoria")
    Integer findNextOrdemPrecedenciaByCategoria(@Param("categoria") Categoria categoria);

    @Query("SELECT COALESCE(MAX(c.ordemPrecedencia), 0) + 1 FROM Cargo c WHERE c.categoria.id = :categoriaId")
    Integer findNextOrdemPrecedenciaByCategoriaId(@Param("categoriaId") UUID categoriaId);

    // Validação de ordem
    @Query("SELECT CASE WHEN COUNT(c) = 0 THEN true ELSE false END FROM Cargo c WHERE c.categoria.id = :categoriaId AND c.ordemPrecedencia = :ordem")
    boolean isOrdemPrecedenciaDisponivel(@Param("categoriaId") UUID categoriaId, @Param("ordem") Integer ordem);

    @Query("SELECT CASE WHEN COUNT(c) = 0 THEN true ELSE false END FROM Cargo c WHERE c.categoria.id = :categoriaId AND c.ordemPrecedencia = :ordem AND c.id != :cargoId")
    boolean isOrdemPrecedenciaDisponivel(@Param("categoriaId") UUID categoriaId, @Param("ordem") Integer ordem, @Param("cargoId") UUID cargoId);

    // === CONSULTAS PARA ELEGIBILIDADE ===

    /**
     * Busca cargos elegíveis para determinada hierarquia
     */
    @Query("SELECT c FROM Cargo c WHERE c.ativo = true AND c.hierarquia <= :hierarquia ORDER BY c.hierarquia, c.nome")
    List<Cargo> findCargosElegiveisParaHierarquia(@Param("hierarquia") HierarquiaCargo hierarquia);

    @Query(value = """
        SELECT c.* FROM cargos c 
        WHERE c.ativo = true 
        AND jsonb_exists(c.elegibilidade, :elegibilidade)
        """, nativeQuery = true)
    List<Cargo> findCargosPorElegibilidade(@Param("elegibilidade") String elegibilidade);

    @Query(value = """
        SELECT c.* FROM cargos c 
        WHERE c.ativo = true 
        AND c.disponivel_eleicao = true
        AND jsonb_exists(c.elegibilidade, :elegibilidade)
        """, nativeQuery = true)
    List<Cargo> findCargosQuePermitemElegibilidade(@Param("elegibilidade") String elegibilidade);

    /**
     * Verifica se um cargo pode candidatar-se a outro
     */
    @Query(value = """
        SELECT CASE WHEN COUNT(c2) > 0 THEN true ELSE false END 
        FROM cargos c1, cargos c2 
        WHERE c1.id = :cargoOrigemId 
        AND c2.id = :cargoDestinoId 
        AND c1.hierarquia <= c2.hierarquia
        AND jsonb_exists(c2.elegibilidade, c1.hierarquia::text)
        """, nativeQuery = true)
    boolean verificarElegibilidade(@Param("cargoOrigemId") UUID cargoOrigemId, @Param("cargoDestinoId") UUID cargoDestinoId);

    // === CONSULTAS ESPECÍFICAS COMBINADAS ===

    /**
     * Busca cargos disponíveis para eleições (completo)
     */
    @Query("""
            SELECT c FROM Cargo c 
            WHERE c.ativo = true 
            AND c.disponivelEleicao = true 
            AND c.nome IS NOT NULL 
            AND c.descricao IS NOT NULL 
            AND c.categoria IS NOT NULL 
            ORDER BY c.categoria.ordemExibicao, c.ordemPrecedencia NULLS LAST, c.nome
            """)
    List<Cargo> findCargosDisponiveis();

    /**
     * Busca cargos disponíveis com paginação
     */
    @Query("""
            SELECT c FROM Cargo c 
            WHERE c.ativo = true 
            AND c.disponivelEleicao = true 
            AND c.nome IS NOT NULL 
            AND c.descricao IS NOT NULL 
            AND c.categoria IS NOT NULL 
            ORDER BY c.categoria.ordemExibicao, c.ordemPrecedencia NULLS LAST, c.nome
            """)
    Page<Cargo> findCargosDisponiveis(Pageable pageable);

    /**
     * Conta cargos disponíveis
     */
    @Query("""
            SELECT COUNT(c) FROM Cargo c 
            WHERE c.ativo = true 
            AND c.disponivelEleicao = true 
            AND c.nome IS NOT NULL 
            AND c.descricao IS NOT NULL 
            AND c.categoria IS NOT NULL
            """)
    long countCargosDisponiveis();

    /**
     * Cargos disponíveis por categoria
     */
    @Query("""
            SELECT c FROM Cargo c 
            WHERE c.categoria = :categoria 
            AND c.ativo = true 
            AND c.disponivelEleicao = true 
            ORDER BY c.ordemPrecedencia NULLS LAST, c.nome
            """)
    List<Cargo> findCargosDisponiveisByCategoria(@Param("categoria") Categoria categoria);

    @Query("""
            SELECT c FROM Cargo c 
            WHERE c.categoria.id = :categoriaId 
            AND c.ativo = true 
            AND c.disponivelEleicao = true 
            ORDER BY c.ordemPrecedencia NULLS LAST, c.nome
            """)
    List<Cargo> findCargosDisponiveisByCategoriaId(@Param("categoriaId") UUID categoriaId);

    // === CONSULTAS POR TIPOS DE CARGO ===

    /**
     * Cargos ministeriais (Pastoral, Presbiteral, Diaconal)
     */
    @Query("SELECT c FROM Cargo c WHERE c.hierarquia IN ('PASTORAL', 'PRESBITERAL', 'DIACONAL') ORDER BY c.hierarquia, c.nome")
    List<Cargo> findCargosMinisteriais();

    @Query("SELECT c FROM Cargo c WHERE c.ativo = true AND c.hierarquia IN ('PASTORAL', 'PRESBITERAL', 'DIACONAL') ORDER BY c.hierarquia, c.nome")
    List<Cargo> findCargosMinisteriaisAtivos();

    /**
     * Cargos de liderança (Pastoral, Presbiteral, Liderança)
     */
    @Query("SELECT c FROM Cargo c WHERE c.hierarquia IN ('PASTORAL', 'PRESBITERAL', 'LIDERANCA') ORDER BY c.hierarquia, c.nome")
    List<Cargo> findCargosLideranca();

    @Query("SELECT c FROM Cargo c WHERE c.ativo = true AND c.hierarquia IN ('PASTORAL', 'PRESBITERAL', 'LIDERANCA') ORDER BY c.hierarquia, c.nome")
    List<Cargo> findCargosLiderancaAtivos();

    // === CONSULTAS PARA RELATÓRIOS ===

    @Query("SELECT c FROM Cargo c ORDER BY c.createdAt DESC")
    List<Cargo> findTop10ByOrderByCreatedAtDesc();

    /**
     * Conta cargos por categoria
     */
    @Query("SELECT cat.nome, COUNT(c) FROM Categoria cat LEFT JOIN cat.cargos c GROUP BY cat.id, cat.nome ORDER BY COUNT(c) DESC")
    List<Object[]> countCargosPorCategoria();

    /**
     * Conta cargos por hierarquia
     */
    @Query("SELECT c.hierarquia, COUNT(c) FROM Cargo c GROUP BY c.hierarquia ORDER BY c.hierarquia")
    List<Object[]> countCargosPorHierarquia();

    /**
     * Estatísticas completas
     */
    @Query("""
            SELECT cat.nome, c.hierarquia, COUNT(c),
                   COUNT(CASE WHEN c.ativo = true THEN 1 END),
                   COUNT(CASE WHEN c.ativo = true AND c.disponivelEleicao = true THEN 1 END)
            FROM Categoria cat 
            LEFT JOIN cat.cargos c 
            GROUP BY cat.id, cat.nome, c.hierarquia 
            ORDER BY cat.ordemExibicao, c.hierarquia
            """)
    List<Object[]> getEstatisticasCompletas();

    /**
     * Relatório hierarquia por categoria
     */
    @Query("""
            SELECT cat.nome, c.hierarquia, c.nome, c.ativo, c.disponivelEleicao
            FROM Categoria cat 
            JOIN cat.cargos c 
            ORDER BY cat.ordemExibicao, c.hierarquia, c.ordemPrecedencia NULLS LAST, c.nome
            """)
    List<Object[]> getRelatorioHierarquiaPorCategoria();

    // === CONSULTAS COM FILTROS AVANÇADOS ===

    /**
     * Busca com filtros múltiplos
     */
    @Query("""
            SELECT c FROM Cargo c 
            JOIN c.categoria cat
            WHERE
            (:nome IS NULL OR UPPER(CAST(c.nome AS string)) LIKE UPPER(CONCAT('%', :nome, '%'))) AND
            (:categoriaId IS NULL OR c.categoria.id = :categoriaId) AND
            (:hierarquia IS NULL OR c.hierarquia = :hierarquia) AND
            (:ativo IS NULL OR c.ativo = :ativo) AND
            (:disponivelEleicao IS NULL OR c.disponivelEleicao = :disponivelEleicao)
            ORDER BY cat.ordemExibicao, c.ordemPrecedencia ASC NULLS LAST, c.nome
            """)
    Page<Cargo> findByFiltros(@Param("nome") String nome,
                              @Param("categoriaId") UUID categoriaId,
                              @Param("hierarquia") HierarquiaCargo hierarquia,
                              @Param("ativo") Boolean ativo,
                              @Param("disponivelEleicao") Boolean disponivelEleicao,
                              Pageable pageable);

    /**
     * Busca por múltiplos critérios
     */
    @Query("""
            SELECT c FROM Cargo c WHERE
            (:categoriaIds IS NULL OR c.categoria.id IN :categoriaIds) AND
            (:hierarquias IS NULL OR c.hierarquia IN :hierarquias) AND
            (:ativo IS NULL OR c.ativo = :ativo) AND
            (:disponivelEleicao IS NULL OR c.disponivelEleicao = :disponivelEleicao)
            ORDER BY c.categoria.ordemExibicao, c.ordemPrecedencia NULLS LAST, c.nome
            """)
    List<Cargo> findByMultiplosCriterios(@Param("categoriaIds") List<UUID> categoriaIds,
                                         @Param("hierarquias") List<HierarquiaCargo> hierarquias,
                                         @Param("ativo") Boolean ativo,
                                         @Param("disponivelEleicao") Boolean disponivelEleicao);

    // === CONSULTAS PARA VALIDAÇÃO ===

    /**
     * Verifica se cargo pode ser deletado (sem candidatos - implementar futuramente)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cargo c WHERE c.id = :id")
    boolean canDeleteCargo(@Param("id") UUID id);

    /**
     * Lista cargos com candidatos (implementar quando tiver módulo de candidatos)
     */
    @Query("SELECT c FROM Cargo c WHERE c.id IN (SELECT DISTINCT cand.cargoPretendidoId FROM Candidato cand)")
    List<Cargo> findCargosComCandidatos();

    /**
     * Busca cargos com informações incompletas
     */
    @Query("""
        SELECT c FROM Cargo c WHERE
        c.nome IS NULL OR c.nome = '' OR
        c.descricao IS NULL OR c.descricao = '' OR
        c.categoria IS NULL OR
        c.hierarquia IS NULL OR
        c.elegibilidade IS NULL OR SIZE(c.elegibilidade) = 0
        """)
    List<Cargo> findCargosIncompletos();

    // === CONSULTAS ESPECÍFICAS DO CONTEXTO ECLESIÁSTICO ===

    /**
     * Cargos do conselho eclesiástico (por nome da categoria)
     */
    @Query("SELECT c FROM Cargo c WHERE c.categoria.nome = 'Conselho Eclesiástico' ORDER BY c.ordemPrecedencia NULLS LAST, c.nome")
    List<Cargo> findCargosConselhoEclesiastico();

    /**
     * Cargos administrativos
     */
    @Query("SELECT c FROM Cargo c WHERE c.hierarquia = 'ADMINISTRATIVO' ORDER BY c.categoria.ordemExibicao, c.ordemPrecedencia NULLS LAST, c.nome")
    List<Cargo> findCargosAdministrativos();

    /**
     * Cargos por faixa de hierarquia (baseado na ordem)
     */
    @Query("""
            SELECT c FROM Cargo c WHERE
            (:hierarquiaMin IS NULL OR c.hierarquia >= :hierarquiaMin) AND
            (:hierarquiaMax IS NULL OR c.hierarquia <= :hierarquiaMax)
            ORDER BY c.hierarquia, c.categoria.ordemExibicao, c.nome
            """)
    List<Cargo> findCargosByFaixaHierarquia(@Param("hierarquiaMin") HierarquiaCargo hierarquiaMin,
                                            @Param("hierarquiaMax") HierarquiaCargo hierarquiaMax);

    /**
     * Cargos que podem eleger para determinado cargo (baseado na hierarquia e elegibilidade)
     */
    @Query(value = """
        SELECT c1.* FROM cargos c1, cargos c2 WHERE
        c2.id = :cargoDestinoId AND
        c1.ativo = true AND
        c1.hierarquia <= c2.hierarquia AND
        jsonb_exists(c2.elegibilidade, c1.hierarquia::text)
        ORDER BY c1.hierarquia, c1.nome
        """, nativeQuery = true)
    List<Cargo> findCargosQuePodemElegerPara(@Param("cargoDestinoId") UUID cargoDestinoId);

    // === CONSULTAS ADICIONAIS ÚTEIS ===

    /**
     * Busca cargos por período de criação
     */
    @Query("SELECT c FROM Cargo c WHERE c.createdAt BETWEEN :inicio AND :fim ORDER BY c.createdAt DESC")
    List<Cargo> findByCreatedAtBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    /**
     * Busca cargos mais recentes por categoria
     */
    @Query("SELECT c FROM Cargo c WHERE c.categoria.id = :categoriaId ORDER BY c.createdAt DESC")
    List<Cargo> findRecentesByCategoria(@Param("categoriaId") UUID categoriaId);

    /**
     * Conta cargos ativos por categoria
     */
    @Query("SELECT COUNT(c) FROM Cargo c WHERE c.categoria.id = :categoriaId AND c.ativo = true")
    long countAtivosByCategoria(@Param("categoriaId") UUID categoriaId);

    /**
     * Busca cargos sem ordem de precedência
     */
    @Query("SELECT c FROM Cargo c WHERE c.ordemPrecedencia IS NULL ORDER BY c.categoria.ordemExibicao, c.nome")
    List<Cargo> findCargosSemOrdemPrecedencia();

    /**
     * Busca cargos duplicados por nome (case insensitive)
     */
    @Query("""
            SELECT c FROM Cargo c WHERE UPPER(TRIM(c.nome)) IN (
                SELECT UPPER(TRIM(c2.nome)) FROM Cargo c2 
                GROUP BY UPPER(TRIM(c2.nome)) 
                HAVING COUNT(c2) > 1
            ) ORDER BY c.nome, c.id
            """)
    List<Cargo> findCargosDuplicados();

    /**
     * Busca próximos cargos a serem atualizados (sem atualização há muito tempo)
     */
    @Query("SELECT c FROM Cargo c WHERE c.updatedAt < :dataLimite ORDER BY c.updatedAt ASC")
    List<Cargo> findCargosDesatualizados(@Param("dataLimite") LocalDateTime dataLimite);

    /**
     * Estatísticas de elegibilidade
     */
    @Query(value = """
            SELECT 
                eligibility_item,
                COUNT(*) as total_cargos
            FROM (
                SELECT 
                    jsonb_array_elements_text(c.elegibilidade) as eligibility_item
                FROM cargos c 
                WHERE c.ativo = true 
                AND c.elegibilidade IS NOT NULL
            ) as eligibilities
            GROUP BY eligibility_item
            ORDER BY total_cargos DESC
            """, nativeQuery = true)
    List<Object[]> getEstatisticasElegibilidade();

    /**
     * Verifica conflitos de ordem de precedência na categoria
     */
    @Query("""
            SELECT c FROM Cargo c WHERE c.categoria.id = :categoriaId 
            AND c.ordemPrecedencia IN (
                SELECT c2.ordemPrecedencia FROM Cargo c2 
                WHERE c2.categoria.id = :categoriaId 
                AND c2.ordemPrecedencia IS NOT NULL
                GROUP BY c2.ordemPrecedencia 
                HAVING COUNT(c2) > 1
            )
            ORDER BY c.ordemPrecedencia, c.nome
            """)
    List<Cargo> findConflitosOrdemPrecedencia(@Param("categoriaId") UUID categoriaId);

    /**
     * Busca gaps na numeração de ordem de precedência
     */
    @Query(value = """
            SELECT DISTINCT c.ordem_precedencia + 1 as gap_start
            FROM cargos c 
            WHERE c.categoria_id = :categoriaId
            AND c.ordem_precedencia IS NOT NULL
            AND NOT EXISTS (
                SELECT 1 FROM cargos c2 
                WHERE c2.categoria_id = :categoriaId 
                AND c2.ordem_precedencia = c.ordem_precedencia + 1
            )
            AND c.ordem_precedencia < (
                SELECT MAX(c3.ordem_precedencia) 
                FROM cargos c3 
                WHERE c3.categoria_id = :categoriaId
            )
            ORDER BY gap_start
            """, nativeQuery = true)
    List<Integer> findGapsOrdemPrecedencia(@Param("categoriaId") UUID categoriaId);

    /**
     * Busca cargos ordenados para reorganização de precedência
     */
    @Query("""
            SELECT c FROM Cargo c 
            WHERE c.categoria.id = :categoriaId 
            ORDER BY 
                CASE WHEN c.ordemPrecedencia IS NULL THEN 1 ELSE 0 END,
                c.ordemPrecedencia ASC,
                c.nome ASC
            """)
    List<Cargo> findCargosParaReorganizacao(@Param("categoriaId") UUID categoriaId);

    @Query(value = """
        SELECT c.* FROM cargos c 
        WHERE c.ativo = true 
        AND c.disponivel_eleicao = true
        AND jsonb_exists(c.elegibilidade, :nivelMembro)
        ORDER BY c.hierarquia, c.nome
        """, nativeQuery = true)
    List<Cargo> findCargosElegiveisParaMembro(@Param("nivelMembro") String nivelMembro);


}