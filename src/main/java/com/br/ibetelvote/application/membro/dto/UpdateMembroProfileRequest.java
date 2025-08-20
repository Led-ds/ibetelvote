package com.br.ibetelvote.application.membro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMembroProfileRequest {

    // Dados pessoais básicos
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    private String email;

    private LocalDate dataNascimento;

    // Dados da igreja
    private UUID cargoAtualId; // MUDANÇA: era String cargo
    private String departamento;
    private LocalDate dataBatismo;
    private LocalDate dataMembroDesde;

    // Contato
    private String telefone;
    private String celular;

    // Endereço
    private String endereco;
    private String cidade;
    private String estado;
    private String cep;

    // Observações
    private String observacoes;
}