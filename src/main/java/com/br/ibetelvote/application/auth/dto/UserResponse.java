package com.br.ibetelvote.application.auth.dto;

import com.br.ibetelvote.domain.entities.enus.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private UserRole role;
    private String formattedRole;
    private Boolean ativo;
    private Boolean accountNonExpired;
    private Boolean accountNonLocked;
    private Boolean credentialsNonExpired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Dados do membro associado (se existir)
    private MembroBasicInfo membro;
}