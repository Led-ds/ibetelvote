package com.br.ibetelvote.application.candidato.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatoListResponse {

    private UUID id;
    private String nomeCandidato;
    private String numeroCandidato;
    private UUID membroId;
    private String nomeMembro;
    private UUID cargoPretendidoId;
    private String nomeCargoPretendido;
    private String cargoAtualMembro;
    private Boolean ativo;
    private Boolean aprovado;
    private boolean temFotoCampanha;
    private String fotoBase64; // Para listagens com foto
    private int totalVotos;
    private double percentualVotos;
    private String statusCandidatura;
    private LocalDateTime createdAt;
}