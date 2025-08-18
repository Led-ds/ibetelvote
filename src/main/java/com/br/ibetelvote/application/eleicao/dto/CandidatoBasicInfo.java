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
public class CandidatoBasicInfo {

    private UUID id;
    private String nomeCandidato;
    private String numeroCandidato;
    private Boolean temFotoCampanha;
    private Boolean aprovado;
    private int totalVotos;
    private String nomeCargoRetendido;
}