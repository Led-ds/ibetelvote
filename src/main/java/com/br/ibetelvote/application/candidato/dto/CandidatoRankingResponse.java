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
public class CandidatoRankingResponse {

    private int posicao;
    private UUID candidatoId;
    private String nomeCandidato;
    private String numeroCandidato;
    private String nomeCargoPretendido;
    private int totalVotos;
    private double percentualVotos;
    private boolean temFotoCampanha;
    private String fotoBase64;
    private boolean eleito; // Se está nas primeiras posições para ser eleito
}