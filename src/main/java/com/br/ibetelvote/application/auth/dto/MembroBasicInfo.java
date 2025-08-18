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
    private String fotoBase64;
    private String cargo;
    private Boolean ativo;
}