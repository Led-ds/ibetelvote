package com.br.ibetelvote.application.eleicao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimiteVotacaoResponse {

    private UUID cargoId;
    private String nomeCargo;
    private Integer limiteVotos;
    private Integer votosJaDados;
    private Integer votosRestantes;
    private boolean podeVotarMais;
    private List<UUID> candidatosJaVotados;

    public int getVotosRestantes() {
        if (limiteVotos == null || votosJaDados == null) return 0;
        return Math.max(0, limiteVotos - votosJaDados);
    }

    public boolean isPodeVotarMais() {
        return getVotosRestantes() > 0;
    }
}