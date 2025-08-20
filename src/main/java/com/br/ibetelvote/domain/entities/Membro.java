package com.br.ibetelvote.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Entity
@Table(name = "membros", indexes = {
        @Index(name = "idx_membro_email", columnList = "email", unique = true),
        @Index(name = "idx_membro_nome", columnList = "nome"),
        @Index(name = "idx_membro_ativo", columnList = "ativo"),
        @Index(name = "idx_membro_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_membro_cargo_atual", columnList = "cargo_atual_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Membro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // === DADOS PESSOAIS BÁSICOS ===
    @NotBlank(message = "Nome é obrigatório")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @Column(name = "cpf", nullable = false, unique = true, length = 14)
    private String cpf;

    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    // === CARGO ATUAL (REFATORADO) ===
    @Column(name = "cargo_atual_id")
    private UUID cargoAtualId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_atual_id", insertable = false, updatable = false)
    private Cargo cargoAtual;

    // === DADOS ESPECÍFICOS DA IGREJA ===
    @Column(name = "departamento", length = 100)
    private String departamento;

    @Column(name = "data_batismo")
    private LocalDate dataBatismo;

    @Column(name = "data_membro_desde")
    private LocalDate dataMembroDesde;

    // === DADOS DE CONTATO ===
    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "celular", length = 20)
    private String celular;

    @Column(name = "endereco", length = 255)
    private String endereco;

    @Column(name = "cidade", length = 100)
    private String cidade;

    @Column(name = "estado", length = 2)
    private String estado;

    @Column(name = "cep", length = 10)
    private String cep;

    // === DADOS DA FOTO ===
    @Lob
    @Column(name = "foto_data")
    private byte[] fotoData;

    @Column(name = "foto_tipo", length = 50)
    private String fotoTipo;

    @Column(name = "foto_nome", length = 255)
    private String fotoNome;

    // === DADOS ADICIONAIS ===
    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    // === CONTROLE ===
    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    // === RELACIONAMENTO COM USER ===
    @Column(name = "user_id", unique = true)
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    // === AUDITORIA ===
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // === MÉTODOS DE NEGÓCIO ===

    /**
     * Ativa o membro
     */
    public void activate() {
        this.ativo = true;
    }

    /**
     * Desativa o membro
     */
    public void deactivate() {
        this.ativo = false;
    }

    /**
     * Atualiza perfil básico do membro
     */
    public void updateBasicProfile(String nome, String email, LocalDate dataNascimento, String cpf) {
        this.nome = nome;
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.cpf = cpf;
    }

    /**
     * Atualiza informações da igreja
     */
    public void updateChurchInfo(UUID cargoAtualId, String departamento, LocalDate dataBatismo, LocalDate dataMembroDesde) {
        this.cargoAtualId = cargoAtualId;
        this.departamento = departamento;
        this.dataBatismo = dataBatismo;
        this.dataMembroDesde = dataMembroDesde;
    }

    /**
     * Atualiza informações de contato
     */
    public void updateContactInfo(String telefone, String celular, String endereco, String cidade, String estado, String cep) {
        this.telefone = telefone;
        this.celular = celular;
        this.endereco = endereco;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
    }

    /**
     * Atualiza a foto do membro
     */
    public void updatePhoto(byte[] fotoData, String fotoTipo, String fotoNome) {
        this.fotoData = fotoData;
        this.fotoTipo = fotoTipo;
        this.fotoNome = fotoNome;
    }

    /**
     * Remove a foto do membro
     */
    public void removePhoto() {
        this.fotoData = null;
        this.fotoTipo = null;
        this.fotoNome = null;
    }

    /**
     * Atualiza cargo atual do membro
     */
    public void updateCargoAtual(UUID cargoAtualId) {
        this.cargoAtualId = cargoAtualId;
    }

    /**
     * Remove cargo atual do membro
     */
    public void removeCargoAtual() {
        this.cargoAtualId = null;
    }

    /**
     * Associa membro a um usuário
     */
    public void associateUser(UUID userId) {
        this.userId = userId;
    }

    /**
     * Desassocia membro do usuário
     */
    public void dissociateUser() {
        this.userId = null;
    }

    /**
     * Atualiza observações
     */
    public void updateObservations(String observacoes) {
        this.observacoes = observacoes;
    }

    // === MÉTODOS DE VALIDAÇÃO ===

    /**
     * Verifica se o membro está ativo
     */
    public boolean isActive() {
        return this.ativo != null && this.ativo;
    }

    /**
     * Verifica se o membro tem foto
     */
    public boolean hasPhoto() {
        return this.fotoData != null && this.fotoData.length > 0;
    }

    /**
     * Verifica se o membro tem usuário associado
     */
    public boolean hasUser() {
        return this.userId != null;
    }

    /**
     * Verifica se o membro tem cargo atual
     */
    public boolean hasCargoAtual() {
        return this.cargoAtualId != null;
    }

    /**
     * Verifica se pode criar usuário
     */
    public boolean canCreateUser() {
        return this.ativo &&
                this.cpf != null && !this.cpf.trim().isEmpty() &&
                this.email != null && !this.email.trim().isEmpty() &&
                this.userId == null;
    }

    /**
     * Verifica se o membro pode se candidatar para um cargo específico
     */
    public boolean podeSeCandidarPara(Cargo cargoDesejado) {
        if (!isActive() || cargoDesejado == null || !cargoDesejado.isAtivo()) {
            return false;
        }

        // Se não tem cargo atual, só pode se candidatar para Diácono
        if (!hasCargoAtual()) {
            return "Diácono".equalsIgnoreCase(cargoDesejado.getNome());
        }

        // Lógica de hierarquia baseada no cargo atual
        return validarHierarquiaEletiva(cargoDesejado);
    }

    /**
     * Valida hierarquia para eleições
     */
    private boolean validarHierarquiaEletiva(Cargo cargoDesejado) {
        if (cargoAtual == null) {
            return false;
        }

        String cargoAtualNome = cargoAtual.getNome();
        String cargoDesejadoNome = cargoDesejado.getNome();

        // Obreiro pode se candidatar apenas para Diácono
        if ("Obreiro".equalsIgnoreCase(cargoAtualNome)) {
            return "Diácono".equalsIgnoreCase(cargoDesejadoNome);
        }

        // Diácono pode se candidatar para Diácono (reeleição) ou Presbítero
        if ("Diácono".equalsIgnoreCase(cargoAtualNome)) {
            return "Diácono".equalsIgnoreCase(cargoDesejadoNome) ||
                    "Presbítero".equalsIgnoreCase(cargoDesejadoNome);
        }

        // Presbítero pode se candidatar apenas para Presbítero (reeleição)
        if ("Presbítero".equalsIgnoreCase(cargoAtualNome)) {
            return "Presbítero".equalsIgnoreCase(cargoDesejadoNome);
        }

        // Pastor não participa de eleições comuns
        return false;
    }

    // === MÉTODOS UTILITÁRIOS ===

    /**
     * Retorna nome para exibição
     */
    public String getDisplayName() {
        return this.nome;
    }

    /**
     * Retorna foto como Base64 para envio ao frontend
     */
    @Transient
    public String getFotoBase64() {
        if (hasPhoto()) {
            return "data:" + fotoTipo + ";base64," +
                    Base64.getEncoder().encodeToString(fotoData);
        }
        return null;
    }

    /**
     * Retorna nome do cargo atual
     */
    public String getNomeCargoAtual() {
        return cargoAtual != null ? cargoAtual.getNome() : "Sem cargo";
    }

    /**
     * Verifica se tem endereço completo
     */
    public boolean hasCompleteAddress() {
        return this.endereco != null && !this.endereco.trim().isEmpty() &&
                this.cidade != null && !this.cidade.trim().isEmpty() &&
                this.estado != null && !this.estado.trim().isEmpty();
    }

    /**
     * Verifica se tem informações de contato
     */
    public boolean hasContactInfo() {
        return (this.telefone != null && !this.telefone.trim().isEmpty()) ||
                (this.celular != null && !this.celular.trim().isEmpty());
    }

    /**
     * Verifica se perfil básico está completo
     */
    public boolean isBasicProfileComplete() {
        return this.nome != null && !this.nome.trim().isEmpty() &&
                this.email != null && !this.email.trim().isEmpty() &&
                this.cpf != null && !this.cpf.trim().isEmpty() &&
                this.dataNascimento != null &&
                hasContactInfo();
    }

    /**
     * Retorna telefone principal
     */
    public String getPrimaryPhone() {
        if (this.celular != null && !this.celular.trim().isEmpty()) {
            return this.celular;
        }
        return this.telefone;
    }

    /**
     * Retorna endereço completo
     */
    public String getFullAddress() {
        if (!hasCompleteAddress()) {
            return null;
        }
        return String.format("%s, %s - %s", this.endereco, this.cidade, this.estado);
    }

    /**
     * Calcula idade aproximada
     */
    public int getIdadeAproximada() {
        if (this.dataNascimento == null) {
            return 0;
        }
        return java.time.Period.between(this.dataNascimento, LocalDate.now()).getYears();
    }

    /**
     * Calcula tempo como membro
     */
    public int getTempoComoMembro() {
        if (this.dataMembroDesde == null) {
            return 0;
        }
        return java.time.Period.between(this.dataMembroDesde, LocalDate.now()).getYears();
    }

    // === MÉTODOS DE VALIDAÇÃO ESTÁTICOS ===

    /**
     * Valida CPF
     */
    public static boolean isValidCPF(String cpf) {
        if (cpf == null) return false;

        // Remove caracteres especiais
        cpf = cpf.replaceAll("[^0-9]", "");

        // Verifica se tem 11 dígitos
        if (cpf.length() != 11) return false;

        // Verifica se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) return false;

        // Validação dos dígitos verificadores (algoritmo CPF)
        try {
            int[] digits = cpf.chars().map(c -> c - '0').toArray();

            // Primeiro dígito verificador
            int sum1 = 0;
            for (int i = 0; i < 9; i++) {
                sum1 += digits[i] * (10 - i);
            }
            int digit1 = 11 - (sum1 % 11);
            if (digit1 >= 10) digit1 = 0;

            // Segundo dígito verificador
            int sum2 = 0;
            for (int i = 0; i < 10; i++) {
                sum2 += digits[i] * (11 - i);
            }
            int digit2 = 11 - (sum2 % 11);
            if (digit2 >= 10) digit2 = 0;

            return digits[9] == digit1 && digits[10] == digit2;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("Membro{id=%s, nome='%s', cargo='%s', ativo=%s}",
                id, nome, getNomeCargoAtual(), ativo);
    }
}