package com.br.ibetelvote.domain.entities;

import com.br.ibetelvote.domain.entities.enums.TipoVoto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "votos",
        indexes = {
                @Index(name = "idx_voto_eleicao_id", columnList = "eleicao_id"),
                @Index(name = "idx_voto_candidato_id", columnList = "candidato_id"),
                @Index(name = "idx_voto_membro_id", columnList = "membro_id"),
                @Index(name = "idx_voto_cargo_pretendido_id", columnList = "cargo_pretendido_id"),
                @Index(name = "idx_voto_membro_eleicao", columnList = "membro_id, eleicao_id"),
                @Index(name = "idx_voto_cargo_eleicao", columnList = "cargo_pretendido_id, eleicao_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_voto_membro_candidato",
                        columnNames = {"membro_id", "candidato_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Membro é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membro_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_voto_membro"))
    private Membro membro;

    @NotNull(message = "Eleição é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eleicao_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_voto_eleicao"))
    private Eleicao eleicao;

    @NotNull(message = "Cargo pretendido é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cargo_pretendido_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_voto_cargo_pretendido"))
    private Cargo cargoPretendido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_id",
            foreignKey = @ForeignKey(name = "fk_voto_candidato"))
    private Candidato candidato;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_voto", nullable = false, length = 20)
    @Builder.Default
    private TipoVoto tipoVoto = TipoVoto.CANDIDATO;

    @Column(name = "hash_voto", length = 64)
    private String hashVoto;

    @Column(name = "ip_origem", length = 45)
    private String ipOrigem;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "data_voto", nullable = false, updatable = false)
    private LocalDateTime dataVoto;

    // Factory Methods
    public static Voto criarVotoValido(Membro membro, Eleicao eleicao, Candidato candidato) {
        if (!eleicao.isVotacaoAberta()) {
            throw new IllegalStateException("Votação não está aberta");
        }

        if (!candidato.isAprovado() || !candidato.isAtivo()) {
            throw new IllegalStateException("Candidato não está disponível para votação");
        }

        return Voto.builder()
                .membro(membro)
                .eleicao(eleicao)
                .candidato(candidato)
                .cargoPretendido(candidato.getCargoPretendido())
                .tipoVoto(TipoVoto.CANDIDATO)
                .build();
    }

    public static Voto criarVotoBranco(Membro membro, Eleicao eleicao, Cargo cargoPretendido) {
        return Voto.builder()
                .membro(membro)
                .eleicao(eleicao)
                .cargoPretendido(cargoPretendido)
                .candidato(null)
                .tipoVoto(TipoVoto.BRANCO)
                .build();
    }

    public static Voto criarVotoNulo(Membro membro, Eleicao eleicao, Cargo cargoPretendido) {
        return Voto.builder()
                .membro(membro)
                .eleicao(eleicao)
                .cargoPretendido(cargoPretendido)
                .candidato(null)
                .tipoVoto(TipoVoto.NULO)
                .build();
    }

    // Business Methods
    public void definirHashSeguranca(String hash) {
        this.hashVoto = hash;
    }

    public void definirDadosOrigem(String ipOrigem, String userAgent) {
        this.ipOrigem = ipOrigem;
        this.userAgent = userAgent;
    }

    public boolean isVotoValido() {
        return TipoVoto.CANDIDATO.equals(tipoVoto) && candidato != null;
    }

    public boolean isVotoBranco() {
        return TipoVoto.BRANCO.equals(tipoVoto);
    }

    public boolean isVotoNulo() {
        return TipoVoto.NULO.equals(tipoVoto);
    }

    public boolean temCandidato() {
        return candidato != null;
    }

    public boolean temHash() {
        return hashVoto != null && !hashVoto.trim().isEmpty();
    }

    public boolean isVotoSeguro() {
        return temHash() && ipOrigem != null;
    }

    public String getTipoVotoDescricao() {
        return tipoVoto.getDescricao();
    }

    public String getNomeCandidato() {
        if (candidato != null) {
            return candidato.getNomeCandidato();
        }
        return getTipoVotoDescricao();
    }

    public String getNumeroCandidato() {
        if (candidato != null && candidato.temNumero()) {
            return candidato.getNumeroCandidato();
        }
        return tipoVoto.name();
    }

    public String getNomeMembro() {
        return membro != null ? membro.getNome() : "Desconhecido";
    }

    public String getNomeCargoPretendido() {
        return cargoPretendido != null ? cargoPretendido.getNome() : "Desconhecido";
    }

    public String getNomeEleicao() {
        return eleicao != null ? eleicao.getNome() : "Desconhecida";
    }

    public String getDataVotoFormatada() {
        return dataVoto != null ?
                dataVoto.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) :
                "Data não disponível";
    }

    public String getIpMascarado() {
        if (ipOrigem == null) {
            return "IP não registrado";
        }
        String[] parts = ipOrigem.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".*.*";
        }
        return "IP mascarado";
    }

    public String getResumoVoto() {
        return String.format("Voto %s - %s - %s em %s",
                getTipoVotoDescricao(),
                getNomeMembro(),
                getNomeCandidato(),
                getDataVotoFormatada());
    }

    public boolean isSameVote(Membro membro, Cargo cargoPretendido, Eleicao eleicao) {
        return Objects.equals(this.membro, membro) &&
               Objects.equals(this.cargoPretendido, cargoPretendido) &&
               Objects.equals(this.eleicao, eleicao);
    }

    @PrePersist
    @PreUpdate
    public void validarAntesDePeristir() {
        validarConsistencia();
        validarCandidatoEleicao();
    }

    // Validation Methods
    public void validarConsistencia() {
        if (membro == null) {
            throw new IllegalStateException("Membro é obrigatório");
        }
        if (eleicao == null) {
            throw new IllegalStateException("Eleição é obrigatória");
        }
        if (cargoPretendido == null) {
            throw new IllegalStateException("Cargo pretendido é obrigatório");
        }

        if (TipoVoto.CANDIDATO.equals(tipoVoto) && candidato == null) {
            throw new IllegalStateException("Voto em candidato deve ter candidato associado");
        }
    }

    public void validarCandidatoEleicao() {
        if (candidato != null) {
            // Candidato deve pertencer à mesma eleição
            if (!Objects.equals(candidato.getEleicaoId(), eleicao.getId())) {
                throw new IllegalStateException("Candidato não pertence à eleição especificada");
            }

            // Candidato deve pertencer ao cargo especificado
            if (!Objects.equals(candidato.getCargoPretendidoId(), cargoPretendido.getId())) {
                throw new IllegalStateException("Candidato não pertence ao cargo especificado");
            }
        }
    }

    public void validarCandidatoCompativel() {
        if (candidato != null && cargoPretendido != null) {
            if (!Objects.equals(candidato.getCargoPretendido(), this.cargoPretendido)) {
                throw new IllegalStateException("Candidato não pertence ao cargo pretendido especificado");
            }
        }
    }

    public void validarVotoCompleto() {
        validarConsistencia();
        validarCandidatoCompativel();
    }

    public boolean isVotoParaCandidato() {
        return TipoVoto.CANDIDATO.equals(tipoVoto) && candidato != null;
    }

    // Utility Methods
    public static String gerarHashVoto(UUID membroId, UUID candidatoId, LocalDateTime dataVoto) {
        String input = membroId.toString() +
                       (candidatoId != null ? candidatoId.toString() : "VOTO_ESPECIAL") +
                       dataVoto.toString();

        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    public static String gerarHashVoto(Membro membro, Candidato candidato, LocalDateTime dataVoto) {
        return gerarHashVoto(
                membro.getId(),
                candidato != null ? candidato.getId() : null,
                dataVoto
        );
    }

    // Status Methods
    public String getCargoAtualMembro() {
        return membro != null ? membro.getNomeCargoAtual() : null;
    }

    public String getEmailMembro() {
        return membro != null ? membro.getEmail() : null;
    }

    public boolean isMembroAtivo() {
        return membro != null && membro.isActive();
    }

    public boolean isEleicaoAtiva() {
        return eleicao != null && eleicao.isAtiva();
    }

    public boolean isCargoAtivoElegivel() {
        return cargoPretendido != null && cargoPretendido.isAtivo();
    }

    public boolean candidatoPodeReceberVotos() {
        return candidato != null && candidato.podeReceberVotos();
    }

    @Override
    public String toString() {
        return String.format("Voto{id=%s, tipo=%s, membro=%s, cargo=%s, eleicao=%s}",
                id, getTipoVotoDescricao(), getNomeMembro(), getNomeCargoPretendido(), getNomeEleicao());
    }
}