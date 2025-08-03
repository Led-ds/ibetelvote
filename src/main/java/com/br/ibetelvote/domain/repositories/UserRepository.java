package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.User;
import com.br.ibetelvote.domain.entities.enus.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Page<User> findAll(Pageable pageable);
    void deleteById(UUID id);
    boolean existsById(UUID id);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    long count();

    // Consultas por Role
    Page<User> findByRole(UserRole role, Pageable pageable);
    List<User> findByRoleAndAtivoTrue(UserRole role);
    long countByRole(UserRole role);
    long countByRoleAndAtivoTrue(UserRole role);

    // Consultas por Status
    Page<User> findByAtivoTrue(Pageable pageable);
    long countByAtivoTrue();

    // Consultas específicas para autenticação
    Optional<User> findActiveUserByEmail(String email);
    Optional<User> findByIdWithMembro(UUID id);
}