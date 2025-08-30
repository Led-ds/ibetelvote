package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Categoria;
import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import com.br.ibetelvote.domain.repositories.CategoriaRepository;
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
public interface CategoriaJpaRepository extends JpaRepository<Categoria, UUID>, CategoriaRepository {

    @Query("SELECT c FROM Categoria c WHERE UPPER(TRIM(c.nome)) = UPPER(TRIM(:nome))")
    Optional<Categoria> findByNome(@Param("nome") String nome);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Categoria c WHERE UPPER(TRIM(c.nome)) = UPPER(TRIM(:nome))")
    boolean existsByNome(@Param("nome") String nome);

    @Query("SELECT c FROM Categoria c WHERE UPPER(c.nome) LIKE UPPER(CONCAT('%', :nome, '%'))")
    List<Categoria> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Categoria c WHERE UPPER(TRIM(c.nome)) = UPPER(TRIM(:nome)) AND c.id != :id")
    boolean existsByNomeAndIdNot(@Param("nome") String nome, @Param("id") UUID id);

    @Query("SELECT c FROM Categoria c WHERE UPPER(TRIM(c.nome)) = UPPER(TRIM(:nome))")
    Optional<Categoria> findByNomeIgnoreCaseAndTrimmed(@Param("nome") String nome);

    // === CONSULTAS POR STATUS ===

    List<Categoria> findByAtivoTrue();
    List<Categoria> findByAtivoFalse();
    Page<Categoria> findByAtivo(Boolean ativo, Pageable pageable);
    long countByAtivo(Boolean ativo);

    // === CONSULTAS ORDENADAS ===

    List<Categoria> findAllByOrderByNomeAsc();
    List<Categoria> findAllByOrderByOrdemExibicaoAsc();
    List<Categoria> findByAtivoTrueOrderByOrdemExibicaoAsc();
    Page<Categoria> findByAtivoTrueOrderByOrdemExibicaoAsc(Pageable pageable);

    // === CONSULTAS ESPECÍFICAS ===

    /**
     * Busca categorias que possuem pelo menos um cargo
     */
    @Query("SELECT DISTINCT c FROM Categoria c JOIN c.cargos ca WHERE ca.id IS NOT NULL")
    List<Categoria> findCategoriasComCargos();

    /**
     * Busca categorias sem nenhum cargo
     */
    @Query("SELECT c FROM Categoria c WHERE c.id NOT IN (SELECT DISTINCT ca.categoria.id FROM Cargo ca WHERE ca.categoria.id IS NOT NULL)")
    List<Categoria> findCategoriasSemCargos();

    /**
     * Busca categorias que possuem cargos ativos
     */
    @Query("SELECT DISTINCT c FROM Categoria c JOIN c.cargos ca WHERE ca.ativo = true")
    List<Categoria> findCategoriasComCargosAtivos();

    /**
     * Busca categorias com cargos disponíveis para eleições
     */
    @Query("SELECT DISTINCT c FROM Categoria c JOIN c.cargos ca WHERE ca.ativo = true AND ca.disponivelEleicao = true")
    List<Categoria> findCategoriasComCargosDisponiveis();

    /**
     * Busca próxima ordem de exibição disponível
     */
    @Query("SELECT COALESCE(MAX(c.ordemExibicao), 0) + 1 FROM Categoria c")
    Integer findNextOrdemExibicao();

    /**
     * Verifica se ordem de exibição está em uso
     */
    boolean existsByOrdemExibicao(Integer ordemExibicao);

    /**
     * Verifica se ordem está em uso por categoria diferente
     */
    boolean existsByOrdemExibicaoAndIdNot(Integer ordemExibicao, UUID id);

    // === CONSULTAS PARA RELATÓRIOS ===

    /**
     * Busca categorias criadas recentemente
     */
    @Query("SELECT c FROM Categoria c ORDER BY c.createdAt DESC")
    List<Categoria> findTop10ByOrderByCreatedAtDesc();

    /**
     * Busca categorias ordenadas por quantidade de cargos (desc)
     */
    @Query("SELECT c FROM Categoria c LEFT JOIN c.cargos ca GROUP BY c ORDER BY COUNT(ca) DESC")
    List<Categoria> findCategoriasComMaisCargos();

    /**
     * Conta cargos por categoria
     */
    @Query("SELECT c.nome, COUNT(ca) FROM Categoria c LEFT JOIN c.cargos ca GROUP BY c.id, c.nome ORDER BY COUNT(ca) DESC")
    List<Object[]> countCargosPorCategoria();

    /**
     * Estatísticas completas de categorias
     */
    @Query("""
            SELECT c.nome, 
                   COUNT(ca) as totalCargos,
                   COUNT(CASE WHEN ca.ativo = true THEN 1 END) as cargosAtivos,
                   COUNT(CASE WHEN ca.ativo = true AND ca.disponivelEleicao = true THEN 1 END) as cargosDisponiveis
            FROM Categoria c 
            LEFT JOIN c.cargos ca 
            GROUP BY c.id, c.nome 
            ORDER BY c.ordemExibicao, c.nome
            """)
    List<Object[]> getEstatisticasCategorias();

