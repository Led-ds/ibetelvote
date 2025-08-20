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
public class VotoResponse {

    private UUID id;
    private UUID membroId;
    private UUID eleicaoId;
    private UUID cargoPretendidoId;
    private UUID candidatoId;
    private Boolean votoBranco;
    private Boolean votoNulo;
    private String hashVoto;
    private LocalDateTime dataVoto;

    private String nomeEleicao;
    private String nomeCargoPretendido;
    private String nomeCandidato;
    private String numeroCandidato;
    private String tipoVoto;
    private String dataVotoFormatada;

    private String resumoVoto;
    private boolean votoSeguro;
    private String ipMascarado; // IP parcialmente mascarado para auditoria

    private boolean votoValido;
    private String statusVoto; // "VÁLIDO", "BRANCO", "NULO"

    /**
     * Verifica se é um voto válido (tem candidato)
     */
    public boolean isVotoValido() {
        return candidatoId != null && !Boolean.TRUE.equals(votoBranco) && !Boolean.TRUE.equals(votoNulo);
    }

    /**
     * Retorna descrição amigável do voto
     */
    public String getDescricaoVoto() {
        if (isVotoValido()) {
            return String.format("Voto para %s (%s)", nomeCandidato, numeroCandidato);
        } else if (Boolean.TRUE.equals(votoBranco)) {
            return "Voto em Branco";
        } else if (Boolean.TRUE.equals(votoNulo)) {
            return "Voto Nulo";
        }
        return "Voto Indefinido";
    }
}