package com.br.ibetelvote.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "votos", indexes = {
        @Index(name = "idx_voto_eleicao_id", columnList = "eleicao_id"),
        @Index(name = "idx_voto_cargo_id", columnList = "cargo_id"),
        @Index(name = "idx_voto_membro_id", columnList = "membro_id"),
        @Index(name = "idx_voto_candidato_id", columnList = "candidato_id"),
        @Index(name = "idx_voto_data", columnList = "data_voto"),
        @Index(name = "idx_voto_hash", columnList = "hash_voto")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_voto_membro_cargo", columnNames = {"membro_id", "cargo_id", "eleicao_id"})
})
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
    @Column(name = "membro_id", nullable = false)
    private UUID membroId;

    @NotNull(message = "Eleição é obrigatória")
    @Column(name = "eleicao_id", nullable = false)
    private UUID eleicaoId;

    @NotNull(message = "Cargo é obrigatório")
    @Column(name = "cargo_id", nullable = false)
    private UUID cargoId;

    // Candidato é opcional (voto em branco ou nulo não tem candidato)
    @Column(name = "candidato_id")
    private UUID candidatoId;

    @Builder.Default
    @Column(name = "voto_branco", nullable = false)
    private Boolean votoBranco = false;

    @Builder.Default
    @Column(name = "voto_nulo", nullable = false)
    private Boolean votoNulo = false;

    @Column(name = "hash_voto", length = 64)
    private String hashVoto;

    @Column(name = "ip_origem", length = 45)
    private String ipOrigem;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "data_voto", nullable = false, updatable = false)
    private LocalDateTime dataVoto;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_id", insertable = false, updatable = false)
    private Candidato candidato;

    // === MÉTODOS DE NEGÓCIO ===

    public static Voto criarVotoValido(UUID membroId, UUID eleicaoId, UUID cargoId, UUID candidatoId) {
        return Voto.builder()
                .membroId(membroId)
                .eleicaoId(eleicaoId)
                .cargoId(cargoId)
                .candidatoId(candidatoId)
                .votoBranco(false)
                .votoNulo(false)
                .build();
    }

    public static Voto criarVotoBranco(UUID membroId, UUID eleicaoId, UUID cargoId) {
        return Voto.builder()
                .membroId(membroId)
                .eleicaoId(eleicaoId)
                .cargoId(cargoId)
                .candidatoId(null)
                .votoBranco(true)
                .votoNulo(false)
                .build();
    }

    public static Voto criarVotoNulo(UUID membroId, UUID eleicaoId, UUID cargoId) {
        return Voto.builder()
                .membroId(membroId)
                .eleicaoId(eleicaoId)
                .cargoId(cargoId)
                .candidatoId(null)
                .votoBranco(false)
                .votoNulo(true)
                .build();
    }

    public void definirHashSeguranca(String hash) {
        this.hashVoto = hash;
    }

    public void definirDadosOrigem(String ipOrigem, String userAgent) {
        this.ipOrigem = ipOrigem;
        this.userAgent = userAgent;
    }

    // === MÉTODOS DE VALIDAÇÃO ===

    public boolean isVotoValido() {
        return candidatoId != null && !isVotoBranco() && !isVotoNulo();
    }

    public boolean isVotoBranco() {
        return votoBranco != null && votoBranco;
    }

    public boolean isVotoNulo() {
        return votoNulo != null && votoNulo;
    }

    public boolean temCandidato() {
        return candidatoId != null;
    }

    public boolean temHash() {
        return hashVoto != null && !hashVoto.trim().isEmpty();
    }

    public boolean isVotoSeguro() {
        return temHash() && ipOrigem != null;
    }

    // === MÉTODOS UTILITÁRIOS ===

    public String getTipoVoto() {
        if (isVotoValido()) {
            return "Válido";
        } else if (isVotoBranco()) {
            return "Branco";
        } else if (isVotoNulo()) {
            return "Nulo";
        } else {
            return "Indefinido";
        }
    }

    public String getNomeCandidato() {
        if (candidato != null) {
            return candidato.getNomeCandidato();
        }
        return getTipoVoto();
    }

    public String getNumeroCandidato() {
        if (candidato != null && candidato.temNumero()) {
            return candidato.getNumeroCandidato();
        }
        return getTipoVoto().toUpperCase();
    }

    public String getNomeMembro() {
        return membro != null ? membro.getNome() : "Desconhecido";
    }

    public String getNomeCargo() {
        return cargo != null ? cargo.getNome() : "Desconhecido";
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
        // Mascarar IP para proteção (ex: 192.168.*.*)
        String[] parts = ipOrigem.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".*.*";
        }
        return "IP mascarado";
    }

    // === MÉTODOS DE AUDITORIA ===

    public String getResumoVoto() {
        return String.format("Voto %s - %s - %s em %s",
                getTipoVoto(),
                getNomeMembro(),
                getNomeCandidato(),
                getDataVotoFormatada());
    }

    public boolean isSameVote(UUID membroId, UUID cargoId, UUID eleicaoId) {
        return this.membroId.equals(membroId) &&
                this.cargoId.equals(cargoId) &&
                this.eleicaoId.equals(eleicaoId);
    }

    // === VALIDAÇÕES DE NEGÓCIO ===

    public void validarConsistencia() {
        // Validar que não pode ser branco E nulo ao mesmo tempo
        if (isVotoBranco() && isVotoNulo()) {
            throw new IllegalStateException("Voto não pode ser branco e nulo simultaneamente");
        }

        // Validar que voto válido deve ter candidato
        if (!isVotoBranco() && !isVotoNulo() && candidatoId == null) {
            throw new IllegalStateException("Voto válido deve ter candidato");
        }

        // Validar que voto branco/nulo não deve ter candidato
        if ((isVotoBranco() || isVotoNulo()) && candidatoId != null) {
            throw new IllegalStateException("Voto branco/nulo não deve ter candidato");
        }
    }

    // === MÉTODOS ESTÁTICOS UTILITÁRIOS ===

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
}