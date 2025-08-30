package com.br.ibetelvote.application.cargo.dto;

import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
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

    // === NOVOS CAMPOS ===

    // Categoria
    private UUID categoriaId;
    private String categoriaNome;
    private Integer categoriaOrdemExibicao;

    // Hierarquia
    private HierarquiaCargo hierarquia;
    private String hierarquiaDisplayName;
    private String hierarquiaCor;
    private String hierarquiaIcone;
    private Integer hierarquiaNivel;

    // Precedência
    private Integer ordemPrecedencia;
    private String requisitosCargo;
    private String requisitoResumo;

    // Elegibilidade
    private List<String> elegibilidade;
    private String elegibilidadeFormatada;
    private Boolean disponivelEleicao;

    // === CAMPOS COMPUTADOS ===

    // Status
    private String status;
    private String displayName;
    private String resumo;
    private boolean temInformacoesCompletas;
    private boolean podeSerUsadoEmEleicoes;
    private boolean temCategoria;

    // Estatísticas
    private long totalCandidatos;
    private boolean temCandidatos;
    private String estatisticasResumo;

    // Validações
    private boolean podeSerRemovido;
    private boolean podeSerEditado;
    private boolean podeSerPromovido;
    private HierarquiaCargo proximaHierarquiaSugerida;

    // === MÉTODOS AUXILIARES PARA FRONTEND ===

    /**
     * Retorna CSS class baseada no status
     */
    public String getStatusCssClass() {
        if (!ativo) return "inactive";
        if (!disponivelEleicao) return "unavailable";
        if (!temInformacoesCompletas) return "incomplete";
        return "active";
    }

    /**
     * Retorna prioridade de exibição
     */
    public Integer getPrioridadeExibicao() {
        int prioridade = 0;

        if (categoriaOrdemExibicao != null) {
            prioridade += categoriaOrdemExibicao * 1000;
        }

        if (ordemPrecedencia != null) {
            prioridade += ordemPrecedencia * 100;
        }

        if (hierarquiaNivel != null) {
            prioridade += hierarquiaNivel * 10;
        }

        return prioridade;
    }

    /**
     * Verifica se é cargo ministerial
     */
    public boolean isCargoMinisterial() {
        return hierarquia != null &&
                (hierarquia == HierarquiaCargo.PASTORAL ||
                        hierarquia == HierarquiaCargo.PRESBITERAL ||
                        hierarquia == HierarquiaCargo.DIACONAL);
    }

    /**
     * Verifica se é cargo de liderança
     */
    public boolean isCargoLideranca() {
        return hierarquia != null &&
                (hierarquia == HierarquiaCargo.PASTORAL ||
                        hierarquia == HierarquiaCargo.PRESBITERAL ||
                        hierarquia == HierarquiaCargo.LIDERANCA);
    }

    /**
     * Retorna tooltip informativo
     */
    public String getTooltip() {
        StringBuilder tooltip = new StringBuilder();
        tooltip.append(nome);

        if (categoriaNome != null) {
            tooltip.append(" - ").append(categoriaNome);
        }

        if (hierarquiaDisplayName != null) {
            tooltip.append(" (").append(hierarquiaDisplayName).append(")");
        }

        if (ordemPrecedencia != null) {
            tooltip.append(" - Ordem: ").append(ordemPrecedencia);
        }

        return tooltip.toString();
    }

    /**
     * Retorna resumo da elegibilidade para exibição
     */
    public String getElegibilidadeResumo() {
        if (elegibilidade == null || elegibilidade.isEmpty()) {
            return "Não definida";
        }

        if (elegibilidade.size() <= 3) {
            return String.join(", ", elegibilidade);
        }

        return elegibilidade.get(0) + ", " + elegibilidade.get(1) +
                " e mais " + (elegibilidade.size() - 2) + " níveis";
    }

    /**
     * Verifica se aceita candidatura de um nível específico
     */
    public boolean aceitaCandidaturaDe(String nivel) {
        return elegibilidade != null && elegibilidade.contains(nivel.toUpperCase());
    }

    /**
     * Retorna indicador visual de complexidade do cargo
     */
    public String getIndicadorComplexidade() {
        int complexidade = 0;

        if (hierarquia != null) {
            complexidade += switch (hierarquia) {
                case PASTORAL -> 5;
                case PRESBITERAL -> 4;
                case DIACONAL -> 3;
                case LIDERANCA -> 2;
                case ADMINISTRATIVO -> 2;
                case AUXILIAR -> 1;
            };
        }

        if (elegibilidade != null) {
            complexidade += Math.min(elegibilidade.size(), 3);
        }

        if (requisitosCargo != null && !requisitosCargo.trim().isEmpty()) {
            complexidade += 1;
        }

        return switch (complexidade) {
            case 0, 1, 2 -> "Simples";
            case 3, 4, 5 -> "Moderado";
            case 6, 7, 8 -> "Complexo";
            default -> "Muito Complexo";
        };
    }

    /**
     * Retorna cor do badge baseada na hierarquia
     */
    public String getBadgeColor() {
        if (hierarquiaCor != null) {
            return hierarquiaCor;
        }

        if (!ativo) return "#6B7280"; // Cinza
        if (!disponivelEleicao) return "#F59E0B"; // Amarelo

        return "#10B981"; // Verde padrão
    }
}