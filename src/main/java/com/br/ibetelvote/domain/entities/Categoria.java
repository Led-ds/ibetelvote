package com.br.ibetelvote.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade representando as categorias organizacionais dos cargos ministeriais.
 *
 * As categorias servem para agrupar cargos por área de atuação, facilitando
 * a organização administrativa e a busca de informações.
 */
@Entity
@Table(name = "categorias", indexes = {
        @Index(name = "idx_categoria_nome", columnList = "nome"),
        @Index(name = "idx_categoria_ativo", columnList = "ativo"),
        @Index(name = "idx_categoria_ordem", columnList = "ordem_exibicao"),
        @Index(name = "idx_categoria_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "cargos") // Evita recursão infinita
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Nome da categoria é obrigatório")
    @Column(name = "nome", nullable = false, unique = true, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @PositiveOrZero(message = "Ordem de exibição deve ser positiva")
    @Builder.Default
    @Column(name = "ordem_exibicao", nullable = false)
    private Integer ordemExibicao = 0;

    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // === RELACIONAMENTOS ===

    /**
     * Lista de cargos que pertencem a esta categoria
     */
    @OneToMany(mappedBy = "categoria", cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
    @OrderBy("ordem_precedencia ASC, nome ASC")
    @Builder.Default
    private List<Cargo> cargos = new ArrayList<>();

    // === MÉTODOS DE NEGÓCIO ===

    /**
     * Ativa a categoria
     */
    public void ativar() {
        this.ativo = true;
    }

    /**
     * Desativa a categoria
     */
    public void desativar() {
        this.ativo = false;
    }

    /**
     * Verifica se a categoria está ativa
     */
    public boolean isAtiva() {
        return ativo != null && ativo;
    }

    /**
     * Adiciona um cargo à categoria
     */
    public void adicionarCargo(Cargo cargo) {
        if (cargo != null && !cargos.contains(cargo)) {
            cargos.add(cargo);
            cargo.setCategoria(this);
        }
    }

    /**
     * Remove um cargo da categoria
     */
    public void removerCargo(Cargo cargo) {
        if (cargo != null && cargos.contains(cargo)) {
            cargos.remove(cargo);
            cargo.setCategoria(null);
        }
    }

    /**
     * Retorna a quantidade de cargos na categoria
     */
    public int getTotalCargos() {
        return cargos != null ? cargos.size() : 0;
    }

    /**
     * Retorna a quantidade de cargos ativos na categoria
     */
    public long getTotalCargosAtivos() {
        if (cargos == null) {
            return 0;
        }
        return cargos.stream()
                .filter(Cargo::isAtivo)
                .count();
    }

    /**
     * Retorna a quantidade de cargos disponíveis para eleições
     */
    public long getTotalCargosDisponiveis() {
        if (cargos == null) {
            return 0;
        }
        return cargos.stream()
                .filter(cargo -> cargo.isAtivo() && cargo.isDisponivelEleicao())
                .count();
    }

    /**
     * Retorna lista de cargos ativos ordenados por precedência
     */
    public List<Cargo> getCargosAtivos() {
        if (cargos == null) {
            return new ArrayList<>();
        }
        return cargos.stream()
                .filter(Cargo::isAtivo)
                .sorted((c1, c2) -> {
                    // Primeiro por ordem de precedência, depois por nome
                    if (c1.getOrdemPrecedencia() != null && c2.getOrdemPrecedencia() != null) {
                        int ordem = c1.getOrdemPrecedencia().compareTo(c2.getOrdemPrecedencia());
                        if (ordem != 0) return ordem;
                    }
                    return c1.getNome().compareTo(c2.getNome());
                })
                .toList();
    }

    /**
     * Retorna lista de cargos disponíveis para eleições
     */
    public List<Cargo> getCargosDisponiveis() {
        if (cargos == null) {
            return new ArrayList<>();
        }
        return cargos.stream()
                .filter(cargo -> cargo.isAtivo() && cargo.isDisponivelEleicao())
                .sorted((c1, c2) -> {
                    if (c1.getOrdemPrecedencia() != null && c2.getOrdemPrecedencia() != null) {
                        int ordem = c1.getOrdemPrecedencia().compareTo(c2.getOrdemPrecedencia());
                        if (ordem != 0) return ordem;
                    }
                    return c1.getNome().compareTo(c2.getNome());
                })
                .toList();
    }

    /**
     * Verifica se a categoria pode ser removida
     */
    public boolean podeSerRemovida() {
        return getTotalCargos() == 0;
    }

    /**
     * Retorna nome para exibição
     */
    public String getDisplayName() {
        return nome;
    }

    /**
     * Retorna resumo da categoria
     */
    public String getResumo() {
        if (descricao != null && !descricao.trim().isEmpty()) {
            return descricao.length() > 150 ?
                    descricao.substring(0, 147) + "..." :
                    descricao;
        }
        return "Sem descrição disponível";
    }

    /**
     * Retorna status da categoria
     */
    public String getStatus() {
        return isAtiva() ? "Ativa" : "Inativa";
    }

    /**
     * Verifica se tem informações completas
     */
    public boolean temInformacoesCompletas() {
        return nome != null && !nome.trim().isEmpty() &&
                descricao != null && !descricao.trim().isEmpty();
    }

    /**
     * Retorna estatísticas resumidas da categoria
     */
    public String getEstatisticas() {
        return String.format("Total: %d | Ativos: %d | Disponíveis: %d",
                getTotalCargos(), getTotalCargosAtivos(), getTotalCargosDisponiveis());
    }

    // === MÉTODOS UTILITÁRIOS ESTÁTICOS ===

    /**
     * Valida se o nome da categoria é válido
     */
    public static boolean isNomeValido(String nome) {
        return nome != null &&
                !nome.trim().isEmpty() &&
                nome.trim().length() >= 3 &&
                nome.trim().length() <= 100;
    }

    /**
     * Normaliza o nome da categoria
     */
    public static String normalizarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return nome;
        }

        String nomeNormalizado = nome.trim();
        // Primeira letra maiúscula de cada palavra
        StringBuilder resultado = new StringBuilder();
        String[] palavras = nomeNormalizado.toLowerCase().split("\\s+");

        for (int i = 0; i < palavras.length; i++) {
            if (i > 0) {
                resultado.append(" ");
            }
            if (palavras[i].length() > 0) {
                resultado.append(Character.toUpperCase(palavras[i].charAt(0)));
                if (palavras[i].length() > 1) {
                    resultado.append(palavras[i].substring(1));
                }
            }
        }

        return resultado.toString();
    }

    /**
     * Valida se a ordem de exibição é válida
     */
    public static boolean isOrdemValida(Integer ordem) {
        return ordem != null && ordem >= 0;
    }

    // === BUILDER CUSTOMIZADO ===

    /**
     * Construtor de builder personalizado para categoria ativa
     */
    public static CategoriaBuilder ativa(String nome) {
        return Categoria.builder()
                .nome(normalizarNome(nome))
                .ativo(true);
    }

    /**
     * Construtor de builder personalizado para categoria com ordem
     */
    public static CategoriaBuilder comOrdem(String nome, Integer ordem) {
        return Categoria.builder()
                .nome(normalizarNome(nome))
                .ordemExibicao(ordem)
                .ativo(true);
    }
}