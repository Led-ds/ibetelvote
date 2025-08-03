package com.br.ibetelvote.application.eleicao.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCargoRequest {

    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    @Positive(message = "Máximo de votos deve ser positivo")
    private Integer maxVotos;

    @Min(value = 1, message = "Ordem de votação deve ser pelo menos 1")
    private Integer ordemVotacao;

    private Boolean permiteVotoBranco;
    private Boolean obrigatorio;

    @Size(max = 2000, message = "Instruções específicas deve ter no máximo 2000 caracteres")
    private String instrucoesEspecificas;
}