package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.User;
import com.br.ibetelvote.domain.entities.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    // === OPERAÇÕES BÁSICAS ===
    void deleteById(UUID id);
    long count();

    // === CONSULTAS POR EMAIL ===
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    Optional<User> findActiveUserByEmail(String email);

    // === CONSULTAS COM RELACIONAMENTOS ===
    Optional<User> findByIdWithMembro(UUID id);

    // === CONSULTAS POR ROLE ===
    Page<User> findByRole(UserRole role, Pageable pageable);
    List<User> findByRoleAndAtivoTrue(UserRole role);
    long countByRole(UserRole role);
    long countByRoleAndAtivoTrue(UserRole role);

    // === CONSULTAS POR STATUS ===
    Page<User> findByAtivoTrue(Pageable pageable);
    long countByAtivoTrue();
}