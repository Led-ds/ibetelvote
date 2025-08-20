package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.repositories.CargoRepository;
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
public interface CargoJpaRepository extends JpaRepository<Cargo, UUID>, CargoRepository {

    Optional<Cargo> findByNome(String nome);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cargo c WHERE UPPER(c.nome) = UPPER(:nome)")
    boolean existsByNome(@Param("nome") String nome);

    @Query("SELECT c FROM Cargo c WHERE UPPER(c.nome) LIKE UPPER(CONCAT('%', :nome, '%'))")
    List<Cargo> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    List<Cargo> findByAtivoTrue();
    List<Cargo> findByAtivoFalse();
    Page<Cargo> findByAtivo(Boolean ativo, Pageable pageable);
    long countByAtivo(Boolean ativo);

    List<Cargo> findAllByOrderByNomeAsc();
    List<Cargo> findByAtivoTrueOrderByNomeAsc();
    Page<Cargo> findByAtivoTrueOrderByNomeAsc(Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cargo c WHERE UPPER(c.nome) = UPPER(:nome) AND c.id != :id")
    boolean existsByNomeAndIdNot(@Param("nome") String nome, @Param("id") UUID id);

    @Query("SELECT c FROM Cargo c ORDER BY c.createdAt DESC")
    List<Cargo> findTop10ByOrderByCreatedAtDesc();

    /**
     * Busca cargos disponíveis para eleições (ativos com informações completas)
     */
    @Query("SELECT c FROM Cargo c WHERE c.ativo = true AND c.nome IS NOT NULL AND c.descricao IS NOT NULL ORDER BY c.nome")
    List<Cargo> findCargosDisponiveis();

    /**
     * Busca cargos por filtro genérico
     */
    @Query("SELECT c FROM Cargo c WHERE " +
            "(:nome IS NULL OR UPPER(c.nome) LIKE UPPER(CONCAT('%', :nome, '%'))) AND " +
            "(:ativo IS NULL OR c.ativo = :ativo) " +
            "ORDER BY c.nome")
    Page<Cargo> findByFiltros(@Param("nome") String nome,
                              @Param("ativo") Boolean ativo,
                              Pageable pageable);

    /**
     * Conta cargos que podem ser usados em eleições
     */
    @Query("SELECT COUNT(c) FROM Cargo c WHERE c.ativo = true AND c.nome IS NOT NULL AND c.descricao IS NOT NULL")
    long countCargosDisponiveis();

    /**
     * Verifica se existe cargo ativo com o nome especificado
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cargo c WHERE UPPER(c.nome) = UPPER(:nome) AND c.ativo = true")
    boolean existsCargoAtivoByNome(@Param("nome") String nome);
}