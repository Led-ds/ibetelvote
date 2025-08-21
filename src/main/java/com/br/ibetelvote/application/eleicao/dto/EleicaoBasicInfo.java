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
public class EleicaoBasicInfo {

    private UUID id;
    private String nome;
    private Boolean ativa;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private String statusDescricao;
    private boolean votacaoAberta;
    private boolean temCandidatosAprovados;
}