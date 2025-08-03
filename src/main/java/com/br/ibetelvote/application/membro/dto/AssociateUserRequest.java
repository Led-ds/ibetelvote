package com.br.ibetelvote.application.membro.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssociateUserRequest {
    @NotNull(message = "ID do usuário é obrigatório")
    private UUID userId;
}