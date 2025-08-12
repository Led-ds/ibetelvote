package com.br.ibetelvote.application.membro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidarMembroResponse {

    private UUID membroId;
    private String nome;
    private String email;
    private String cpf;
    private boolean podeCreateUser;
    private String message;
}