package com.br.ibetelvote.application.auth.dto;

import com.br.ibetelvote.domain.entities.enus.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class UserProfileResponse {
    UUID id;
    String nome;
    String email;
    UserRole role;
    String foto;
    String cargo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dataNascimento;

    Boolean ativo;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt;
}
