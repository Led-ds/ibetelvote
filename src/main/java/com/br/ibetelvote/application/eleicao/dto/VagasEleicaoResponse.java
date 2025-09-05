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
public class VagasEleicaoResponse {

    private UUID eleicaoId;
    private String nomeEleicao;
    private List<VagaCargoInfo> vagasConfiguradas;
    private int totalVagasConfiguradas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VagaCargoInfo {
        private UUID cargoId;
        private String nomeCargo;
        private Integer numeroVagas;
        private boolean temCandidatos;
        private int totalCandidatos;
    }

    public int getTotalVagasConfiguradas() {
        return vagasConfiguradas != null ?
                vagasConfiguradas.stream()
                        .mapToInt(VagaCargoInfo::getNumeroVagas)
                        .sum() : 0;
    }
}