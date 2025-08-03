package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.auth.dto.*;
import com.br.ibetelvote.domain.entities.enus.UserRole;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface UserService {

    // === OPERAÇÕES BÁSICAS ===
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(UUID id);
    UserResponse getUserByEmail(String email);
    Page<UserResponse> getAllUsers(int page, int size, String sort, String direction);
    void deleteUser(UUID id);

    // === OPERAÇÕES DE CONTROLE DE CONTA ===
    void activateUser(UUID id);
    void deactivateUser(UUID id);
    void lockUser(UUID id);
    void unlockUser(UUID id);

    // === OPERAÇÕES DE ROLE ===
    void changeUserRole(UUID id, ChangeRoleRequest request);
    List<UserResponse> getUsersByRole(UserRole role);

    // === OPERAÇÕES DE SENHA ===
    void changePassword(UUID id, ChangePasswordRequest request);
    void resetPassword(UUID id, String newPassword);

    // === ESTATÍSTICAS ===
    long getTotalUsers();
    long getTotalActiveUsers();
    long getTotalUsersByRole(UserRole role);
}