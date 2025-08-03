package com.br.ibetelvote.application.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroBasicInfo {
    private UUID id;
    private String nome;
    private String foto;
    private String cargo;
    private Boolean ativo;
}