package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Categoria;
import com.br.ibetelvote.domain.repositories.CategoriaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository para Categoria - Versão Simplificada.
 * Implementa apenas os métodos essenciais da interface de domínio.
 * Métodos CRUD básicos são fornecidos automaticamente pelo JpaRepository.
 * Complexidade migrada para CategoriaSpecifications + CategoriaService.
 */
@Repository
public interface CategoriaJpaRepository extends JpaRepository<Categoria, UUID>,
        JpaSpecificationExecutor<Categoria>,
        CategoriaRepository {

    // === IMPLEMENTAÇÃO DOS MÉTODOS DA INTERFACE DOMAIN ===
    @Override
    @Query("SELECT c FROM Categoria c WHERE UPPER(TRIM(c.nome)) = UPPER(TRIM(:nome))")
    Optional<Categoria> findByNome(@Param("nome") String nome);

    @Override
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Categoria c WHERE UPPER(TRIM(c.nome)) = UPPER(TRIM(:nome))")
    boolean existsByNome(@Param("nome") String nome);

    @Override
    List<Categoria> findByAtivoTrue();

    @Override
    List<Categoria> findAllByOrderByNomeAsc();

    @Override
    List<Categoria> findAllByOrderByOrdemExibicaoAsc();

    @Override
    List<Categoria> findByAtivoTrueOrderByOrdemExibicaoAsc();

    @Override
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Categoria c WHERE UPPER(TRIM(c.nome)) = UPPER(TRIM(:nome)) AND c.id != :id")
    boolean existsByNomeAndIdNot(@Param("nome") String nome, @Param("id") UUID id);

    @Override
    boolean existsByOrdemExibicao(Integer ordem);

    @Override
    @Query("SELECT CASE WHEN COUNT(ca) = 0 THEN true ELSE false END " +
            "FROM Categoria c LEFT JOIN c.cargos ca WHERE c.id = :id")
    boolean canDeleteCategoria(@Param("id") UUID id);

    @Override
    Optional<Categoria> findByOrdemExibicao(Integer ordem);

    @Override
    @Query("SELECT COALESCE(MAX(c.ordemExibicao), 0) + 1 FROM Categoria c")
    Integer findNextOrdemExibicao();

}