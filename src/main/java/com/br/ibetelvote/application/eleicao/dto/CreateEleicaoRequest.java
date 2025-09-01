package com.br.ibetelvote.application.eleicao.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEleicaoRequest {

    @NotBlank(message = "Nome da eleição é obrigatório")
    @Size(min = 3, max = 200, message = "Nome deve ter entre 3 e 200 caracteres")
    private String nome;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    @NotNull(message = "Data de início é obrigatória")
    @Future(message = "Data de início deve ser no futuro")
    private LocalDateTime dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    @Future(message = "Data de fim deve ser no futuro")
    private LocalDateTime dataFim;

    @Positive(message = "Total de elegíveis deve ser positivo")
    private Integer totalElegiveis;

    @Builder.Default
    private Boolean permiteVotoBranco = true;
    @Builder.Default
    private Boolean permiteVotoNulo = true;
    @Builder.Default
    private Boolean exibeResultadosParciais = false;

    @Size(max = 2000, message = "Instruções de votação deve ter no máximo 2000 caracteres")
    private String instrucoesVotacao;
}