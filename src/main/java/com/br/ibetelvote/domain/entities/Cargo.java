package com.br.ibetelvote.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cargos", indexes = {
        @Index(name = "idx_cargo_nome", columnList = "nome"),
        @Index(name = "idx_cargo_ativo", columnList = "ativo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Nome do cargo é obrigatório")
    @Column(name = "nome", nullable = false, unique = true, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Atualiza as informações básicas do cargo
     */
    public void updateBasicInfo(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    /**
     * Ativa o cargo
     */
    public void activate() {
        this.ativo = true;
    }

    /**
     * Desativa o cargo
     */
    public void deactivate() {
        this.ativo = false;
    }

    /**
     * Verifica se o cargo está ativo
     */
    public boolean isAtivo() {
        return ativo != null && ativo;
    }

    /**
     * Verifica se o cargo pode ser usado em eleições
     */
    public boolean podeSerUsadoEmEleicoes() {
        return isAtivo() && nome != null && !nome.trim().isEmpty();
    }

    /**
     * Retorna o nome para exibição
     */
    public String getDisplayName() {
        return nome;
    }

    /**
     * Retorna uma descrição resumida do cargo
     */
    public String getResumo() {
        if (descricao != null && !descricao.trim().isEmpty()) {
            return descricao.length() > 100 ?
                    descricao.substring(0, 97) + "..." :
                    descricao;
        }
        return "Sem descrição";
    }

    /**
     * Retorna o status do cargo
     */
    public String getStatus() {
        return isAtivo() ? "Ativo" : "Inativo";
    }

    /**
     * Valida se o nome do cargo é válido
     */
    public static boolean isNomeValido(String nome) {
        return nome != null &&
                !nome.trim().isEmpty() &&
                nome.trim().length() >= 3 &&
                nome.trim().length() <= 100;
    }

    /**
     * Normaliza o nome do cargo (primeira letra maiúscula)
     */
    public static String normalizarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return nome;
        }

        String nomeNormalizado = nome.trim();
        return nomeNormalizado.substring(0, 1).toUpperCase() +
                nomeNormalizado.substring(1).toLowerCase();
    }

    /**
     * Verifica se o cargo tem informações completas
     */
    public boolean temInformacoesCompletas() {
        return isNomeValido(this.nome) &&
                this.descricao != null &&
                !this.descricao.trim().isEmpty();
    }

    @Override
    public String toString() {
        return String.format("Cargo{id=%s, nome='%s', ativo=%s}",
                id, nome, ativo);
    }
}