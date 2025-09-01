package com.br.ibetelvote.application.eleicao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EleicaoFilterRequest {

    private String nome;
    private Boolean ativa;
    private String status; // "aberta", "encerrada", "futura"
    private Boolean temCandidatos;
    private Boolean temCandidatosAprovados;
    @Builder.Default
    private int page = 0;
    @Builder.Default
    private int size = 20;
    @Builder.Default
    private String sort = "dataInicio";
    @Builder.Default
    private String direction = "desc";
}