package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Categoria;
import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de domínio para CargoRepository.
 * Define contratos de negócio que serão implementados pelo CargoJpaRepository.
 * Métodos complexos são implementados no Service usando Specifications.
 */
public interface CargoRepository {

    // === OPERAÇÕES BÁSICAS (JpaRepository já fornece) ===

    boolean existsById(UUID id);
    List<Cargo> findAll();
    List<Cargo> findAllById(Iterable<UUID> ids);
    Page<Cargo> findAll(Pageable pageable);
    long count();

    // === CONSULTAS POR NOME ===

    boolean existsByNome(String nome);
    boolean existsByNomeAndIdNot(String nome, UUID id);

    // === CONSULTAS POR STATUS ===

    List<Cargo> findByAtivoTrue();
    List<Cargo> findByAtivoFalse();
    List<Cargo> findByAtivoTrueOrderByNomeAsc();
    Page<Cargo> findByAtivoTrueOrderByNomeAsc(Pageable pageable);
    Page<Cargo> findByAtivo(Boolean ativo, Pageable pageable);
    long countByAtivo(Boolean ativo);

    // === CONSULTAS POR DISPONIBILIDADE ELEITORAL ===

    List<Cargo> findByDisponivelEleicaoTrue();
    List<Cargo> findByAtivoTrueAndDisponivelEleicaoTrue();
    long countByDisponivelEleicao(Boolean disponivel);

    // === CONSULTAS ORDENADAS ===

    List<Cargo> findAllByOrderByNomeAsc();

    // === CONSULTAS POR CATEGORIA ===

    List<Cargo> findByCategoria(Categoria categoria);
    List<Cargo> findByCategoriaAndAtivoTrue(Categoria categoria);
    List<Cargo> findByCategoria_Id(UUID categoriaId);
    List<Cargo> findByCategoria_IdAndAtivoTrue(UUID categoriaId);
    Page<Cargo> findByCategoria(Categoria categoria, Pageable pageable);
    Page<Cargo> findByCategoria_Id(UUID categoriaId, Pageable pageable);
    long countByCategoria(Categoria categoria);
    long countByCategoria_Id(UUID categoriaId);

    // === CONSULTAS POR CATEGORIA COM ORDENAÇÃO ===

    List<Cargo> findByCategoriaOrderByOrdemPrecedenciaAscNomeAsc(Categoria categoria);
    List<Cargo> findByCategoria_IdOrderByOrdemPrecedenciaAscNomeAsc(UUID categoriaId);
    List<Cargo> findByCategoriaAndAtivoTrueOrderByOrdemPrecedenciaAscNomeAsc(Categoria categoria);

    // === CONSULTAS POR HIERARQUIA ===

    List<Cargo> findByHierarquia(HierarquiaCargo hierarquia);
    List<Cargo> findByHierarquiaAndAtivoTrue(HierarquiaCargo hierarquia);
    Page<Cargo> findByHierarquia(HierarquiaCargo hierarquia, Pageable pageable);
    long countByHierarquia(HierarquiaCargo hierarquia);

    // === CONSULTAS POR HIERARQUIA COM CATEGORIA ===

    List<Cargo> findByCategoriaAndHierarquia(Categoria categoria, HierarquiaCargo hierarquia);
    List<Cargo> findByCategoria_IdAndHierarquia(UUID categoriaId, HierarquiaCargo hierarquia);

    // === CONSULTAS POR MÚLTIPLAS HIERARQUIAS ===

    List<Cargo> findByHierarquiaIn(List<HierarquiaCargo> hierarquias);
    List<Cargo> findByHierarquiaInAndAtivoTrue(List<HierarquiaCargo> hierarquias);

    // === CONSULTAS POR ORDEM DE PRECEDÊNCIA ===

    List<Cargo> findByOrdemPrecedenciaBetween(Integer ordemMin, Integer ordemMax);
    Optional<Cargo> findByCategoriaAndOrdemPrecedencia(Categoria categoria, Integer ordem);
    Optional<Cargo> findByCategoria_IdAndOrdemPrecedencia(UUID categoriaId, Integer ordem);
    boolean existsByCategoriaAndOrdemPrecedencia(Categoria categoria, Integer ordem);
    boolean existsByCategoria_IdAndOrdemPrecedencia(UUID categoriaId, Integer ordem);
    boolean existsByCategoriaAndOrdemPrecedenciaAndIdNot(Categoria categoria, Integer ordem, UUID id);

    // === PRÓXIMA ORDEM DISPONÍVEL ===

    Integer findNextOrdemPrecedenciaByCategoria(Categoria categoria);
    Integer findNextOrdemPrecedenciaByCategoriaId(UUID categoriaId);

    // === CONSULTAS ESPECÍFICAS COMBINADAS ===

    List<Cargo> findCargosDisponiveis();
    Page<Cargo> findCargosDisponiveis(Pageable pageable);
    long countCargosDisponiveis();
    List<Cargo> findCargosDisponiveisByCategoria(Categoria categoria);
    List<Cargo> findCargosDisponiveisByCategoriaId(UUID categoriaId);

    // === CONSULTAS PARA VALIDAÇÃO ===

    List<Cargo> findCargosMinisteriais();
    List<Cargo> findCargosMinisteriaisAtivos();

    List<Cargo> findCargosLideranca();
    List<Cargo> findCargosAdministrativos();

    boolean canDeleteCargo(UUID id);
    boolean existsCargoAtivoByNome(String nome);

    // === CONSULTAS PARA ESTATÍSTICAS ===

    List<Object[]> countCargosPorCategoria();
    List<Object[]> countCargosPorHierarquia();
    List<Object[]> getEstatisticasCompletas();
    List<Object[]> getRelatorioHierarquiaPorCategoria();

    // === CONSULTAS PARA RELATÓRIOS ===

    List<Cargo> findTop10ByOrderByCreatedAtDesc();
    boolean isOrdemPrecedenciaDisponivel(UUID categoriaId, Integer ordem);

    List<Cargo> findByCreatedAtBetween(LocalDateTime inicio, LocalDateTime fim);

    // === MÉTODOS DE SUPORTE PARA SPECIFICATIONS ===

    Optional<String> findNomeById(UUID cargoId);
    Optional<HierarquiaCargo> findHierarquiaById(UUID cargoId);
    boolean existsCategoria(UUID categoriaId);

}