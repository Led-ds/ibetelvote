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
public class VotoAuditResponse {

    private UUID id;
    private UUID eleicaoId;
    private UUID cargoPretendidoId;
    private Boolean votoBranco;
    private Boolean votoNulo;
    private LocalDateTime dataVoto;

    // === DADOS SEGUROS PARA AUDITORIA ===
    private String nomeEleicao;
    private String nomeCargoPretendido;
    private String tipoVoto;
    private String dataVotoFormatada;
    private boolean votoValido;

    // === SEM DADOS SENSÍVEIS ===
    // Não inclui: membroId, candidatoId, hashVoto, IP
}