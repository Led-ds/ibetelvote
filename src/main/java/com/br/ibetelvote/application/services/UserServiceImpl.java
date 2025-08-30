package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.auth.dto.*;
import com.br.ibetelvote.application.mapper.UserMapper;
import com.br.ibetelvote.domain.entities.User;
import com.br.ibetelvote.domain.entities.enums.UserRole;
import com.br.ibetelvote.domain.services.UserService;
import com.br.ibetelvote.infrastructure.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserJpaRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // === OPERAÇÕES BÁSICAS ===

    @Override
    @CacheEvict(value = {"users", "user-stats"}, allEntries = true)
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Criando novo usuário: {}", request.getEmail());

        // Validar se email já existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado: " + request.getEmail());
        }

        // Criar entidade user
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        log.info("Usuário criado com sucesso - ID: {}, Email: {}, Role: {}",
                savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        log.debug("Buscando usuário por ID: {}", id);

        User user = userRepository.findByIdWithMembro(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("Buscando usuário por email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com email: " + email));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(int page, int size, String sort, String direction) {
        log.debug("Listando usuários - página: {}, tamanho: {}", page, size);

        Pageable pageable = createPageable(page, size, sort, direction);
        Page<User> users = userRepository.findAll(pageable);

        return users.map(userMapper::toResponse);
    }

    @Override
    @CacheEvict(value = {"users", "user-stats"}, allEntries = true)
    public void deleteUser(UUID id) {
        log.info("Removendo usuário ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));

        // Validar se pode ser removido (regras de negócio)
        if (user.hasMembro()) {
            throw new IllegalStateException("Não é possível remover usuário que possui membro associado");
        }

        userRepository.delete(user);
        log.info("Usuário removido com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = {"users", "user-stats"}, allEntries = true)
    public void activateUser(UUID id) {
        log.info("Ativando usuário ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));

        user.activate();
        userRepository.save(user);

        log.info("Usuário ativado com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = {"users", "user-stats"}, allEntries = true)
    public void deactivateUser(UUID id) {
        log.info("Desativando usuário ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));

        user.deactivate();
        userRepository.save(user);

        log.info("Usuário desativado com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = {"users", "user-stats"}, allEntries = true)
    public void lockUser(UUID id) {
        log.info("Bloqueando usuário ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));

        user.lockAccount();
        userRepository.save(user);

        log.info("Usuário bloqueado com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = {"users", "user-stats"}, allEntries = true)
    public void unlockUser(UUID id) {
        log.info("Desbloqueando usuário ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));

        user.unlockAccount();
        userRepository.save(user);

        log.info("Usuário desbloqueado com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = {"users", "user-stats"}, allEntries = true)
    public void changeUserRole(UUID id, ChangeRoleRequest request) {
        log.info("Alterando role do usuário ID: {} para {}", id, request.getNewRole());

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));

        UserRole oldRole = user.getRole();
        user.changeRole(request.getNewRole());
        userRepository.save(user);

        log.info("Role alterada com sucesso - ID: {}, De: {} Para: {}",
                id, oldRole, request.getNewRole());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        log.debug("Buscando usuários ativos por role: {}", role);

        List<User> users = userRepository.findByRoleAndAtivoTrue(role);
        return userMapper.toResponseList(users);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void changePassword(UUID id, ChangePasswordRequest request) {
        log.info("Alterando senha do usuário ID: {}", id);

        // Validar confirmação de senha
        if (!request.isNewPasswordConfirmed()) {
            throw new IllegalArgumentException("Confirmação de senha não confere");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));

        // Validar senha atual
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        // Atualizar senha
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Senha alterada com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void resetPassword(UUID id, String newPassword) {
        log.info("Resetando senha do usuário ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + id));

        // Validar nova senha
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Nova senha deve ter no mínimo 6 caracteres");
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
        user.setCredentialsNonExpired(true); // Reativar credenciais se estavam expiradas
        userRepository.save(user);

        log.info("Senha resetada com sucesso - ID: {}", id);
    }

    @Override
    @Cacheable(value = "user-stats", key = "'total'")
    @Transactional(readOnly = true)
    public long getTotalUsers() {
        return userRepository.count();
    }

    @Override
    @Cacheable(value = "user-stats", key = "'total-active'")
    @Transactional(readOnly = true)
    public long getTotalActiveUsers() {
        return userRepository.countByAtivoTrue();
    }

    @Override
    @Cacheable(value = "user-stats", key = "#role")
    @Transactional(readOnly = true)
    public long getTotalUsersByRole(UserRole role) {
        return userRepository.countByRoleAndAtivoTrue(role);
    }

    private Pageable createPageable(int page, int size, String sort, String direction) {
        Sort sortObj = Sort.by(
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sort
        );
        return PageRequest.of(page, size, sortObj);
    }
}