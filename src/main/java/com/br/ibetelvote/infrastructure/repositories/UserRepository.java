package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.User;
import com.br.ibetelvote.domain.entities.enus.UserRole;
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
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndAtivoTrue(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    List<User> findByAtivoTrue();

    Page<User> findByAtivoTrue(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.ativo = true AND " +
            "(LOWER(u.nome) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findActiveUsersWithSearch(@Param("search") String search, Pageable pageable);

    List<User> findByRoleAndAtivoTrue(UserRole role);

    Page<User> findByRoleAndAtivoTrue(UserRole role, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.ativo = true")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.ativo = true")
    long countActiveUsersByRole(@Param("role") UserRole role);

    @Query("SELECT u FROM User u WHERE u.ativo = true AND u.role IN :roles")
    List<User> findActiveUsersByRoles(@Param("roles") List<UserRole> roles);

    @Query("SELECT u FROM User u WHERE u.ativo = true AND " +
            "u.role = :role AND " +
            "(LOWER(u.nome) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findActiveUsersByRoleWithSearch(@Param("role") UserRole role,
                                               @Param("search") String search,
                                               Pageable pageable);
}