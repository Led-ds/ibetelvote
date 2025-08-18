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
    List<Membro> findByFotoDataIsNull();

    @Override
    @Query("SELECT m FROM Membro m WHERE " +
            "m.nome IS NULL OR m.nome = '' OR " +
            "m.email IS NULL OR m.email = '' OR " +
            "m.dataNascimento IS NULL OR " +
            "m.cargo IS NULL OR m.cargo = '' OR " +
            "(m.telefone IS NULL OR m.telefone = '') AND (m.celular IS NULL OR m.celular = '')")
    List<Membro> findMembrosWithIncompleteProfile();

    @Override
    @Query(value = "SELECT * FROM membros m WHERE " +
            "(:nome IS NULL OR LOWER(m.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
            "(:email IS NULL OR LOWER(m.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:cargo IS NULL OR LOWER(m.cargo) LIKE LOWER(CONCAT('%', :cargo, '%'))) AND " +
            "(:ativo IS NULL OR m.ativo = :ativo) AND " +
            "(:hasUser IS NULL OR " +
            "  (:hasUser = true AND m.user_id IS NOT NULL) OR " +
            "  (:hasUser = false AND m.user_id IS NULL)) " +
            "ORDER BY m.nome",
            nativeQuery = true)
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

    // ✅ MÉTODO FALTANTE ADICIONADO
    /**
     * Busca um membro pelo email E CPF (ambos devem coincidir)
     * Usado para validação no auto-cadastro
     */
    @Query("SELECT m FROM Membro m WHERE m.email = :email AND m.cpf = :cpf")
    Optional<Membro> findByEmailAndCpf(@Param("email") String email, @Param("cpf") String cpf);

    // ✅ MÉTODOS ADICIONAIS ÚTEIS PARA AUTO-CADASTRO

    /**
     * Busca membro por CPF
     */
    Optional<Membro> findByCpf(String cpf);

    /**
     * Verifica se já existe um membro com o CPF informado
     */
    boolean existsByCpf(String cpf);

    /**
     * Verifica se existe membro com CPF diferente do ID informado
     */
    boolean existsByCpfAndIdNot(String cpf, UUID id);

    boolean existsById(UUID id);
}