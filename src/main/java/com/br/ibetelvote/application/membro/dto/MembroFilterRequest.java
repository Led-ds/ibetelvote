package com.br.ibetelvote.application.membro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroFilterRequest {
    private String nome;
    private String email;
    private Boolean ativo;
    private int page = 0;
    private int size = 20;
    private String sort = "nome";
    private String direction = "asc";
}