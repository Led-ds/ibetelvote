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
    private String cargo; // Nome do cargo atual (não mais o campo String direto)
    private Boolean ativo;

    private String email;
    private String telefone;

    private UUID cargoAtualId;
    private Boolean temCargo;
}