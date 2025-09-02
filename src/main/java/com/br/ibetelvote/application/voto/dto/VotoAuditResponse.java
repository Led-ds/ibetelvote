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
public class VotoAuditResponse {

    private UUID id;
    private UUID eleicaoId;
    private UUID cargoPretendidoId;

    private TipoVoto tipoVoto;

    // Mantendo para compatibilidade
    @Deprecated
    private Boolean votoBranco;
    @Deprecated
    private Boolean votoNulo;

    private LocalDateTime dataVoto;

    // Dados seguros para auditoria
    private String nomeEleicao;
    private String nomeCargoPretendido;
    private String dataVotoFormatada;
    private boolean votoValido;
    private String tipoVotoDescricao;
    private String statusVoto;

}