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
public class EleicaoStatsResponse {

    private UUID id;
    private String nome;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private Integer totalElegiveis;
    private Integer totalVotantes;
    private double percentualParticipacao;

    // Estatísticas de candidatos
    private int totalCandidatos;
    private int totalCandidatosAprovados;
    private int totalCandidatosPendentes;
    private int totalCandidatosReprovados;

    // Estatísticas de cargos
    private int totalCargosComCandidatos;

    // Estatísticas de votos
    private int totalVotosContabilizados;
    private int totalVotosBrancos;
    private int totalVotosNulos;

    // Status
    private String statusDescricao;
    private boolean votacaoAberta;
    private long duracaoEmHoras;
}