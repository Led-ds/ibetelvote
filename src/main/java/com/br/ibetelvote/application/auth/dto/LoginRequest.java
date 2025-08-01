package com.br.ibetelvote.application.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        String email,

        @NotBlank(message = "Password é obrigatório")
        @Size(min = 6, message = "Password deve ter pelo menos 6 caracteres")
        String password
) {}