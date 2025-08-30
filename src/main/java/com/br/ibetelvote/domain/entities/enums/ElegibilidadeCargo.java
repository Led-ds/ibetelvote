package com.br.ibetelvote.domain.entities.enums;

import lombok.Getter;

/**
 * Enum representando os níveis de elegibilidade para candidaturas a cargos.
 *
 * Define os diferentes status/cargos que uma pessoa pode ter e que
 * determinam sua elegibilidade para candidatar-se a outros cargos.
 */
@Getter
public enum ElegibilidadeCargo {

    MEMBRO("Membro", "Membro batizado da igreja", 6, "👤"),
    OBREIRO("Obreiro", "Obreiro/Cooperador", 5, "🤝"),
    DIACONO("Diácono", "Diácono ordenado", 4, "🛡️"),
    PRESBITERO("Presbítero", "Presbítero ordenado", 3, "⭐"),
    PASTOR("Pastor", "Pastor ordenado", 2, "👨‍💼"),
    BISPO("Bispo", "Bispo/Pastor Presidente", 1, "👑");

    private final String displayName;
    private final String descricao;
    private final Integer nivel;
    private final String icone;

    ElegibilidadeCargo(String displayName, String descricao, Integer nivel, String icone) {
        this.displayName = displayName;
        this.descricao = descricao;
        this.nivel = nivel;
        this.icone = icone;
    }

    /**
     * Verifica se este nível pode candidatar-se a um cargo de determinada hierarquia.
     *
     * @param hierarquiaDestino hierarquia do cargo de destino
     * @return true se pode candidatar-se, false caso contrário
     */
    public boolean podeCandidatarA(HierarquiaCargo hierarquiaDestino) {
        if (hierarquiaDestino == null) {
            return false;
        }

        return switch (this) {
            case MEMBRO -> hierarquiaDestino == HierarquiaCargo.AUXILIAR ||
                    hierarquiaDestino == HierarquiaCargo.ADMINISTRATIVO;
            case OBREIRO -> hierarquiaDestino == HierarquiaCargo.DIACONAL ||
                    hierarquiaDestino == HierarquiaCargo.AUXILIAR ||
                    hierarquiaDestino == HierarquiaCargo.LIDERANCA ||
                    hierarquiaDestino == HierarquiaCargo.ADMINISTRATIVO;
            case DIACONO -> hierarquiaDestino != HierarquiaCargo.PASTORAL; // Pode tudo exceto pastoral
            case PRESBITERO -> true; // Pode candidatar-se a qualquer cargo
            case PASTOR -> true; // Pode candidatar-se a qualquer cargo
            case BISPO -> true; // Pode candidatar-se a qualquer cargo
        };
    }

    /**
     * Verifica se este nível é superior ou igual a outro.
     *
     * @param outro nível para comparar
     * @return true se é superior ou igual, false caso contrário
     */
    public boolean ehSuperiorOuIgualA(ElegibilidadeCargo outro) {
        if (outro == null) {
            return true;
        }
        return this.nivel <= outro.nivel;
    }

    /**
     * Verifica se este nível é inferior a outro.
     *
     * @param outro nível para comparar
     * @return true se é inferior, false caso contrário
     */
    public boolean ehInferiorA(ElegibilidadeCargo outro) {
        if (outro == null) {
            return false;
        }
        return this.nivel > outro.nivel;
    }

    /**
     * Retorna o próximo nível na hierarquia (promoção).
     *
     * @return próximo nível ou null se já for o mais alto
     */
    public ElegibilidadeCargo getProximoNivel() {
        for (ElegibilidadeCargo nivel : values()) {
            if (nivel.nivel == this.nivel - 1) {
                return nivel;
            }
        }
        return null; // Já é o nível mais alto
    }

    /**
     * Retorna o nível anterior na hierarquia.
     *
     * @return nível anterior ou null se já for o mais baixo
     */
    public ElegibilidadeCargo getNivelAnterior() {
        for (ElegibilidadeCargo nivel : values()) {
            if (nivel.nivel == this.nivel + 1) {
                return nivel;
            }
        }
        return null; // Já é o nível mais baixo
    }

    /**
     * Busca nível por nome (case insensitive).
     *
     * @param nome nome do nível
     * @return nível encontrado ou null
     */
    public static ElegibilidadeCargo fromString(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }

        for (ElegibilidadeCargo nivel : values()) {
            if (nivel.name().equalsIgnoreCase(nome.trim()) ||
                    nivel.displayName.equalsIgnoreCase(nome.trim())) {
                return nivel;
            }
        }
        return null;
    }

    /**
     * Retorna todos os níveis que podem candidatar-se a uma hierarquia específica.
     *
     * @param hierarquia hierarquia do cargo
     * @return array de níveis elegíveis
     */
    public static ElegibilidadeCargo[] getNiveisElegiveispara(HierarquiaCargo hierarquia) {
        return java.util.Arrays.stream(values())
                .filter(nivel -> nivel.podeCandidatarA(hierarquia))
                .toArray(ElegibilidadeCargo[]::new);
    }

    /**
     * Verifica se é nível ministerial (Diácono para cima).
     *
     * @return true se é ministerial, false caso contrário
     */
    public boolean ehMinisterial() {
        return this.nivel <= DIACONO.nivel;
    }

    /**
     * Verifica se é nível de liderança (Obreiro para cima).
     *
     * @return true se é liderança, false caso contrário
     */
    public boolean ehLideranca() {
        return this.nivel <= OBREIRO.nivel;
    }

    /**
     * Retorna string formatada para exibição com ícone.
     *
     * @return string com ícone e nome
     */
    public String getDisplayNameComIcone() {
        return String.format("%s %s", icone, displayName);
    }

    /**
     * Retorna cor associada ao nível (para UI).
     *
     * @return código da cor hex
     */
    public String getCor() {
        return switch (this) {
            case BISPO -> "#7C3AED";         // Roxo escuro
            case PASTOR -> "#8B5CF6";        // Roxo
            case PRESBITERO -> "#3B82F6";    // Azul
            case DIACONO -> "#10B981";       // Verde
            case OBREIRO -> "#F59E0B";       // Amarelo
            case MEMBRO -> "#6B7280";        // Cinza
        };
    }

    /**
     * Retorna níveis ordenados por precedência.
     *
     * @return array ordenado por precedência (mais alto primeiro)
     */
    public static ElegibilidadeCargo[] porOrdemDePrecedencia() {
        return java.util.Arrays.stream(values())
                .sorted(java.util.Comparator.comparing(ElegibilidadeCargo::getNivel))
                .toArray(ElegibilidadeCargo[]::new);
    }

    /**
     * Valida se a string representa um nível válido.
     *
     * @param nivel string para validar
     * @return true se válida, false caso contrário
     */
    public static boolean isValido(String nivel) {
        return fromString(nivel) != null;
    }

    /**
     * Retorna lista de strings para usar no JSONB.
     *
     * @param niveis array de níveis
     * @return lista de strings
     */
    public static java.util.List<String> toStringList(ElegibilidadeCargo[] niveis) {
        return java.util.Arrays.stream(niveis)
                .map(Enum::name)
                .toList();
    }

    /**
     * Converte lista de strings em array de níveis.
     *
     * @param strings lista de strings
     * @return array de níveis
     */
    public static ElegibilidadeCargo[] fromStringList(java.util.List<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return new ElegibilidadeCargo[0];
        }

        return strings.stream()
                .map(ElegibilidadeCargo::fromString)
                .filter(java.util.Objects::nonNull)
                .toArray(ElegibilidadeCargo[]::new);
    }

    @Override
    public String toString() {
        return displayName;
    }
}