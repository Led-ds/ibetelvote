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
public class MembroFilterRequest {
    private String nome;
    private String email;
    private UUID cargoAtualId; // MUDANÇA: era String cargo, agora UUID cargoAtualId
    private String nomeCargo; // ADICIONADO: para busca por nome do cargo
    private Boolean ativo;
    private Boolean hasUser;
    private int page = 0;
    private int size = 20;
    private String sort = "nome";
    private String direction = "asc";
}