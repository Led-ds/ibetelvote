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
public class VotoResponse {

    private UUID id;
    private UUID membroId;
    private UUID eleicaoId;
    private UUID cargoPretendidoId;
    private UUID candidatoId;
    private TipoVoto tipoVoto;

    @Deprecated
    private Boolean votoBranco;
    @Deprecated
    private Boolean votoNulo;

    private String hashVoto;
    private LocalDateTime dataVoto;

    private String nomeEleicao;
    private String nomeCargoPretendido;
    private String nomeCandidato;
    private String numeroCandidato;
    private String nomeMembro;
    private String dataVotoFormatada;

    private String resumoVoto;
    private boolean votoSeguro;
    private String ipMascarado;

    private boolean votoValido;
    private String statusVoto;
    private String tipoVotoDescricao;

    public boolean isVotoValido() {
        return TipoVoto.CANDIDATO.equals(tipoVoto) && candidatoId != null;
    }

    public boolean isVotoBranco() {
        return TipoVoto.BRANCO.equals(tipoVoto);
    }

    public boolean isVotoNulo() {
        return TipoVoto.NULO.equals(tipoVoto);
    }

    public String getDescricaoVoto() {
        if (isVotoValido()) {
            return String.format("Voto para %s (%s)", nomeCandidato, numeroCandidato);
        } else if (isVotoBranco()) {
            return "Voto em Branco";
        } else if (isVotoNulo()) {
            return "Voto Nulo";
        }
        return "Voto Indefinido";
    }

    public String getTipoVotoString() {
        return tipoVoto != null ? tipoVoto.name() : "INDEFINIDO";
    }
}