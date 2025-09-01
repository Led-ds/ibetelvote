package com.br.ibetelvote.application.voto.dto;

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

        @NotNull(message = "Cargo pretendido é obrigatório")
        private UUID cargoPretendidoId;

        private UUID candidatoId;

        @Builder.Default
        private Boolean votoBranco = false;

        @Builder.Default
        private Boolean votoNulo = false;

        /**
         * Valida se apenas um tipo de voto foi especificado
         */
        public boolean isVotoValido() {
            int tiposVoto = 0;
            if (Boolean.TRUE.equals(votoBranco)) tiposVoto++;
            if (Boolean.TRUE.equals(votoNulo)) tiposVoto++;
            if (candidatoId != null) tiposVoto++;

            return tiposVoto == 1;
        }

        /**
         * Retorna o tipo de voto como string
         */
        public String getTipoVoto() {
            if (candidatoId != null) return "CANDIDATO";
            if (Boolean.TRUE.equals(votoBranco)) return "BRANCO";
            if (Boolean.TRUE.equals(votoNulo)) return "NULO";
            return "INDEFINIDO";
        }
    }

    /**
     * Valida se há votos duplicados para o mesmo cargo
     */
    public boolean temVotosDuplicados() {
        if (votos == null) return false;

        return votos.stream()
                .map(VotoIndividual::getCargoPretendidoId)
                .distinct()
                .count() != votos.size();
    }

    /**
     * Retorna lista de cargos com votos duplicados
     */
    public List<UUID> getCargosDuplicados() {
        if (votos == null) return List.of();

        return votos.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        VotoIndividual::getCargoPretendidoId,
                        java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(java.util.Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
    }
}