    // === CONSULTAS PARA VALIDAÇÃO ===

    /**
     * Verifica se categoria pode ser removida (sem cargos)
     */
    @Query("SELECT CASE WHEN COUNT(ca) = 0 THEN true ELSE false END FROM Categoria c LEFT JOIN c.cargos ca WHERE c.id = :id")
    boolean canDeleteCategoria(@Param("id") UUID id);

    /**
     * Lista categorias que não podem ser removidas (têm cargos)
     */
    @Query("SELECT DISTINCT c FROM Categoria c JOIN c.cargos ca")
    List<Categoria> findCategoriasNaoRemovíveis();

    // === CONSULTAS COM FILTROS COMPLEXOS ===

    /**
     * Busca categorias com filtros múltiplos
     */
    @Query("""
            SELECT c FROM Categoria c WHERE
            (:nome IS NULL OR UPPER(c.nome) LIKE UPPER(CONCAT('%', :nome, '%'))) AND
            (:ativo IS NULL OR c.ativo = :ativo) AND
            (:ordemMin IS NULL OR c.ordemExibicao >= :ordemMin) AND
            (:ordemMax IS NULL OR c.ordemExibicao <= :ordemMax)
            ORDER BY c.ordemExibicao, c.nome
            """)
    Page<Categoria> findByFiltros(@Param("nome") String nome,
                                  @Param("ativo") Boolean ativo,
                                  @Param("ordemMin") Integer ordemMin,
                                  @Param("ordemMax") Integer ordemMax,
                                  Pageable pageable);

    /**
     * Busca categorias ativas com cargos criados em determinado período
     */
    @Query("""
            SELECT DISTINCT c FROM Categoria c 
            JOIN c.cargos ca 
            WHERE c.ativo = true 
            AND ca.createdAt BETWEEN :inicio AND :fim
            ORDER BY c.ordemExibicao
            """)
    List<Categoria> findCategoriasComCargosNoPeriodo(@Param("inicio") LocalDateTime inicio,
                                                     @Param("fim") LocalDateTime fim);

    /**
     * Busca categoria por ordem de exibição
     */
    Optional<Categoria> findByOrdemExibicao(Integer ordem);

    /**
     * Busca categorias entre determinadas ordens
     */
    @Query("SELECT c FROM Categoria c WHERE c.ordemExibicao BETWEEN :ordemInicio AND :ordemFim ORDER BY c.ordemExibicao")
    List<Categoria> findByOrdemExibicaoBetween(@Param("ordemInicio") Integer ordemInicio,
                                               @Param("ordemFim") Integer ordemFim);

    /**
     * Busca todas as ordens de exibição em uso
     */
    @Query("SELECT DISTINCT c.ordemExibicao FROM Categoria c WHERE c.ordemExibicao IS NOT NULL ORDER BY c.ordemExibicao")
    List<Integer> findAllOrdensEmUso();

    /**
     * Conta categorias ativas com cargos disponíveis
     */
    @Query("""
            SELECT COUNT(DISTINCT c) FROM Categoria c 
            JOIN c.cargos ca 
            WHERE c.ativo = true 
            AND ca.ativo = true 
            AND ca.disponivelEleicao = true
            """)
    long countCategoriasAtivasComCargosDisponiveis();

    /**
     * Busca categorias por hierarquia dos cargos
     */
    @Query("""
            SELECT DISTINCT c FROM Categoria c 
            JOIN c.cargos ca 
            WHERE ca.hierarquia = :hierarquia 
            ORDER BY c.ordemExibicao
            """)
    List<Categoria> findByHierarquiaDosCargos(@Param("hierarquia") HierarquiaCargo hierarquia);

    /**
     * Busca estatísticas por hierarquia
     */
    @Query("""
            SELECT c.nome, ca.hierarquia, COUNT(ca) 
            FROM Categoria c 
            JOIN c.cargos ca 
            WHERE c.ativo = true AND ca.ativo = true
            GROUP BY c.id, c.nome, ca.hierarquia 
            ORDER BY c.ordemExibicao, ca.hierarquia
            """)
    List<Object[]> getEstatisticasPorHierarquia();

    /**
     * Verifica se categoria tem cargos de determinada hierarquia
     */
    @Query("""
            SELECT CASE WHEN COUNT(ca) > 0 THEN true ELSE false END 
            FROM Categoria c 
            JOIN c.cargos ca 
            WHERE c.id = :categoriaId AND ca.hierarquia = :hierarquia
            """)
    boolean temCargosDaHierarquia(@Param("categoriaId") UUID categoriaId,
                                  @Param("hierarquia") HierarquiaCargo hierarquia);

    /**
     * Lista categorias ordenadas por precedência (ordem + nome)
     */
    @Query("SELECT c FROM Categoria c ORDER BY c.ordemExibicao ASC, c.nome ASC")
    List<Categoria> findAllOrderByPrecedencia();

    /**
     * Busca categorias ativas para seleção (básico)
     */
    @Query("SELECT c FROM Categoria c WHERE c.ativo = true ORDER BY c.ordemExibicao ASC, c.nome ASC")
    List<Categoria> findCategoriasParaSelecao();
}