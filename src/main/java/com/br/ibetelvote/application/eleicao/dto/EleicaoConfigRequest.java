package com.br.ibetelvote.application.eleicao.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EleicaoConfigRequest {

    @Positive(message = "Total de elegíveis deve ser positivo")
    private Integer totalElegiveis;

    private Boolean permiteVotoBranco;
    private Boolean permiteVotoNulo;
    private Boolean exibeResultadosParciais;
}