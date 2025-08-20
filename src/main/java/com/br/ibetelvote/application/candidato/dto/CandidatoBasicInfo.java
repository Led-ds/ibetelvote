package com.br.ibetelvote.application.candidato.dto;

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
    private UUID cargoPretendidoId;
    private String nomeCargoPretendido;
    private Boolean ativo;
    private Boolean aprovado;
    private boolean temFotoCampanha;
    private int totalVotos;
    private String statusCandidatura;
}