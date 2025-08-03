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
public class EleicaoListResponse {

    private UUID id;
    private String nome;
    private String descricao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private Boolean ativa;
    private Integer totalElegiveis;
    private Integer totalVotantes;
    private String statusDescricao;
    private double percentualParticipacao;
    private int totalCargos;
    private int totalCandidatos;
    private boolean votacaoAberta;
}