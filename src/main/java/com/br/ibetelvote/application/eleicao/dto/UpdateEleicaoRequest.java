package com.br.ibetelvote.application.eleicao.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEleicaoRequest {

    @Size(min = 3, max = 200, message = "Nome deve ter entre 3 e 200 caracteres")
    private String nome;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    @Positive(message = "Total de elegíveis deve ser positivo")
    private Integer totalElegiveis;

    private Boolean permiteVotoBranco;
    private Boolean permiteVotoNulo;
    private Boolean exibeResultadosParciais;

    @Size(max = 2000, message = "Instruções de votação deve ter no máximo 2000 caracteres")
    private String instrucoesVotacao;
}