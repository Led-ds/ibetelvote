package com.br.ibetelvote.application.candidato.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatoStatsResponse {

    private long totalCandidatos;
    private long candidatosAtivos;
    private long candidatosInativos;
    private long candidatosAprovados;
    private long candidatosPendentes;
    private long candidatosReprovados;
    private long candidatosComFoto;
    private long candidatosSemFoto;
    private long candidatosComNumero;
    private long candidatosSemNumero;
    private long candidatosElegiveis;
    private Map<String, Long> candidatosPorCargo;
    private Map<String, Long> candidatosPorStatus;
    private double percentualAprovacao;
    private double percentualComFoto;
}