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
@Table(name = "cargos", indexes = {
        @Index(name = "idx_cargo_eleicao_id", columnList = "eleicao_id"),
        @Index(name = "idx_cargo_nome", columnList = "nome"),
        @Index(name = "idx_cargo_ordem", columnList = "ordem_votacao")
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
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "Máximo de votos é obrigatório")
    @Positive(message = "Máximo de votos deve ser positivo")
    @Column(name = "max_votos", nullable = false)
    private Integer maxVotos;

    @Builder.Default
    @Column(name = "ordem_votacao")
    private Integer ordemVotacao = 1;

    @Builder.Default
    @Column(name = "permite_voto_branco", nullable = false)
    private Boolean permiteVotoBranco = true;

    @Builder.Default
    @Column(name = "obrigatorio", nullable = false)
    private Boolean obrigatorio = true;

    @Column(name = "instrucoes_especificas", columnDefinition = "TEXT")
    private String instrucoesEspecificas;

    @NotNull(message = "Eleição é obrigatória")
    @Column(name = "eleicao_id", nullable = false)
    private UUID eleicaoId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // === RELACIONAMENTOS ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eleicao_id", insertable = false, updatable = false)
    private Eleicao eleicao;

    @OneToMany(mappedBy = "cargo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Candidato> candidatos = new ArrayList<>();

    @OneToMany(mappedBy = "cargo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Voto> votos = new ArrayList<>();

    // === MÉTODOS DE NEGÓCIO ===

    public void updateBasicInfo(String nome, String descricao, Integer maxVotos,
                                Integer ordemVotacao, String instrucoesEspecificas) {
        this.nome = nome;
        this.descricao = descricao;
        this.maxVotos = maxVotos;
        this.ordemVotacao = ordemVotacao;
        this.instrucoesEspecificas = instrucoesEspecificas;
    }

    public void updateConfiguracoes(Boolean permiteVotoBranco, Boolean obrigatorio) {
        this.permiteVotoBranco = permiteVotoBranco;
        this.obrigatorio = obrigatorio;
    }

    public void associateEleicao(UUID eleicaoId) {
        this.eleicaoId = eleicaoId;
    }

    // === MÉTODOS DE VALIDAÇÃO ===

    public boolean temCandidatos() {
        return candidatos != null && !candidatos.isEmpty();
    }

    public boolean podeReceberVotos() {
        return temCandidatos() && eleicao != null && eleicao.isVotacaoAberta();
    }

    public boolean isObrigatorio() {
        return obrigatorio != null && obrigatorio;
    }

    public boolean permiteVotoBranco() {
        return permiteVotoBranco != null && permiteVotoBranco;
    }

    public boolean membroJaVotouNesteCargo(UUID membroId) {
        return votos.stream()
                .anyMatch(voto -> voto.getMembroId().equals(membroId));
    }

    public boolean membroAtingiuLimiteVotos(UUID membroId) {
        long votosDoMembro = votos.stream()
                .filter(voto -> voto.getMembroId().equals(membroId))
                .count();
        return votosDoMembro >= maxVotos;
    }

    // === MÉTODOS UTILITÁRIOS ===

    public int getTotalVotos() {
        return votos != null ? votos.size() : 0;
    }

    public int getTotalCandidatos() {
        return candidatos != null ? candidatos.size() : 0;
    }

    public List<Candidato> getCandidatosAtivos() {
        if (candidatos == null) {
            return new ArrayList<>();
        }
        return candidatos.stream()
                .filter(candidato -> candidato.isAtivo())
                .toList();
    }

    public long getTotalVotosValidos() {
        return votos.stream()
                .filter(voto -> voto.getCandidatoId() != null)
                .count();
    }

    public long getTotalVotosBranco() {
        return votos.stream()
                .filter(voto -> voto.isVotoBranco())
                .count();
    }

    public long getTotalVotosNulo() {
        return votos.stream()
                .filter(voto -> voto.isVotoNulo())
                .count();
    }

    public String getStatusVotacao() {
        if (!podeReceberVotos()) {
            return "Indisponível para votação";
        }

        int totalVotos = getTotalVotos();
        if (totalVotos == 0) {
            return "Aguardando votos";
        }

        return String.format("%d votos recebidos", totalVotos);
    }

    // === MÉTODOS DE RELATÓRIO ===

    public double getPercentualParticipacao() {
        if (eleicao == null || eleicao.getTotalVotantes() == 0) {
            return 0.0;
        }
        return (getTotalVotos() * 100.0) / eleicao.getTotalVotantes();
    }

    public String getResumoVotacao() {
        int total = getTotalVotos();
        long validos = getTotalVotosValidos();
        long brancos = getTotalVotosBranco();
        long nulos = getTotalVotosNulo();

        return String.format("Total: %d | Válidos: %d | Brancos: %d | Nulos: %d",
                total, validos, brancos, nulos);
    }
}