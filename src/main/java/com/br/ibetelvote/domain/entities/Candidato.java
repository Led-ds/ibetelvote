package com.br.ibetelvote.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "candidatos", indexes = {
        @Index(name = "idx_candidato_eleicao_id", columnList = "eleicao_id"),
        @Index(name = "idx_candidato_cargo_pretendido_id", columnList = "cargo_pretendido_id"),
        @Index(name = "idx_candidato_membro_id", columnList = "membro_id"),
        @Index(name = "idx_candidato_numero", columnList = "numero_candidato"),
        @Index(name = "idx_candidato_ativo", columnList = "ativo"),
        @Index(name = "idx_candidato_aprovado", columnList = "aprovado")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_candidato_numero_eleicao", columnNames = {"numero_candidato", "eleicao_id"}),
        @UniqueConstraint(name = "uk_candidato_membro_cargo_eleicao", columnNames = {"membro_id", "cargo_pretendido_id", "eleicao_id"})
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

    @NotNull(message = "Cargo pretendido é obrigatório")
    @Column(name = "cargo_pretendido_id", nullable = false)
    private UUID cargoPretendidoId;

    @Column(name = "numero_candidato", length = 10)
    private String numeroCandidato;

    @NotBlank(message = "Nome do candidato é obrigatório")
    @Column(name = "nome_candidato", nullable = false, length = 100)
    private String nomeCandidato;

    @Column(name = "descricao_candidatura", columnDefinition = "TEXT")
    private String descricaoCandidatura;

    @Column(name = "propostas", columnDefinition = "TEXT")
    private String propostas;

    @Column(name = "experiencia", columnDefinition = "TEXT")
    private String experiencia;

    @Lob
    @Column(name = "foto_campanha_data")
    private byte[] fotoCampanhaData;

    @Column(name = "foto_campanha_tipo", length = 50)
    private String fotoCampanhaTipo;

    @Column(name = "foto_campanha_nome", length = 255)
    private String fotoCampanhaNome;

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
    @JoinColumn(name = "cargo_pretendido_id", insertable = false, updatable = false)
    private Cargo cargoPretendido;

    @OneToMany(mappedBy = "candidato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Voto> votos = new ArrayList<>();

    /**
     * Atualiza dados básicos da candidatura
     */
    public void updateCandidatura(String nomeCandidato, String descricaoCandidatura,
                                  String propostas, String experiencia) {
        this.nomeCandidato = nomeCandidato;
        this.descricaoCandidatura = descricaoCandidatura;
        this.propostas = propostas;
        this.experiencia = experiencia;
    }

    /**
     * Atualiza foto da campanha
     */
    public void updateFotoCampanha(byte[] fotoCampanhaData, String fotoCampanhaTipo, String fotoCampanhaNome) {
        this.fotoCampanhaData = fotoCampanhaData;
        this.fotoCampanhaTipo = fotoCampanhaTipo;
        this.fotoCampanhaNome = fotoCampanhaNome;
    }

    /**
     * Remove foto da campanha
     */
    public void removeFotoCampanha() {
        this.fotoCampanhaData = null;
        this.fotoCampanhaTipo = null;
        this.fotoCampanhaNome = null;
    }

    /**
     * Ativa a candidatura
     */
    public void activate() {
        this.ativo = true;
    }

    /**
     * Desativa a candidatura
     */
    public void deactivate() {
        this.ativo = false;
    }

    /**
     * Aprova a candidatura
     */
    public void aprovar() {
        this.aprovado = true;
        this.dataAprovacao = LocalDateTime.now();
        this.motivoReprovacao = null;
    }

    /**
     * Reprova a candidatura
     */
    public void reprovar(String motivo) {
        this.aprovado = false;
        this.motivoReprovacao = motivo;
        this.dataAprovacao = null;
    }

    /**
     * Define número do candidato
     */
    public void definirNumero(String numeroCandidato) {
        this.numeroCandidato = numeroCandidato;
    }

    /**
     * Atualiza cargo pretendido
     */
    public void updateCargoPretendido(UUID cargoPretendidoId) {
        this.cargoPretendidoId = cargoPretendidoId;
    }

    /**
     * Verifica se a candidatura está ativa
     */
    public boolean isAtivo() {
        return ativo != null && ativo;
    }

    /**
     * Verifica se a candidatura está aprovada
     */
    public boolean isAprovado() {
        return aprovado != null && aprovado;
    }

    /**
     * Verifica se a candidatura está reprovada
     */
    public boolean isReprovado() {
        return !isAprovado();
    }

    /**
     * Verifica se pode receber votos
     */
    public boolean podeReceberVotos() {
        return isAtivo() && isAprovado() &&
                eleicao != null && eleicao.isVotacaoAberta();
    }

    /**
     * Verifica se tem foto de campanha
     */
    public boolean temFotoCampanha() {
        return fotoCampanhaData != null && fotoCampanhaData.length > 0;
    }

    /**
     * Verifica se tem número definido
     */
    public boolean temNumero() {
        return numeroCandidato != null && !numeroCandidato.trim().isEmpty();
    }

    /**
     * Verifica se a candidatura está completa
     */
    public boolean isCandidaturaCompleta() {
        return nomeCandidato != null && !nomeCandidato.trim().isEmpty() &&
                descricaoCandidatura != null && !descricaoCandidatura.trim().isEmpty() &&
                propostas != null && !propostas.trim().isEmpty();
    }

    /**
     * Verifica se o membro pode se candidatar ao cargo pretendido
     */
    public boolean membroPodeSeCandidarParaCargo() {
        if (membro == null || cargoPretendido == null) {
            return false;
        }
        return membro.podeSeCandidarPara(cargoPretendido);
    }

    /**
     * Verifica se a candidatura é elegível (todas as validações)
     */
    public boolean isElegivel() {
        return isAtivo() &&
                membro != null && membro.isActive() &&
                cargoPretendido != null && cargoPretendido.isAtivo() &&
                membroPodeSeCandidarParaCargo() &&
                isCandidaturaCompleta();
    }

    // === MÉTODOS UTILITÁRIOS ===

    /**
     * Retorna total de votos recebidos
     */
    public int getTotalVotos() {
        return votos != null ? votos.size() : 0;
    }

    /**
     * Retorna nome do cargo pretendido
     */
    public String getNomeCargoPretendido() {
        return cargoPretendido != null ? cargoPretendido.getNome() : "Cargo não definido";
    }

    /**
     * Retorna nome do membro
     */
    public String getNomeMembro() {
        return membro != null ? membro.getNome() : nomeCandidato;
    }

    /**
     * Retorna status da candidatura
     */
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

    /**
     * Retorna nome para exibição
     */
    public String getDisplayName() {
        return nomeCandidato;
    }

    /**
     * Retorna número formatado
     */
    public String getNumeroFormatado() {
        return numeroCandidato != null ? numeroCandidato : "S/N";
    }

    /**
     * Calcula percentual de votos em relação ao total de votos válidos do cargo
     */
    public double getPercentualVotos() {
        if (eleicao == null) {
            return 0.0;
        }

        // Contar total de votos válidos para o mesmo cargo na eleição
        long totalVotosValidosCargo = eleicao.getVotos().stream()
                .filter(voto -> voto.getCandidato() != null)
                .filter(voto -> {
                    if (voto.getCandidato().getCargoPretendidoId() != null) {
                        return voto.getCandidato().getCargoPretendidoId().equals(this.cargoPretendidoId);
                    }
                    return false;
                })
                .count();

        if (totalVotosValidosCargo == 0) {
            return 0.0;
        }

        return (getTotalVotos() * 100.0) / totalVotosValidosCargo;
    }

    /**
     * Retorna resumo da votação
     */
    public String getResumoVotacao() {
        int votos = getTotalVotos();
        double percentual = getPercentualVotos();
        return String.format("%d votos (%.1f%%)", votos, percentual);
    }

    /**
     * Retorna foto de campanha como Base64
     */
    @Transient
    public String getFotoCampanhaBase64() {
        if (temFotoCampanha()) {
            return Base64.getEncoder().encodeToString(fotoCampanhaData);
        }
        return null;
    }

    /**
     * Retorna foto como Data URI
     */
    public String getFotoCampanhaDataUri() {
        if (temFotoCampanha() && fotoCampanhaTipo != null) {
            String base64 = getFotoCampanhaBase64();
            return "data:" + fotoCampanhaTipo + ";base64," + base64;
        }
        return null;
    }

    /**
     * Retorna tamanho da foto em bytes
     */
    public long getFotoCampanhaSize() {
        return temFotoCampanha() ? fotoCampanhaData.length : 0;
    }

    // === MÉTODOS DE INFORMAÇÃO ===

    /**
     * Retorna cargo atual do membro
     */
    public String getCargoAtualMembro() {
        return membro != null ? membro.getNomeCargoAtual() : null;
    }

    /**
     * Retorna email do membro
     */
    public String getEmailMembro() {
        return membro != null ? membro.getEmail() : null;
    }

    /**
     * Verifica se membro está ativo
     */
    public boolean isMembroAtivo() {
        return membro != null && membro.isActive();
    }

    /**
     * Retorna nome da eleição
     */
    public String getNomeEleicao() {
        return eleicao != null ? eleicao.getNome() : "Eleição não definida";
    }

    /**
     * Compara candidatos por total de votos (para ordenação)
     */
    public int compareVotos(Candidato outro) {
        return Integer.compare(outro.getTotalVotos(), this.getTotalVotos());
    }

    /**
     * Verifica se tem mais votos que outro candidato
     */
    public boolean temMaisVotosQue(Candidato outro) {
        return this.getTotalVotos() > outro.getTotalVotos();
    }

    /**
     * Verifica se é candidato do mesmo cargo
     */
    public boolean isMesmoCargo(Candidato outro) {
        return this.cargoPretendidoId != null &&
                this.cargoPretendidoId.equals(outro.getCargoPretendidoId());
    }

    /**
     * Valida se todos os dados obrigatórios estão preenchidos
     */
    public void validarDadosObrigatorios() {
        if (membroId == null) {
            throw new IllegalStateException("Membro é obrigatório");
        }
        if (eleicaoId == null) {
            throw new IllegalStateException("Eleição é obrigatória");
        }
        if (cargoPretendidoId == null) {
            throw new IllegalStateException("Cargo pretendido é obrigatório");
        }
        if (nomeCandidato == null || nomeCandidato.trim().isEmpty()) {
            throw new IllegalStateException("Nome do candidato é obrigatório");
        }
    }

    /**
     * Valida se a candidatura pode ser aprovada
     */
    public void validarParaAprovacao() {
        validarDadosObrigatorios();

        if (!isCandidaturaCompleta()) {
            throw new IllegalStateException("Candidatura incompleta - faltam dados obrigatórios");
        }

        if (!membroPodeSeCandidarParaCargo()) {
            throw new IllegalStateException("Membro não atende aos requisitos hierárquicos para o cargo pretendido");
        }

        if (!isMembroAtivo()) {
            throw new IllegalStateException("Membro deve estar ativo para ser candidato");
        }
    }

    @Override
    public String toString() {
        return String.format("Candidato{id=%s, nome='%s', cargo='%s', ativo=%s, aprovado=%s}",
                id, nomeCandidato, getNomeCargoPretendido(), ativo, aprovado);
    }
}