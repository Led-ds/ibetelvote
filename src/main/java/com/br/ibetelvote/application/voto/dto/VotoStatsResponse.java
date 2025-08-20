package com.br.ibetelvote.application.voto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VotoStatsResponse {

    private long totalVotos;
    private long votosValidos;
    private long votosBranco;
    private long votosNulo;
    private long votantesUnicos;
    private double percentualParticipacao;
    private double percentualVotosValidos;
    private double percentualVotosBranco;
    private double percentualVotosNulo;

    // === ESTATÍSTICAS POR CARGO ===
    private Map<String, Long> votosPorCargo;
    private Map<String, Double> percentualPorCargo;

    // === ESTATÍSTICAS TEMPORAIS ===
    private Map<String, Long> votosPorHora;
    private Map<String, Long> votosPorDia;

    // === SEGURANÇA ===
    private long votosComDadosCompletos;
    private long votosSemHash;
    private Map<String, Long> distribuicaoPorIP;
}