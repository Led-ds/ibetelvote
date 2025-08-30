package com.br.ibetelvote.application.cargo.dto;

import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoBasicInfo {

    private UUID id;
    private String nome;
    private Boolean ativo;
    private String status;

    // === CAMPOS ADICIONADOS ===

    // Categoria
    private UUID categoriaId;
    private String categoriaNome;

    // Hierarquia
    private HierarquiaCargo hierarquia;
    private String hierarquiaDisplayName;
    private String hierarquiaIcone;

    // Precedência
    private Integer ordemPrecedencia;

    // Disponibilidade
    private Boolean disponivelEleicao;

    // === MÉTODOS AUXILIARES ===

    /**
     * Retorna nome completo para exibição
     */
    public String getNomeCompleto() {
        StringBuilder nomeCompleto = new StringBuilder(nome);

        if (categoriaNome != null) {
            nomeCompleto.append(" (").append(categoriaNome).append(")");
        }

        return nomeCompleto.toString();
    }

    /**
     * Retorna descrição hierárquica resumida
     */
    public String getDescricaoHierarquica() {
        if (hierarquiaDisplayName == null) {
            return "Não definida";
        }

        StringBuilder descricao = new StringBuilder();

        if (hierarquiaIcone != null) {
            descricao.append(hierarquiaIcone).append(" ");
        }

        descricao.append(hierarquiaDisplayName);

        if (ordemPrecedencia != null) {
            descricao.append(" (").append(ordemPrecedencia).append("º)");
        }

        return descricao.toString();
    }

    /**
     * Verifica se o cargo está disponível para seleção
     */
    public boolean isDisponivelParaSelecao() {
        return ativo != null && ativo &&
                disponivelEleicao != null && disponivelEleicao;
    }

    /**
     * Retorna prioridade para ordenação
     */
    public String getChaveOrdenacao() {
        StringBuilder chave = new StringBuilder();

        // Categoria (se disponível)
        chave.append(categoriaNome != null ? categoriaNome : "ZZZZ");
        chave.append("_");

        // Ordem de precedência (pad com zeros)
        chave.append(String.format("%03d", ordemPrecedencia != null ? ordemPrecedencia : 999));
        chave.append("_");

        // Nome
        chave.append(nome);

        return chave.toString();
    }

    /**
     * Retorna CSS class para estilização
     */
    public String getCssClass() {
        if (!isDisponivelParaSelecao()) {
            return "cargo-indisponivel";
        }

        if (hierarquia != null) {
            return "cargo-" + hierarquia.name().toLowerCase();
        }

        return "cargo-padrao";
    }
}