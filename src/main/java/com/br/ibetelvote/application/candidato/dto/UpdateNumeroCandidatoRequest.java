package com.br.ibetelvote.application.candidato.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNumeroCandidatoRequest {

    @NotBlank(message = "Número do candidato é obrigatório")
    @Size(max = 10, message = "Número deve ter no máximo 10 caracteres")
    private String numeroCandidato;
}