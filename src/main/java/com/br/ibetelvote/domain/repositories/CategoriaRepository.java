package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository {


    List<Categoria> findAll();
    Page<Categoria> findAll(Pageable pageable);
    void deleteById(UUID id);
    long count();

    // === CONSULTAS POR NOME ===
    Optional<Categoria> findByNome(String nome);
    boolean existsByNome(String nome);
    List<Categoria> findByNomeContainingIgnoreCase(String nome);
    boolean existsByNomeAndIdNot(String nome, UUID id);

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
    List<Categoria> findCategoriasComCargos();
    List<Categoria> findCategoriasSemCargos();
    List<Categoria> findCategoriasComCargosAtivos();
    List<Categoria> findCategoriasComCargosDisponiveis();
    Integer findNextOrdemExibicao();

    boolean existsByOrdemExibicao(Integer ordem);
    boolean existsByOrdemExibicaoAndIdNot(Integer ordem, UUID id);

    // === CONSULTAS PARA RELATÓRIOS ===
    List<Categoria> findTop10ByOrderByCreatedAtDesc();
    List<Categoria> findCategoriasComMaisCargos();
    List<Object[]> countCargosPorCategoria();
    List<Object[]> getEstatisticasCategorias();

    // === CONSULTAS PARA VALIDAÇÃO ===
    boolean canDeleteCategoria(UUID id);
    List<Categoria> findCategoriasNaoRemovíveis();
    Optional<Categoria> findByNomeIgnoreCaseAndTrimmed(String nome);

    // === CONSULTAS COM FILTROS ===
    Page<Categoria> findByFiltros(String nome, Boolean ativo, Integer ordemMin, Integer ordemMax, Pageable pageable);
    List<Categoria> findCategoriasComCargosNoPeriodo(java.time.LocalDateTime inicio, java.time.LocalDateTime fim);
}