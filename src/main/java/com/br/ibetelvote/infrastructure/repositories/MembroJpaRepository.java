package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Membro;
import com.br.ibetelvote.domain.repositories.MembroRepository;
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
public interface MembroJpaRepository extends JpaRepository<Membro, UUID>, MembroRepository {

    @Override
    Optional<Membro> findByEmail(String email);

    @Override
    Optional<Membro> findByUserId(UUID userId);

    @Override
    boolean existsByEmail(String email);

    @Override
    boolean existsByEmailAndIdNot(String email, UUID id);

    @Override
    boolean existsByUserId(UUID userId);

    @Override
    Page<Membro> findByAtivoTrue(Pageable pageable);

    @Override
    long countByAtivoTrue();

    @Override
    Page<Membro> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Override
    Page<Membro> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    @Override
    Page<Membro> findByCargoContainingIgnoreCase(String cargo, Pageable pageable);

    @Override
    List<Membro> findByUserIdIsNull();

    @Override
    List<Membro> findByUserIdIsNotNull();

    @Override
    List<Membro> findByFotoIsNull();

    @Override
    @Query("SELECT m FROM Membro m WHERE " +
            "m.nome IS NULL OR m.nome = '' OR " +
            "m.email IS NULL OR m.email = '' OR " +
            "m.dataNascimento IS NULL OR " +
            "m.cargo IS NULL OR m.cargo = '' OR " +
            "(m.telefone IS NULL OR m.telefone = '') AND (m.celular IS NULL OR m.celular = '')")
    List<Membro> findMembrosWithIncompleteProfile();

    @Override
    @Query("SELECT m FROM Membro m WHERE " +
            "(:nome IS NULL OR UPPER(m.nome) LIKE UPPER(CONCAT('%', :nome, '%'))) AND " +
            "(:email IS NULL OR UPPER(m.email) LIKE UPPER(CONCAT('%', :email, '%'))) AND " +
            "(:cargo IS NULL OR UPPER(m.cargo) LIKE UPPER(CONCAT('%', :cargo, '%'))) AND " +
            "(:ativo IS NULL OR m.ativo = :ativo) AND " +
            "(:hasUser IS NULL OR " +
            "  (:hasUser = true AND m.userId IS NOT NULL) OR " +
            "  (:hasUser = false AND m.userId IS NULL))")
    Page<Membro> findByFilters(@Param("nome") String nome,
                               @Param("email") String email,
                               @Param("cargo") String cargo,
                               @Param("ativo") Boolean ativo,
                               @Param("hasUser") Boolean hasUser,
                               Pageable pageable);

    // Consultas com JOIN para User
    @Query("SELECT m FROM Membro m LEFT JOIN FETCH m.user WHERE m.id = :id")
    Optional<Membro> findByIdWithUser(@Param("id") UUID id);

    @Query("SELECT m FROM Membro m LEFT JOIN FETCH m.user WHERE m.email = :email")
    Optional<Membro> findByEmailWithUser(@Param("email") String email);

    boolean existsById(UUID id);
}