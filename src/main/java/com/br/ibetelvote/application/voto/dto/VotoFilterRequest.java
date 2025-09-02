package com.br.ibetelvote.application.voto.dto;

import com.br.ibetelvote.domain.entities.enums.TipoVoto;
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
public class VotoFilterRequest {

    private UUID eleicaoId;
    private UUID cargoPretendidoId;
    private UUID candidatoId;
    private UUID membroId;

    private TipoVoto tipoVoto;

    // Mantendo para compatibilidade
    @Deprecated
    private Boolean votoBranco;
    @Deprecated
    private Boolean votoNulo;

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private boolean apenasVotosSeguro;
    private boolean apenasComHash;

    // Paginação
    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sort = "dataVoto";

    @Builder.Default
    private String direction = "desc";

    public boolean hasFilters() {
        return eleicaoId != null || cargoPretendidoId != null ||
                candidatoId != null || membroId != null ||
                tipoVoto != null || dataInicio != null || dataFim != null;
    }

    public boolean isValidDateRange() {
        if (dataInicio == null || dataFim == null) return true;
        return dataInicio.isBefore(dataFim) || dataInicio.equals(dataFim);
    }
}
