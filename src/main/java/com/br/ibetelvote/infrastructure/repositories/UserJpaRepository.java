package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.User;
import com.br.ibetelvote.domain.entities.enus.UserRole;
import com.br.ibetelvote.domain.repositories.UserRepository;
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
public interface UserJpaRepository extends JpaRepository<User, UUID>, UserRepository {

    @Override
    Optional<User> findByEmail(String email);

    @Override
    boolean existsByEmail(String email);

    @Override
    boolean existsByEmailAndIdNot(String email, UUID id);

    @Override
    Page<User> findByRole(UserRole role, Pageable pageable);

    @Override
    List<User> findByRoleAndAtivoTrue(UserRole role);

    @Override
    long countByRole(UserRole role);

    @Override
    long countByRoleAndAtivoTrue(UserRole role);

    @Override
    Page<User> findByAtivoTrue(Pageable pageable);

    @Override
    long countByAtivoTrue();

    @Override
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.ativo = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    @Override
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.membro WHERE u.id = :id")
    Optional<User> findByIdWithMembro(@Param("id") UUID id);

    boolean existsById(UUID id);

    Optional<User> findById(UUID id);

    User findByUser(UUID id);
}