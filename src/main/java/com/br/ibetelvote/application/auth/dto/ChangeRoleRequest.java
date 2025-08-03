package com.br.ibetelvote.application.auth.dto;

import com.br.ibetelvote.domain.entities.enus.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleRequest {
    @NotNull(message = "Nova role é obrigatória")
    private UserRole newRole;

    private String reason;
}