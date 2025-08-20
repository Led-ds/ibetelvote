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
        @Index(name = "idx_voto_candidato_id", columnList = "candidato_id"),
        @Index(name = "idx_voto_membro_id", columnList = "membro_id"),
        @Index(name = "idx_voto_cargo_pretendido_id", columnList = "cargo_pretendido_id"),
        @Index(name = "idx_voto_data", columnList = "data_voto"),
        @Index(name = "idx_voto_hash", columnList = "hash_voto"),
        @Index(name = "idx_voto_tipo", columnList = "voto_branco, voto_nulo")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_voto_membro_cargo_eleicao",
                columnNames = {"membro_id", "cargo_pretendido_id", "eleicao_id"})
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

    @NotNull(message = "Cargo pretendido é obrigatório")
    @Column(name = "cargo_pretendido_id", nullable = false)
    private UUID cargoPretendidoId;

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
    @JoinColumn(name = "cargo_pretendido_id", insertable = false, updatable = false)
    private Cargo cargoPretendido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_id", insertable = false, updatable = false)
    private Candidato candidato;

    // === MÉTODOS DE CRIAÇÃO (FACTORY METHODS) ===

    /**
     * Cria um voto válido para um candidato específico
     */
    public static Voto criarVotoValido(UUID membroId, UUID eleicaoId, UUID candidatoId) {
        return Voto.builder()
                .membroId(membroId)
                .eleicaoId(eleicaoId)
                .candidatoId(candidatoId)
                .cargoPretendidoId(null) // Será definido após buscar o candidato
                .votoBranco(false)
                .votoNulo(false)
                .build();
    }

    /**
     * Cria um voto em branco para um cargo específico
     */
    public static Voto criarVotoBranco(UUID membroId, UUID eleicaoId, UUID cargoPretendidoId) {
        return Voto.builder()
                .membroId(membroId)
                .eleicaoId(eleicaoId)
                .cargoPretendidoId(cargoPretendidoId)
                .candidatoId(null)
                .votoBranco(true)
                .votoNulo(false)
                .build();
    }

    /**
     * Cria um voto nulo para um cargo específico
     */
    public static Voto criarVotoNulo(UUID membroId, UUID eleicaoId, UUID cargoPretendidoId) {
        return Voto.builder()
                .membroId(membroId)
                .eleicaoId(eleicaoId)
                .cargoPretendidoId(cargoPretendidoId)
                .candidatoId(null)
                .votoBranco(false)
                .votoNulo(true)
                .build();
    }

    /**
     * Define o cargo pretendido baseado no candidato escolhido
     */
    public void definirCargoPretendidoPorCandidato(Candidato candidato) {
        if (candidato != null) {
            this.cargoPretendidoId = candidato.getCargoPretendidoId();
        }
    }

    /**
     * Define o hash de segurança do voto
     */
    public void definirHashSeguranca(String hash) {
        this.hashVoto = hash;
    }

    /**
     * Define os dados de origem da votação
     */
    public void definirDadosOrigem(String ipOrigem, String userAgent) {
        this.ipOrigem = ipOrigem;
        this.userAgent = userAgent;
    }

    // === MÉTODOS DE VALIDAÇÃO ===

    /**
     * Verifica se é um voto válido (tem candidato e não é branco/nulo)
     */
    public boolean isVotoValido() {
        return candidatoId != null && !isVotoBranco() && !isVotoNulo();
    }

    /**
     * Verifica se é um voto em branco
     */
    public boolean isVotoBranco() {
        return votoBranco != null && votoBranco;
    }

    /**
     * Verifica se é um voto nulo
     */
    public boolean isVotoNulo() {
        return votoNulo != null && votoNulo;
    }

    /**
     * Verifica se tem candidato associado
     */
    public boolean temCandidato() {
        return candidatoId != null;
    }

    /**
     * Verifica se tem hash de segurança
     */
    public boolean temHash() {
        return hashVoto != null && !hashVoto.trim().isEmpty();
    }

    /**
     * Verifica se o voto tem dados de segurança
     */
    public boolean isVotoSeguro() {
        return temHash() && ipOrigem != null;
    }

    /**
     * Retorna o tipo do voto como string
     */
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

    /**
     * Retorna o nome do candidato ou tipo de voto
     */
    public String getNomeCandidato() {
        if (candidato != null) {
            return candidato.getNomeCandidato();
        }
        return getTipoVoto();
    }

    /**
     * Retorna o número do candidato ou tipo de voto
     */
    public String getNumeroCandidato() {
        if (candidato != null && candidato.temNumero()) {
            return candidato.getNumeroCandidato();
        }
        return getTipoVoto().toUpperCase();
    }

    /**
     * Retorna o nome do membro votante
     */
    public String getNomeMembro() {
        return membro != null ? membro.getNome() : "Desconhecido";
    }

    public String getNomeCargoPretendido() {
        return cargoPretendido != null ? cargoPretendido.getNome() : "Desconhecido";
    }

    /**
     * Retorna o nome da eleição
     */
    public String getNomeEleicao() {
        return eleicao != null ? eleicao.getNome() : "Desconhecida";
    }

    /**
     * Retorna a data formatada da votação
     */
    public String getDataVotoFormatada() {
        return dataVoto != null ?
                dataVoto.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) :
                "Data não disponível";
    }

    /**
     * Retorna o IP mascarado para proteção de privacidade
     */
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

    /**
     * Retorna um resumo do voto para auditoria
     */
    public String getResumoVoto() {
        return String.format("Voto %s - %s - %s em %s",
                getTipoVoto(),
                getNomeMembro(),
                getNomeCandidato(),
                getDataVotoFormatada());
    }

    /**
     * Verifica se é o mesmo voto (mesmo membro, cargo e eleição)
     */
    public boolean isSameVote(UUID membroId, UUID cargoPretendidoId, UUID eleicaoId) {
        return this.membroId.equals(membroId) &&
                this.cargoPretendidoId.equals(cargoPretendidoId) &&
                this.eleicaoId.equals(eleicaoId);
    }

    /**
     * Valida a consistência interna do voto
     */
    public void validarConsistencia() {
        // Validar IDs obrigatórios
        if (membroId == null) {
            throw new IllegalStateException("Membro é obrigatório");
        }
        if (eleicaoId == null) {
            throw new IllegalStateException("Eleição é obrigatória");
        }
        if (cargoPretendidoId == null) {
            throw new IllegalStateException("Cargo pretendido é obrigatório");
        }

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

    public void validarCandidatoCompativel() {
        if (candidato != null && cargoPretendidoId != null) {
            if (!candidato.getCargoPretendidoId().equals(this.cargoPretendidoId)) {
                throw new IllegalStateException("Candidato não pertence ao cargo pretendido especificado");
            }
        }
    }

    public void validarElegibilidadeMembro() {
        if (membro != null && cargoPretendido != null) {
            // TODO: Implementar validações específicas se necessário
            // Por exemplo, verificar se membro pode votar em determinados cargos
        }
    }

    /**
     * Executa todas as validações do voto
     */
    public void validarVotoCompleto() {
        validarConsistencia();
        validarCandidatoCompativel();
        validarElegibilidadeMembro();
    }

    // === MÉTODOS ESTÁTICOS UTILITÁRIOS ===

    /**
     * Gera hash de segurança para o voto
     */
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

    public static boolean isTipoVotoPermitido(boolean votoBranco, boolean votoNulo, UUID candidatoId) {
        // Exatamente um tipo deve ser verdadeiro
        int tiposAtivos = 0;
        if (votoBranco) tiposAtivos++;
        if (votoNulo) tiposAtivos++;
        if (candidatoId != null) tiposAtivos++;

        return tiposAtivos == 1;
    }

    // === MÉTODOS DE INFORMAÇÃO ADICIONAL ===

    /**
     * Retorna informações do cargo atual do membro
     */
    public String getCargoAtualMembro() {
        return membro != null ? membro.getNomeCargoAtual() : null;
    }

    /**
     * Retorna o email do membro (para auditoria)
     */
    public String getEmailMembro() {
        return membro != null ? membro.getEmail() : null;
    }

    /**
     * Verifica se o membro está ativo
     */
    public boolean isMembroAtivo() {
        return membro != null && membro.isActive();
    }

    /**
     * Verifica se a eleição está ativa
     */
    public boolean isEleicaoAtiva() {
        return eleicao != null && eleicao.isAtiva();
    }

    /**
     * Verifica se o cargo está ativo
     */
    public boolean isCargoAtivoElegivel() {
        return cargoPretendido != null && cargoPretendido.isAtivo();
    }

    /**
     * Verifica se o candidato pode receber votos
     */
    public boolean candidatoPodeReceberVotos() {
        return candidato != null && candidato.podeReceberVotos();
    }

    @Override
    public String toString() {
        return String.format("Voto{id=%s, tipo=%s, membro=%s, cargo=%s, eleicao=%s}",
                id, getTipoVoto(), getNomeMembro(), getNomeCargoPretendido(), getNomeEleicao());
    }
}