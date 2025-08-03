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
    private int page = 0;
    private int size = 20;
    private String sort = "dataInicio";
    private String direction = "desc";
}