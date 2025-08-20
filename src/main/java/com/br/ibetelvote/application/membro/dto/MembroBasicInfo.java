package com.br.ibetelvote.application.membro.dto;

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
    private String email;
    private UUID cargoAtualId;
    private String nomeCargoAtual;
    private Boolean ativo;
    private boolean hasUser;
}
