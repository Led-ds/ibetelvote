package com.br.ibetelvote.application.membro.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroUploadFotoRequest {

    @NotNull(message = "Dados da foto são obrigatórios")
    private byte[] fotoData;

    @NotNull(message = "Tipo da foto é obrigatório")
    @Size(max = 50, message = "Tipo deve ter no máximo 50 caracteres")
    private String fotoTipo;

    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    private String fotoNome;
}