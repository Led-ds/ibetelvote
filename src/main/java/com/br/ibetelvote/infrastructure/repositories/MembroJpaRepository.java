package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Membro;
import com.br.ibetelvote.domain.repositories.MembroRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository para Membro - Versão Simplificada.
 * Implementa apenas os métodos essenciais da interface de domínio.
 * Métodos CRUD básicos são fornecidos automaticamente pelo JpaRepository.
 * Complexidade migrada para MembroSpecifications + MembroService.
 */
@Repository
public interface MembroJpaRepository extends JpaRepository<Membro, UUID>,
        JpaSpecificationExecutor<Membro>,
        MembroRepository {

    // === IMPLEMENTAÇÃO DOS MÉTODOS DA INTERFACE DOMAIN ===
    @Override
    @Query("SELECT m FROM Membro m WHERE UPPER(m.email) = UPPER(:email)")
    Optional<Membro> findByEmail(@Param("email") String email);

    @Override
    Optional<Membro> findByCpf(String cpf);

    @Override
    Optional<Membro> findByUserId(UUID userId);

    @Override
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
            "FROM Membro m WHERE UPPER(m.email) = UPPER(:email)")
    boolean existsByEmail(@Param("email") String email);

    @Override
    boolean existsByCpf(String cpf);

    @Override
    boolean existsByUserId(UUID userId);

    @Override
    List<Membro> findByAtivoTrue();

    @Override
    List<Membro> findByAtivoFalse();

    @Override
    long countByAtivo(Boolean ativo);

    @Override
    List<Membro> findByCargoAtualId(UUID cargoId);

    @Override
    long countByCargoAtualId(UUID cargoId);

    @Override
    List<Membro> findAllByOrderByNomeAsc();

    @Override
    List<Membro> findByAtivoTrueOrderByNomeAsc();

    @Override
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
            "FROM Membro m WHERE UPPER(m.email) = UPPER(:email) AND m.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") UUID id);

    @Override
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
            "FROM Membro m WHERE m.cpf = :cpf AND m.id != :id")
    boolean existsByCpfAndIdNot(@Param("cpf") String cpf, @Param("id") UUID id);

    @Override
    @Query("SELECT m FROM Membro m WHERE UPPER(m.nome) LIKE UPPER(CONCAT('%', :nome, '%'))")
    List<Membro> findByNomeContainingIgnoreCase(@Param("nome") String nome);

}