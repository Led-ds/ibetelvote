package com.br.ibetelvote.domain.entities.enums;

import lombok.Getter;

@Getter
public enum HierarquiaCargo {

    PASTORAL("Pastoral", "Liderança pastoral da igreja", 1, "👨‍💼"),
    PRESBITERAL("Presbiteral", "Autoridade espiritual intermediária", 2, "🤝"),
    DIACONAL("Diaconal", "Serviço e assistência à comunidade", 3, "🛡️"),
    LIDERANCA("Liderança", "Liderança de ministérios específicos", 4, "⭐"),
    AUXILIAR("Auxiliar", "Funções de apoio e suporte", 5, "🔧"),
    ADMINISTRATIVO("Administrativo", "Gestão e administração", 6, "📋");

    private final String displayName;
    private final String descricao;
    private final Integer ordem;
    private final String icone;

    HierarquiaCargo(String displayName, String descricao, Integer ordem, String icone) {
        this.displayName = displayName;
        this.descricao = descricao;
        this.ordem = ordem;
        this.icone = icone;
    }

    /**
     *
     * @param destino hierarquia de destino
     * @return true se pode eleger, false caso contrário
     */
    public boolean podeElegerPara(HierarquiaCargo destino) {
        if (destino == null) {
            return false;
        }
        // Pode eleger para mesmo nível ou níveis superiores (ordem menor)
        return this.ordem <= destino.ordem;
    }

    /**
     *
     * @param outra hierarquia para comparar
     * @return true se é superior, false caso contrário
     */
    public boolean ehSuperiorA(HierarquiaCargo outra) {
        if (outra == null) {
            return true;
        }
        return this.ordem < outra.ordem;
    }

    /**
     * @param outra hierarquia para comparar
     * @return true se é igual ou superior, false caso contrário
     */
    public boolean ehIgualOuSuperiorA(HierarquiaCargo outra) {
        if (outra == null) {
            return true;
        }
        return this.ordem <= outra.ordem;
    }

    /**
     * @return próxima hierarquia ou null se já for a mais alta
     */
    public HierarquiaCargo getProximaHierarquia() {
        for (HierarquiaCargo hierarquia : values()) {
            if (hierarquia.ordem == this.ordem - 1) {
                return hierarquia;
            }
        }
        return null; // Já é a hierarquia mais alta
    }

    /**
     * @return hierarquia anterior ou null se já for a mais baixa
     */
    public HierarquiaCargo getHierarquiaAnterior() {
        for (HierarquiaCargo hierarquia : values()) {
            if (hierarquia.ordem == this.ordem + 1) {
                return hierarquia;
            }
        }
        return null; // Já é a hierarquia mais baixa
    }

    /**
     * @param nome nome da hierarquia
     * @return hierarquia encontrada ou null
     */
    public static HierarquiaCargo fromString(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }

        for (HierarquiaCargo hierarquia : values()) {
            if (hierarquia.name().equalsIgnoreCase(nome.trim()) ||
                    hierarquia.displayName.equalsIgnoreCase(nome.trim())) {
                return hierarquia;
            }
        }
        return null;
    }

    /**
     * @return array de hierarquias elegíveis
     */
    public HierarquiaCargo[] getHierarquiasElegiveis() {
        return java.util.Arrays.stream(values())
                .filter(h -> h.podeElegerPara(this))
                .toArray(HierarquiaCargo[]::new);
    }

    /**
     * @return true se é ministerial, false caso contrário
     */
    public boolean ehMinisterial() {
        return this == PASTORAL || this == PRESBITERAL || this == DIACONAL;
    }

    /**
     * @return true se é liderança, false caso contrário
     */
    public boolean ehLideranca() {
        return this == PASTORAL || this == PRESBITERAL || this == LIDERANCA;
    }

    /**
     * @return string com ícone e nome
     */
    public String getDisplayNameComIcone() {
        return String.format("%s %s", icone, displayName);
    }

    /**
     * @return código da cor hex
     */
    public String getCor() {
        return switch (this) {
            case PASTORAL -> "#8B5CF6";      // Roxo
            case PRESBITERAL -> "#3B82F6";   // Azul
            case DIACONAL -> "#10B981";      // Verde
            case LIDERANCA -> "#F59E0B";     // Amarelo
            case AUXILIAR -> "#6B7280";      // Cinza
            case ADMINISTRATIVO -> "#EF4444"; // Vermelho
        };
    }

    /**
     * @return nível de acesso
     */
    public int getNivelAcesso() {
        return ordem;
    }

    /**
     * @param hierarquia string para validar
     * @return true se válida, false caso contrário
     */
    public static boolean isValida(String hierarquia) {
        return fromString(hierarquia) != null;
    }

    /**
     * @return array ordenado por precedência
     */
    public static HierarquiaCargo[] porOrdemDePrecedencia() {
        return java.util.Arrays.stream(values())
                .sorted(java.util.Comparator.comparing(HierarquiaCargo::getOrdem))
                .toArray(HierarquiaCargo[]::new);
    }

    @Override
    public String toString() {
        return displayName;
    }
}