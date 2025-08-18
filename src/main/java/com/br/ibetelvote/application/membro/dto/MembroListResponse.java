package com.br.ibetelvote.application.membro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroListResponse {
    private UUID id;
    private String nome;
    private String email;
    private String cargo;
    private String departamento;
    private LocalDate dataNascimento;
    private String fotoBase64;
    private String primaryPhone;
    private Boolean ativo;
    private boolean hasUser;
    private boolean hasPhoto;
    private String userRole; // Role do usuário associado
}