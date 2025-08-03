package com.br.ibetelvote.application.eleicao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoBasicInfo {

    private UUID id;
    private String nome;
    private Integer maxVotos;
    private Integer ordemVotacao;
    private Boolean obrigatorio;
    private int totalCandidatos;
    private int totalVotos;
}
