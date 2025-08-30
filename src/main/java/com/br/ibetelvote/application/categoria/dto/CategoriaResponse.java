package com.br.ibetelvote.application.categoria.dto;

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
public class CategoriaResponse {

    private UUID id;
    private String nome;
    private String descricao;
    private Integer ordemExibicao;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Campos computados
    private String status;
    private String displayName;
    private String resumo;
    private boolean temInformacoesCompletas;
    private long totalCargos;
    private long totalCargosAtivos;
    private long totalCargosDisponiveis;
    private String estatisticas;
    private boolean podeSerRemovida;
}