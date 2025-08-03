package com.br.ibetelvote.application.eleicao.dto;

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
public class VotoResponse {

    private UUID id;
    private UUID membroId;
    private UUID eleicaoId;
    private UUID cargoId;
    private UUID candidatoId;
    private Boolean votoBranco;
    private Boolean votoNulo;
    private String hashVoto;
    private LocalDateTime dataVoto;

    // Dados relacionados (sem quebrar sigilo)
    private String nomeEleicao;
    private String nomeCargo;
    private String tipoVoto;
    private String dataVotoFormatada;
}