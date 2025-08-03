package com.br.ibetelvote.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "membros", indexes = {
        @Index(name = "idx_membro_email", columnList = "email", unique = true),
        @Index(name = "idx_membro_nome", columnList = "nome"),
        @Index(name = "idx_membro_ativo", columnList = "ativo"),
        @Index(name = "idx_membro_user_id", columnList = "user_id", unique = true)
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

    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    // === DADOS ESPECÍFICOS DA IGREJA ===
    @Column(name = "cargo", length = 100)
    private String cargo;

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

    // === DADOS ADICIONAIS ===
    @Column(name = "foto", length = 500)
    private String foto;

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
    public void activate() {
        this.ativo = true;
    }

    public void deactivate() {
        this.ativo = false;
    }

    public void updateBasicProfile(String nome, String email, LocalDate dataNascimento) {
        this.nome = nome;
        this.email = email;
        this.dataNascimento = dataNascimento;
    }

    public void updateChurchInfo(String cargo, String departamento, LocalDate dataBatismo, LocalDate dataMembroDesde) {
        this.cargo = cargo;
        this.departamento = departamento;
        this.dataBatismo = dataBatismo;
        this.dataMembroDesde = dataMembroDesde;
    }

    public void updateContactInfo(String telefone, String celular, String endereco, String cidade, String estado, String cep) {
        this.telefone = telefone;
        this.celular = celular;
        this.endereco = endereco;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
    }

    public void updatePhoto(String foto) {
        this.foto = foto;
    }

    public void removePhoto() {
        this.foto = null;
    }

    public void associateUser(UUID userId) {
        this.userId = userId;
    }

    public void dissociateUser() {
        this.userId = null;
    }

    public void updateObservations(String observacoes) {
        this.observacoes = observacoes;
    }

    // === MÉTODOS UTILITÁRIOS ===
    public boolean hasUser() {
        return this.userId != null;
    }

    public boolean isActive() {
        return this.ativo;
    }

    public String getDisplayName() {
        return this.nome;
    }

    public String getPhotoUrl() {
        return this.foto != null ? this.foto : getDefaultPhotoUrl();
    }

    public boolean hasPhoto() {
        return this.foto != null && !this.foto.trim().isEmpty();
    }

    public boolean hasCompleteAddress() {
        return this.endereco != null && !this.endereco.trim().isEmpty() &&
                this.cidade != null && !this.cidade.trim().isEmpty() &&
                this.estado != null && !this.estado.trim().isEmpty();
    }

    public boolean hasContactInfo() {
        return (this.telefone != null && !this.telefone.trim().isEmpty()) ||
                (this.celular != null && !this.celular.trim().isEmpty());
    }

    public boolean isProfileComplete() {
        return this.nome != null && !this.nome.trim().isEmpty() &&
                this.email != null && !this.email.trim().isEmpty() &&
                this.dataNascimento != null &&
                this.cargo != null && !this.cargo.trim().isEmpty() &&
                hasContactInfo();
    }

    public String getPrimaryPhone() {
        if (this.celular != null && !this.celular.trim().isEmpty()) {
            return this.celular;
        }
        return this.telefone;
    }

    public String getFullAddress() {
        if (!hasCompleteAddress()) {
            return null;
        }
        return String.format("%s, %s - %s", this.endereco, this.cidade, this.estado);
    }

    public int getIdadeAproximada() {
        if (this.dataNascimento == null) {
            return 0;
        }
        return java.time.Period.between(this.dataNascimento, LocalDate.now()).getYears();
    }

    public int getTempoComoMembro() {
        if (this.dataMembroDesde == null) {
            return 0;
        }
        return java.time.Period.between(this.dataMembroDesde, LocalDate.now()).getYears();
    }

    private String getDefaultPhotoUrl() {
        return "/api/v1/files/default-avatar.png";
    }

    // === MÉTODOS PARA VALIDAÇÃO ===
    public boolean isEmailValid() {
        return this.email != null && this.email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public boolean isPhoneValid(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is optional
        }
        return phone.matches("^\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}$");
    }

    public boolean isCepValid() {
        if (this.cep == null || this.cep.trim().isEmpty()) {
            return true; // CEP is optional
        }
        return this.cep.matches("^\\d{5}-?\\d{3}$");
    }
}