package com.br.ibetelvote.application.membro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMembroRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    private String email;

    @Size(max = 100, message = "Cargo deve ter no máximo 100 caracteres")
    private String cargo;

    @Past(message = "Data de nascimento deve ser no passado")
    private LocalDate dataNascimento;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
    private String endereco;
}