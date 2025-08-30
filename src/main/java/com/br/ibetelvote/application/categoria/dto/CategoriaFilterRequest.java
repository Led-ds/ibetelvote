package com.br.ibetelvote.application.categoria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaFilterRequest {

    private String nome;
    private Boolean ativo;
    private Integer ordemMin;
    private Integer ordemMax;
    private Boolean temCargos;
    private Boolean temCargosAtivos;
    private Boolean temCargosDisponiveis;
}