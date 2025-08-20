package com.br.ibetelvote.application.voto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidarVotacaoResponse {

    private boolean votacaoValida;
    private List<String> erros;
    private List<String> avisos;
    private int totalVotos;
    private int votosValidos;
    private List<String> cargosComProblemas;

    // === INFORMAÇÕES ADICIONAIS ===
    private boolean membroElegivel;
    private boolean eleicaoDisponivel;
    private boolean jaVotou;

    /**
     * Verifica se a votação pode prosseguir
     */
    public boolean podeProsseguir() {
        return votacaoValida && erros.isEmpty();
    }

    /**
     * Retorna resumo da validação
     */
    public String getResumoValidacao() {
        if (podeProsseguir()) {
            return String.format("Votação válida com %d votos", totalVotos);
        } else {
            return String.format("Votação inválida: %d erro(s)", erros.size());
        }
    }
}