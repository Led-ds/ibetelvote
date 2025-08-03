package com.br.ibetelvote.application.eleicao.dto;

import jakarta.validation.constraints.NotNull;
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
public class VotarRequest {

    @NotNull(message = "Eleição é obrigatória")
    private UUID eleicaoId;

    @NotNull(message = "Lista de votos é obrigatória")
    private List<VotoIndividual> votos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VotoIndividual {

        @NotNull(message = "Cargo é obrigatório")
        private UUID cargoId;

        private UUID candidatoId; // null para voto branco/nulo
        private Boolean votoBranco = false;
        private Boolean votoNulo = false;
    }
}