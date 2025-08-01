package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Membro;
import com.br.ibetelvote.domain.repositories.MembroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembroJpaRepository extends JpaRepository<Membro, UUID>, MembroRepository {

    @Override
    Optional<Membro> findByEmail(String email);

    @Override
    Optional<Membro> findByUserId(UUID userId);

    @Override
    Page<Membro> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Override
    Page<Membro> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    @Override
    Page<Membro> findByAtivoTrue(Pageable pageable);

    @Override
    boolean existsByEmail(String email);

    @Override
    boolean existsByEmailAndIdNot(String email, UUID id);

    @Override
    long countByAtivoTrue();

    @Query("SELECT m FROM Membro m WHERE " +
            "(:nome IS NULL OR UPPER(m.nome) LIKE UPPER(CONCAT('%', :nome, '%'))) AND " +
            "(:email IS NULL OR UPPER(m.email) LIKE UPPER(CONCAT('%', :email, '%'))) AND " +
            "(:ativo IS NULL OR m.ativo = :ativo)")
    Page<Membro> findByFilters(@Param("nome") String nome,
                               @Param("email") String email,
                               @Param("ativo") Boolean ativo,
                               Pageable pageable);

    @Query("SELECT m FROM Membro m LEFT JOIN FETCH m.user WHERE m.id = :id")
    Optional<Membro> findByIdWithUser(@Param("id") UUID id);
}