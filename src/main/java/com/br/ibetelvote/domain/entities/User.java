package com.br.ibetelvote.domain.entities;

import com.br.ibetelvote.domain.entities.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_role", columnList = "role")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // === CAMPOS DE AUTENTICAÇÃO ===
    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Password é obrigatório")
    @Column(name = "password", nullable = false)
    private String password;

    @NotNull(message = "Role é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    // === CAMPOS DE CONTROLE DE CONTA ===
    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Builder.Default
    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired = true;

    @Builder.Default
    @Column(name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked = true;

    @Builder.Default
    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired = true;

    // === CAMPOS DE AUDITORIA ===
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // === RELACIONAMENTO COM MEMBRO ===
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Membro membro;

    // === IMPLEMENTAÇÃO UserDetails ===
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }

    // === MÉTODOS DE NEGÓCIO - CONTROLE DE CONTA ===
    public void activate() {
        this.ativo = true;
    }

    public void deactivate() {
        this.ativo = false;
    }

    public void lockAccount() {
        this.accountNonLocked = false;
    }

    public void unlockAccount() {
        this.accountNonLocked = true;
    }

    public void expireAccount() {
        this.accountNonExpired = false;
    }

    public void expireCredentials() {
        this.credentialsNonExpired = false;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // === MÉTODOS DE NEGÓCIO - ROLES ===
    public boolean hasRole(UserRole role) {
        return this.role == role;
    }

    public boolean isAdmin() {
        return hasRole(UserRole.ADMINISTRADOR);
    }

    public boolean isProUser() {
        return hasRole(UserRole.UTILIZADOR_PRO);
    }

    public boolean isMember() {
        return hasRole(UserRole.MEMBRO);
    }

    public void promoteToAdmin() {
        this.role = UserRole.ADMINISTRADOR;
    }

    public void promoteToProUser() {
        this.role = UserRole.UTILIZADOR_PRO;
    }

    public void demoteToMember() {
        this.role = UserRole.MEMBRO;
    }

    public void changeRole(UserRole newRole) {
        this.role = newRole;
    }

    // === MÉTODOS UTILITÁRIOS ===
    public boolean isActive() {
        return this.ativo;
    }

    public boolean hasMembro() {
        return this.membro != null;
    }

    public String getDisplayName() {
        return hasMembro() ? membro.getNome() : email;
    }

    public String getFormattedRole() {
        return switch (this.role) {
            case ADMINISTRADOR -> "Administrador";
            case UTILIZADOR_PRO -> "Utilizador Pro";
            case MEMBRO -> "Membro";
        };
    }

    // === MÉTODOS DE CONVENIÊNCIA PARA DADOS DO MEMBRO ===
    public String getNome() {
        return hasMembro() ? membro.getNome() : null;
    }

    public String getFotoBase64() {
        return hasMembro() ? membro.getFotoBase64() : null;
    }

    public boolean hasFoto() {
        return hasMembro() && membro.hasPhoto();
    }

}