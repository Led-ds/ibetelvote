package com.br.ibetelvote.application.membro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
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
public class UpdateMembroRequest {

    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Size(min = 11, max = 14, message = "CPF deve ter formato válido")
    private String cpf;

    @Email(message = "Email deve ser válido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    private String email;

    @Past(message = "Data de nascimento deve ser no passado")
    private LocalDate dataNascimento;

    // Cargo atual
    private UUID cargoAtualId;

    // Dados da igreja
    @Size(max = 100, message = "Departamento deve ter no máximo 100 caracteres")
    private String departamento;

    private LocalDate dataBatismo;
    private LocalDate dataMembroDesde;

    // Dados de contato
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @Size(max = 20, message = "Celular deve ter no máximo 20 caracteres")
    private String celular;

    @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
    private String endereco;

    @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
    private String cidade;

    @Size(max = 2, message = "Estado deve ter 2 caracteres")
    private String estado;

    @Size(max = 10, message = "CEP deve ter no máximo 10 caracteres")
    private String cep;

    @Size(max = 1000, message = "Observações devem ter no máximo 1000 caracteres")
    private String observacoes;

    private Boolean ativo;
}