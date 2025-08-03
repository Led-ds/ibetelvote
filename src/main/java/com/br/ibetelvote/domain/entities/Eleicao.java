package com.br.ibetelvote.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "eleicoes", indexes = {
        @Index(name = "idx_eleicao_nome", columnList = "nome"),
        @Index(name = "idx_eleicao_ativa", columnList = "ativa"),
        @Index(name = "idx_eleicao_data_inicio", columnList = "data_inicio"),
        @Index(name = "idx_eleicao_data_fim", columnList = "data_fim")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Eleicao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Nome da eleição é obrigatório")
    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "Data de início é obrigatória")
    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    @Builder.Default
    @Column(name = "ativa", nullable = false)
    private Boolean ativa = false;

    @Positive(message = "Total de elegíveis deve ser positivo")
    @Column(name = "total_elegiveis")
    private Integer totalElegiveis;

    @Builder.Default
    @Column(name = "total_votantes", nullable = false)
    private Integer totalVotantes = 0;

    @Builder.Default
    @Column(name = "permite_voto_branco", nullable = false)
    private Boolean permiteVotoBranco = true;

    @Builder.Default
    @Column(name = "permite_voto_nulo", nullable = false)
    private Boolean permiteVotoNulo = true;

    @Builder.Default
    @Column(name = "exibe_resultados_parciais", nullable = false)
    private Boolean exibeResultadosParciais = false;

    @Column(name = "instrucoes_votacao", columnDefinition = "TEXT")
    private String instrucoesVotacao;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // === RELACIONAMENTOS ===
    @OneToMany(mappedBy = "eleicao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Cargo> cargos = new ArrayList<>();

    @OneToMany(mappedBy = "eleicao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Candidato> candidatos = new ArrayList<>();

    @OneToMany(mappedBy = "eleicao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Voto> votos = new ArrayList<>();

    // === MÉTODOS DE NEGÓCIO ===

    public void activate() {
        validarParaAtivacao();
        this.ativa = true;
    }

    public void deactivate() {
        this.ativa = false;
    }

    public void encerrar() {
        this.ativa = false;
        this.dataFim = LocalDateTime.now();
    }

    public void incrementarVotantes() {
        this.totalVotantes++;
    }

    public void updateBasicInfo(String nome, String descricao, LocalDateTime dataInicio,
                                LocalDateTime dataFim, String instrucoesVotacao) {
        this.nome = nome;
        this.descricao = descricao;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.instrucoesVotacao = instrucoesVotacao;
    }

    public void updateConfiguracoes(Boolean permiteVotoBranco, Boolean permiteVotoNulo,
                                    Boolean exibeResultadosParciais, Integer totalElegiveis) {
        this.permiteVotoBranco = permiteVotoBranco;
        this.permiteVotoNulo = permiteVotoNulo;
        this.exibeResultadosParciais = exibeResultadosParciais;
        this.totalElegiveis = totalElegiveis;
    }

    // === MÉTODOS DE VALIDAÇÃO ===

    public boolean isAtiva() {
        return this.ativa != null && this.ativa;
    }

    public boolean isVotacaoAberta() {
        LocalDateTime now = LocalDateTime.now();
        return isAtiva() &&
                now.isAfter(dataInicio) &&
                now.isBefore(dataFim);
    }

    public boolean isVotacaoEncerrada() {
        return LocalDateTime.now().isAfter(dataFim);
    }

    public boolean isVotacaoFutura() {
        return LocalDateTime.now().isBefore(dataInicio);
    }

    public boolean temCargos() {
        return cargos != null && !cargos.isEmpty();
    }

    public boolean temCandidatos() {
        return candidatos != null && !candidatos.isEmpty();
    }

    public boolean podeSerAtivada() {
        try {
            validarParaAtivacao();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean membroJaVotou(UUID membroId) {
        return votos.stream()
                .anyMatch(voto -> voto.getMembroId().equals(membroId));
    }

    // === MÉTODOS UTILITÁRIOS ===

    public String getStatusDescricao() {
        if (isVotacaoFutura()) {
            return "Aguardando início";
        } else if (isVotacaoAberta()) {
            return "Votação em andamento";
        } else if (isVotacaoEncerrada()) {
            return "Encerrada";
        } else if (isAtiva()) {
            return "Ativa (aguardando período)";
        } else {
            return "Inativa";
        }
    }

    public double getPercentualParticipacao() {
        if (totalElegiveis == null || totalElegiveis == 0) {
            return 0.0;
        }
        return (totalVotantes.doubleValue() / totalElegiveis.doubleValue()) * 100.0;
    }

    public int getTotalVotosContabilizados() {
        return votos != null ? votos.size() : 0;
    }

    public long getDuracaoEmHoras() {
        if (dataInicio == null || dataFim == null) {
            return 0;
        }
        return java.time.Duration.between(dataInicio, dataFim).toHours();
    }

    // === VALIDAÇÕES PRIVADAS ===

    private void validarParaAtivacao() {
        if (!temCargos()) {
            throw new IllegalStateException("Não é possível ativar eleição sem cargos");
        }
        if (!temCandidatos()) {
            throw new IllegalStateException("Não é possível ativar eleição sem candidatos");
        }
        if (dataInicio == null || dataFim == null) {
            throw new IllegalStateException("Datas de início e fim são obrigatórias");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalStateException("Data de início deve ser anterior à data de fim");
        }
        if (totalElegiveis == null || totalElegiveis <= 0) {
            throw new IllegalStateException("Total de elegíveis deve ser informado e maior que zero");
        }
    }
}