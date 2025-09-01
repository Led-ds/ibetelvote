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
public class CandidatoFilterRequest {

    private String nomeCandidato;
    private UUID eleicaoId;
    private UUID cargoPretendidoId;
    private UUID membroId;
    private Boolean ativo;
    private Boolean aprovado;
    private String numeroCandidato;
    private Boolean temFoto;
    private Boolean candidaturaCompleta;

    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sort = "nomeCandidato";
    @Builder.Default
    private String direction = "asc";
}