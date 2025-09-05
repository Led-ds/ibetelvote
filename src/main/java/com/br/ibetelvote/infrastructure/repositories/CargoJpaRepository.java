package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Categoria;
import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import com.br.ibetelvote.domain.repositories.CargoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CargoJpaRepository extends JpaRepository<Cargo, UUID>, JpaSpecificationExecutor<Cargo>, CargoRepository {

    // === CONSULTAS POR NOME (implementação automática Spring Data) ===

    @Override
    boolean existsByNome(String nome);

    @Override
    boolean existsByNomeAndIdNot(String nome, UUID id);

    // === CONSULTAS POR STATUS ===

    @Override
    List<Cargo> findByAtivoTrue();

    @Override
    List<Cargo> findByAtivoFalse();

    @Override
    List<Cargo> findByAtivoTrueOrderByNomeAsc();

    @Override
    Page<Cargo> findByAtivoTrueOrderByNomeAsc(Pageable pageable);

    @Override
    Page<Cargo> findByAtivo(Boolean ativo, Pageable pageable);

    @Override
    long countByAtivo(Boolean ativo);

    // === CONSULTAS POR DISPONIBILIDADE ELEITORAL ===

    @Override
    List<Cargo> findByDisponivelEleicaoTrue();

    @Override
    List<Cargo> findByAtivoTrueAndDisponivelEleicaoTrue();

    @Override
    long countByDisponivelEleicao(Boolean disponivel);

    // === CONSULTAS ORDENADAS ===

    @Override
    List<Cargo> findAllByOrderByNomeAsc();

    // === CONSULTAS POR CATEGORIA ===

    @Override
    List<Cargo> findByCategoria(Categoria categoria);

    @Override
    List<Cargo> findByCategoriaAndAtivoTrue(Categoria categoria);

    @Override
    List<Cargo> findByCategoria_Id(UUID categoriaId);

    @Override
    List<Cargo> findByCategoria_IdAndAtivoTrue(UUID categoriaId);

    @Override
    Page<Cargo> findByCategoria(Categoria categoria, Pageable pageable);

    @Override
    Page<Cargo> findByCategoria_Id(UUID categoriaId, Pageable pageable);

    @Override
    long countByCategoria(Categoria categoria);

    @Override
    long countByCategoria_Id(UUID categoriaId);

    // === CONSULTAS POR CATEGORIA COM ORDENAÇÃO ===

    @Override
    List<Cargo> findByCategoriaOrderByOrdemPrecedenciaAscNomeAsc(Categoria categoria);

    @Override
    @Query("SELECT c FROM Cargo c WHERE c.categoria.id = :categoriaId " +
            "ORDER BY c.ordemPrecedencia ASC, c.nome ASC")
    List<Cargo> findByCategoria_IdOrderByOrdemPrecedenciaAscNomeAsc(@Param("categoriaId") UUID categoriaId);

    @Override
    List<Cargo> findByCategoriaAndAtivoTrueOrderByOrdemPrecedenciaAscNomeAsc(Categoria categoria);

    // === CONSULTAS POR HIERARQUIA ===

    @Override
    List<Cargo> findByHierarquia(HierarquiaCargo hierarquia);

    @Override
    List<Cargo> findByHierarquiaAndAtivoTrue(HierarquiaCargo hierarquia);

    @Override
    Page<Cargo> findByHierarquia(HierarquiaCargo hierarquia, Pageable pageable);

    @Override
    long countByHierarquia(HierarquiaCargo hierarquia);

    @Query("SELECT c FROM Cargo c WHERE c.hierarquia IN ('PASTORAL', 'PRESBITERAL', 'DIACONAL')")
    List<Cargo> findCargosMinisteriais();

    @Query("SELECT c FROM Cargo c WHERE c.hierarquia IN ('PASTORAL', 'PRESBITERAL', 'DIACONAL') " +
            "AND c.ativo = true")
    List<Cargo> findCargosMinisteriaisAtivos();

    @Query("SELECT c FROM Cargo c WHERE c.hierarquia IN ('PASTORAL', 'PRESBITERAL', 'LIDERANCA') ORDER BY c.hierarquia, c.nome")
    List<Cargo> findCargosLideranca();

    @Query("SELECT c FROM Cargo c WHERE c.hierarquia = 'ADMINISTRATIVO'")
    List<Cargo> findCargosAdministrativos();


    // === CONSULTAS POR HIERARQUIA COM CATEGORIA ===

    @Override
    List<Cargo> findByCategoriaAndHierarquia(Categoria categoria, HierarquiaCargo hierarquia);

    @Override
    List<Cargo> findByCategoria_IdAndHierarquia(UUID categoriaId, HierarquiaCargo hierarquia);

    // === CONSULTAS POR MÚLTIPLAS HIERARQUIAS ===

    @Override
    List<Cargo> findByHierarquiaIn(List<HierarquiaCargo> hierarquias);

    @Override
    List<Cargo> findByHierarquiaInAndAtivoTrue(List<HierarquiaCargo> hierarquias);

    // === CONSULTAS POR ORDEM DE PRECEDÊNCIA ===

    @Override
    List<Cargo> findByOrdemPrecedenciaBetween(Integer ordemMin, Integer ordemMax);

    @Override
    Optional<Cargo> findByCategoriaAndOrdemPrecedencia(Categoria categoria, Integer ordem);

    @Override
    Optional<Cargo> findByCategoria_IdAndOrdemPrecedencia(UUID categoriaId, Integer ordem);

    @Override
    boolean existsByCategoriaAndOrdemPrecedencia(Categoria categoria, Integer ordem);

    @Override
    boolean existsByCategoria_IdAndOrdemPrecedencia(UUID categoriaId, Integer ordem);

    @Override
    boolean existsByCategoriaAndOrdemPrecedenciaAndIdNot(Categoria categoria, Integer ordem, UUID id);

    // === PRÓXIMA ORDEM DISPONÍVEL ===

    @Override
    @Query("SELECT COALESCE(MAX(c.ordemPrecedencia), 0) + 1 FROM Cargo c WHERE c.categoria = :categoria")
    Integer findNextOrdemPrecedenciaByCategoria(@Param("categoria") Categoria categoria);

    @Override
    @Query("SELECT COALESCE(MAX(c.ordemPrecedencia), 0) + 1 FROM Cargo c WHERE c.categoria.id = :categoriaId")
    Integer findNextOrdemPrecedenciaByCategoriaId(@Param("categoriaId") UUID categoriaId);

    @Query("SELECT CASE WHEN COUNT(c) = 0 THEN true ELSE false END FROM Cargo c WHERE c.categoria.id = :categoriaId AND c.ordemPrecedencia = :ordem AND c.id != :cargoId")
    boolean isOrdemPrecedenciaDisponivel(@Param("categoriaId") UUID categoriaId, @Param("ordem") Integer ordem, @Param("cargoId") UUID cargoId);

    // === CONSULTAS ESPECÍFICAS COMBINADAS ===

    @Override
    @Query("SELECT c FROM Cargo c WHERE c.ativo = true AND c.disponivelEleicao = true " +
            "ORDER BY c.nome ASC")
    List<Cargo> findCargosDisponiveis();

    @Override
    @Query("SELECT c FROM Cargo c WHERE c.ativo = true AND c.disponivelEleicao = true")
    Page<Cargo> findCargosDisponiveis(Pageable pageable);

    @Override
    @Query("SELECT COUNT(c) FROM Cargo c WHERE c.ativo = true AND c.disponivelEleicao = true")
    long countCargosDisponiveis();

    @Override
    @Query("SELECT c FROM Cargo c WHERE c.categoria = :categoria " +
            "AND c.ativo = true AND c.disponivelEleicao = true")
    List<Cargo> findCargosDisponiveisByCategoria(@Param("categoria") Categoria categoria);

    @Override
    @Query("SELECT c FROM Cargo c WHERE c.categoria.id = :categoriaId " +
            "AND c.ativo = true AND c.disponivelEleicao = true")
    List<Cargo> findCargosDisponiveisByCategoriaId(@Param("categoriaId") UUID categoriaId);

    // === CONSULTAS PARA VALIDAÇÃO ===

    @Override
    @Query("SELECT CASE WHEN COUNT(cand) = 0 THEN true ELSE false END " +
            "FROM Candidato cand WHERE cand.cargoPretendido.id = :cargoId")
    boolean canDeleteCargo(@Param("cargoId") UUID cargoId);

    @Override
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Cargo c WHERE c.nome = :nome AND c.ativo = true")
    boolean existsCargoAtivoByNome(@Param("nome") String nome);

    // === CONSULTAS PARA ESTATÍSTICAS ===

    @Override
    @Query("SELECT cat.nome, COUNT(c) FROM Cargo c JOIN c.categoria cat GROUP BY cat.nome")
    List<Object[]> countCargosPorCategoria();

    @Override
    @Query("SELECT c.hierarquia, COUNT(c) FROM Cargo c GROUP BY c.hierarquia")
    List<Object[]> countCargosPorHierarquia();

    @Override
    @Query("SELECT " +
            "COUNT(c) as total, " +
            "COUNT(CASE WHEN c.ativo = true THEN 1 END) as ativos, " +
            "COUNT(CASE WHEN c.disponivelEleicao = true THEN 1 END) as disponiveis, " +
            "COUNT(CASE WHEN c.ativo = true AND c.disponivelEleicao = true THEN 1 END) as ativosDisponiveis " +
            "FROM Cargo c")
    List<Object[]> getEstatisticasCompletas();

    @Override
    @Query("SELECT cat.nome, c.hierarquia, COUNT(c) " +
            "FROM Cargo c JOIN c.categoria cat " +
            "GROUP BY cat.nome, c.hierarquia " +
            "ORDER BY cat.nome, c.hierarquia")
    List<Object[]> getRelatorioHierarquiaPorCategoria();

    // === CONSULTAS PARA RELATÓRIOS ===

    @Override
    List<Cargo> findTop10ByOrderByCreatedAtDesc();

    @Override
    List<Cargo> findByCreatedAtBetween(LocalDateTime inicio, LocalDateTime fim);

    // === MÉTODOS DE SUPORTE PARA SPECIFICATIONS ===

    @Override
    @Query("SELECT c.nome FROM Cargo c WHERE c.id = :cargoId")
    Optional<String> findNomeById(@Param("cargoId") UUID cargoId);

    @Override
    @Query("SELECT c.hierarquia FROM Cargo c WHERE c.id = :cargoId")
    Optional<HierarquiaCargo> findHierarquiaById(@Param("cargoId") UUID cargoId);

    @Override
    @Query("SELECT CASE WHEN COUNT(cat) > 0 THEN true ELSE false END " +
            "FROM Categoria cat WHERE cat.id = :categoriaId")
    boolean existsCategoria(@Param("categoriaId") UUID categoriaId);

    // === MÉTODOS ADICIONAIS PARA REORGANIZAÇÃO ===

    @Query("SELECT c FROM Cargo c WHERE c.categoria.id = :categoriaId " +
            "ORDER BY c.ordemPrecedencia ASC NULLS LAST, c.nome ASC")
    List<Cargo> findCargosParaReorganizacao(@Param("categoriaId") UUID categoriaId);

    @Query("SELECT c FROM Cargo c WHERE c.categoria = :categoria " +
            "ORDER BY c.ordemPrecedencia ASC NULLS LAST, c.nome ASC")
    List<Cargo> findCargosParaReorganizacaoByCategoria(@Param("categoria") Categoria categoria);

    ScopedValue<Object> findByNome(String nome);
}