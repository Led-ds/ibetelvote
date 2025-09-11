package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Categoria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de domínio para CategoriaRepository.
 * Contém apenas métodos específicos de negócio.
 * Métodos CRUD básicos são fornecidos automaticamente pelo JpaRepository.
 * Lógicas complexas são implementadas via Specifications no Service.
 */
public interface CategoriaRepository {

    // === CONSULTAS ESPECÍFICAS POR ATRIBUTOS ===
    Optional<Categoria> findByNome(String nome);
    boolean existsByNome(String nome);
    List<Categoria> findByAtivoTrue();

    // === CONSULTAS ORDENADAS ESPECÍFICAS ===
    List<Categoria> findAllByOrderByNomeAsc();
    List<Categoria> findAllByOrderByOrdemExibicaoAsc();
    List<Categoria> findByAtivoTrueOrderByOrdemExibicaoAsc();

    // === VALIDAÇÕES ESPECÍFICAS DE NEGÓCIO ===
    boolean existsByNomeAndIdNot(String nome, UUID id);
    boolean existsByOrdemExibicao(Integer ordem);
    boolean canDeleteCategoria(UUID id);

    // === ORDEM DE EXIBIÇÃO ESPECÍFICA ===
    Optional<Categoria> findByOrdemExibicao(Integer ordem);
    Integer findNextOrdemExibicao();

}