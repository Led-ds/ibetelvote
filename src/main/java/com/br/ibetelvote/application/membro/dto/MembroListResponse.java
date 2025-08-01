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
    private LocalDate dataNascimento;
    private String foto;
    private Boolean ativo;
    private boolean hasUser;
}