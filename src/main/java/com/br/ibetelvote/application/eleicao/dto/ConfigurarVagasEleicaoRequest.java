package com.br.ibetelvote.application.eleicao.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurarVagasEleicaoRequest {

    @NotNull(message = "Lista de configurações de vagas é obrigatória")
    @Valid
    private List<VagaCargo> vagasPorCargo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VagaCargo {

        @NotNull(message = "ID do cargo é obrigatório")
        private UUID cargoId;

        @NotNull(message = "Número de vagas é obrigatório")
        @Min(value = 0, message = "Número de vagas deve ser maior ou igual a zero")
        private Integer numeroVagas;

        // Campos informativos (opcional)
        private String nomeCargo;
    }

    /**
     * Converte para Map usado pela entidade
     */
    public Map<UUID, Integer> toVagasMap() {
        return vagasPorCargo.stream()
                .collect(java.util.stream.Collectors.toMap(
                        VagaCargo::getCargoId,
                        VagaCargo::getNumeroVagas
                ));
    }

    /**
     * Valida se não há cargos duplicados
     */
    public boolean temCargosDuplicados() {
        if (vagasPorCargo == null) return false;

        return vagasPorCargo.stream()
                .map(VagaCargo::getCargoId)
                .distinct()
                .count() != vagasPorCargo.size();
    }
}