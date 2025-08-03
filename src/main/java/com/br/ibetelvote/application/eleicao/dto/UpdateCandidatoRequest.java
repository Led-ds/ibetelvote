package com.br.ibetelvote.application.eleicao.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCandidatoRequest {

    @Size(max = 10, message = "Número do candidato deve ter no máximo 10 caracteres")
    private String numeroCandidato;

    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nomeCandidato;

    @Size(max = 100, message = "Nome do cargo pretendido deve ter no máximo 100 caracteres")
    private String nomeCargoRetendido;

    @Size(max = 2000, message = "Descrição da candidatura deve ter no máximo 2000 caracteres")
    private String descricaoCandidatura;

    @Size(max = 3000, message = "Propostas deve ter no máximo 3000 caracteres")
    private String propostas;

    @Size(max = 2000, message = "Experiência deve ter no máximo 2000 caracteres")
    private String experiencia;
}