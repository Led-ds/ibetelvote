package com.br.ibetelvote.domain.entities.enus;

import lombok.Getter;

@Getter
public enum UserRole {
    MEMBRO("Membro", 1),
    UTILIZADOR_PRO("Utilizador Pro", 2),
    ADMINISTRADOR("Administrador", 3);

    private final String displayName;
    private final int level;

    UserRole(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public boolean hasHigherOrEqualLevel(UserRole other) {
        return this.level >= other.level;
    }

    public boolean canManage(UserRole targetRole) {
        return this.level > targetRole.level;
    }

    public static UserRole fromString(String role) {
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Role inválido: " + role);
        }
    }
}
