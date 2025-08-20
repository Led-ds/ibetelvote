package com.br.ibetelvote.application.voto.dto;

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
    private Boolean votoBranco;
    private Boolean votoNulo;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private String tipoVoto; // "VALIDO", "BRANCO", "NULO"
    private boolean apenasVotosSeguro;

    // === PAGINAÇÃO ===
    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sort = "dataVoto";

    @Builder.Default
    private String direction = "desc";
}
