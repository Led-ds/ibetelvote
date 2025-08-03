package com.br.ibetelvote.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "candidatos", indexes = {
        @Index(name = "idx_candidato_eleicao_id", columnList = "eleicao_id"),
        @Index(name = "idx_candidato_cargo_id", columnList = "cargo_id"),
        @Index(name = "idx_candidato_membro_id", columnList = "membro_id"),
        @Index(name = "idx_candidato_numero", columnList = "numero_candidato"),
        @Index(name = "idx_candidato_ativo", columnList = "ativo")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_candidato_numero_eleicao", columnNames = {"numero_candidato", "eleicao_id"}),
        @UniqueConstraint(name = "uk_candidato_membro_cargo", columnNames = {"membro_id", "cargo_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Candidato {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Membro é obrigatório")
    @Column(name = "membro_id", nullable = false)
    private UUID membroId;

    @NotNull(message = "Eleição é obrigatória")
    @Column(name = "eleicao_id", nullable = false)
    private UUID eleicaoId;

    @NotNull(message = "Cargo é obrigatório")
    @Column(name = "cargo_id", nullable = false)
    private UUID cargoId;

    @Column(name = "numero_candidato", length = 10)
    private String numeroCandidato;

    @NotBlank(message = "Nome do candidato é obrigatório")
    @Column(name = "nome_candidato", nullable = false, length = 100)
    private String nomeCandidato;

    @Column(name = "nome_cargo_pretendido", length = 100)
    private String nomeCargoRetendido;

    @Column(name = "descricao_candidatura", columnDefinition = "TEXT")
    private String descricaoCandidatura;

    @Column(name = "propostas", columnDefinition = "TEXT")
    private String propostas;

    @Column(name = "experiencia", columnDefinition = "TEXT")
    private String experiencia;

    @Column(name = "foto_campanha", length = 500)
    private String fotoCampanha;

    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Builder.Default
    @Column(name = "aprovado", nullable = false)
    private Boolean aprovado = false;

    @Column(name = "motivo_reprovacao", columnDefinition = "TEXT")
    private String motivoReprovacao;

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // === RELACIONAMENTOS ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id", insertable = false, updatable = false)
    private Membro membro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eleicao_id", insertable = false, updatable = false)
    private Eleicao eleicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id", insertable = false, updatable = false)
    private Cargo cargo;

    @OneToMany(mappedBy = "candidato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Voto> votos = new ArrayList<>();

    // === MÉTODOS DE NEGÓCIO ===

    public void updateCandidatura(String nomeCandidato, String nomeCargoRetendido,
                                  String descricaoCandidatura, String propostas, String experiencia) {
        this.nomeCandidato = nomeCandidato;
        this.nomeCargoRetendido = nomeCargoRetendido;
        this.descricaoCandidatura = descricaoCandidatura;
        this.propostas = propostas;
        this.experiencia = experiencia;
    }

    public void updateFotoCampanha(String fotoCampanha) {
        this.fotoCampanha = fotoCampanha;
    }

    public void removeFotoCampanha() {
        this.fotoCampanha = null;
    }

    public void activate() {
        this.ativo = true;
    }

    public void deactivate() {
        this.ativo = false;
    }

    public void aprovar() {
        this.aprovado = true;
        this.dataAprovacao = LocalDateTime.now();
        this.motivoReprovacao = null;
    }

    public void reprovar(String motivo) {
        this.aprovado = false;
        this.motivoReprovacao = motivo;
        this.dataAprovacao = null;
    }

    public void definirNumero(String numeroCandidato) {
        this.numeroCandidato = numeroCandidato;
    }

    // === MÉTODOS DE VALIDAÇÃO ===

    public boolean isAtivo() {
        return ativo != null && ativo;
    }

    public boolean isAprovado() {
        return aprovado != null && aprovado;
    }

    public boolean isReprovado() {
        return !isAprovado();
    }

    public boolean podeReceberVotos() {
        return isAtivo() && isAprovado() &&
                eleicao != null && eleicao.isVotacaoAberta();
    }

    public boolean temFotoCampanha() {
        return fotoCampanha != null && !fotoCampanha.trim().isEmpty();
    }

    public boolean temNumero() {
        return numeroCandidato != null && !numeroCandidato.trim().isEmpty();
    }

    public boolean isCandidaturaCompleta() {
        return nomeCandidato != null && !nomeCandidato.trim().isEmpty() &&
                descricaoCandidatura != null && !descricaoCandidatura.trim().isEmpty() &&
                propostas != null && !propostas.trim().isEmpty();
    }

    // === MÉTODOS UTILITÁRIOS ===

    public int getTotalVotos() {
        return votos != null ? votos.size() : 0;
    }

    public String getStatusCandidatura() {
        if (!isAtivo()) {
            return "Inativo";
        }
        if (isReprovado()) {
            return "Reprovado";
        }
        if (!isAprovado()) {
            return "Aguardando aprovação";
        }
        if (!podeReceberVotos()) {
            return "Aprovado (votação fechada)";
        }
        return "Disponível para votação";
    }

    public String getDisplayName() {
        return nomeCandidato;
    }

    public String getNumeroFormatado() {
        return numeroCandidato != null ? numeroCandidato : "S/N";
    }

    public String getFotoCampanhaUrl() {
        if (temFotoCampanha()) {
            return fotoCampanha;
        }
        // Usar foto do membro se não tiver foto de campanha
        return membro != null && membro.hasPhoto() ?
                membro.getPhotoUrl() : getDefaultPhotoUrl();
    }

    public double getPercentualVotos() {
        if (cargo == null || cargo.getTotalVotosValidos() == 0) {
            return 0.0;
        }
        return (getTotalVotos() * 100.0) / cargo.getTotalVotosValidos();
    }

    public String getResumoVotacao() {
        int votos = getTotalVotos();
        double percentual = getPercentualVotos();
        return String.format("%d votos (%.1f%%)", votos, percentual);
    }

    // === MÉTODOS DE INFORMAÇÃO ===

    public String getNomeCompletoMembro() {
        return membro != null ? membro.getNome() : nomeCandidato;
    }

    public String getCargoMembro() {
        return membro != null ? membro.getCargo() : null;
    }

    public String getEmailMembro() {
        return membro != null ? membro.getEmail() : null;
    }

    public boolean isMembroAtivo() {
        return membro != null && membro.isActive();
    }

    private String getDefaultPhotoUrl() {
        return "/api/v1/files/default-candidate.png";
    }

    // === MÉTODOS DE COMPARAÇÃO ===

    public int compareVotos(Candidato outro) {
        return Integer.compare(outro.getTotalVotos(), this.getTotalVotos());
    }

    public boolean temMaisVotosQue(Candidato outro) {
        return this.getTotalVotos() > outro.getTotalVotos();
    }
}