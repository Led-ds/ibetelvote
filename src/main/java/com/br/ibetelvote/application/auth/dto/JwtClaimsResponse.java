package com.br.ibetelvote.application.auth.dto;

import com.br.ibetelvote.domain.entities.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@Getter
@Setter
public class JwtClaimsResponse {
    UUID userId;
    String email;
    String nome;
    UserRole role;
    Boolean ativo;
}
