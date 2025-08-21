package com.br.ibetelvote.application.eleicao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EleicaoValidacaoResponse {

    private boolean podeSerAtivada;
    private List<String> motivosImpedimento;
    private boolean temCandidatosAprovados;
    private int totalCandidatosAprovados;
    private int totalCargosComCandidatos;
    private List<String> cargosComCandidatos;
    private List<String> cargosSemCandidatos;

    // Validações de período
    private boolean datasValidas;
    private boolean periodoValido;
    private boolean totalElegiveisDefinido;

    // Resumo
    private String resumoValidacao;
}