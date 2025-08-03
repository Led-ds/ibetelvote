package com.br.ibetelvote.application.membro.dto;

import com.br.ibetelvote.domain.entities.enus.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicInfo {
    private UUID id;
    private String email;
    private UserRole role;
    private String formattedRole;
    private Boolean ativo;
}