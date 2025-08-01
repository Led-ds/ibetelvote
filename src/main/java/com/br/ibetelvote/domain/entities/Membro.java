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
        @Index(name = "idx_membro_user_id", columnList = "user_id")
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

    @NotBlank(message = "Nome é obrigatório")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "cargo", length = 100)
    private String cargo;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "foto", length = 500)
    private String foto;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "endereco", length = 255)
    private String endereco;

    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Business Methods
    public void activate() {
        this.ativo = true;
    }

    public void deactivate() {
        this.ativo = false;
    }

    public void updateProfile(String nome, String cargo, LocalDate dataNascimento, String telefone, String endereco) {
        this.nome = nome;
        this.cargo = cargo;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.endereco = endereco;
    }

    public void updatePhoto(String foto) {
        this.foto = foto;
    }

    public void associateUser(UUID userId) {
        this.userId = userId;
    }

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

    private String getDefaultPhotoUrl() {
        return "/api/v1/files/default-avatar.png";
    }
}