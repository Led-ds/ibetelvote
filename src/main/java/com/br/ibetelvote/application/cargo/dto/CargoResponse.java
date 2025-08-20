package com.br.ibetelvote.application.cargo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoResponse {

    private UUID id;
    private String nome;
    private String descricao;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Campos computados
    private String status;
    private String displayName;
    private String resumo;
    private boolean temInformacoesCompletas;
    private boolean podeSerUsadoEmEleicoes;
}