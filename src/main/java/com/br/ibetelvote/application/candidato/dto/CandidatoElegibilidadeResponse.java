package com.br.ibetelvote.application.candidato.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatoElegibilidadeResponse {

    private UUID membroId;
    private String nomeMembro;
    private String cargoAtualMembro;
    private UUID cargoPretendidoId;
    private String nomeCargoPretendido;
    private boolean elegivel;
    private boolean membroAtivo;
    private boolean cargoAtivo;
    private boolean membroPodeSeCandidarParaCargo;
    private List<String> motivosInelegibilidade;
    private String resumoElegibilidade;
}