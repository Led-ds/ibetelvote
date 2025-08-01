package com.br.ibetelvote.application.membro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroResponse {
    private UUID id;
    private String nome;
    private String email;
    private String cargo;
    private LocalDate dataNascimento;
    private String foto;
    private String telefone;
    private String endereco;
    private Boolean ativo;
    private UUID userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}