package com.br.ibetelvote.application.candidato.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCandidatoRequest {

    @NotNull(message = "Membro é obrigatório")
    private UUID membroId;

    @NotNull(message = "Eleição é obrigatória")
    private UUID eleicaoId;

    @NotNull(message = "Cargo pretendido é obrigatório")
    private UUID cargoPretendidoId;

    @NotBlank(message = "Nome do candidato é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nomeCandidato;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricaoCandidatura;

    @Size(max = 2000, message = "Propostas devem ter no máximo 2000 caracteres")
    private String propostas;

    @Size(max = 1000, message = "Experiência deve ter no máximo 1000 caracteres")
    private String experiencia;

    @Size(max = 10, message = "Número do candidato deve ter no máximo 10 caracteres")
    private String numeroCandidato;

    @Builder.Default
    private Boolean ativo = true;
}