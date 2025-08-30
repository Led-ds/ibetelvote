package com.br.ibetelvote.application.categoria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaBasicInfo {

    private UUID id;
    private String nome;
    private Integer ordemExibicao;
    private Boolean ativo;
    private String status;
    private long totalCargos;
}