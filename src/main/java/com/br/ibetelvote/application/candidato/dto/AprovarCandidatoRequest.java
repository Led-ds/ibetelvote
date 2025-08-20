package com.br.ibetelvote.application.candidato.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AprovarCandidatoRequest {

    @NotNull(message = "Status de aprovação é obrigatório")
    private Boolean aprovado;

    private String motivoReprovacao; // Obrigatório se aprovado = false
}