package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.auth.dto.*;
import com.br.ibetelvote.domain.entities.Membro;
import com.br.ibetelvote.domain.entities.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    // === CREATE MAPPING ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ativo", constant = "true")
    @Mapping(target = "accountNonExpired", constant = "true")
    @Mapping(target = "accountNonLocked", constant = "true")
    @Mapping(target = "credentialsNonExpired", constant = "true")
    @Mapping(target = "membro", ignore = true)
    User toEntity(CreateUserRequest request);

    // === RESPONSE MAPPINGS ===
    @Mapping(target = "formattedRole", expression = "java(user.getFormattedRole())")
    @Mapping(target = "membro", source = "membro", qualifiedByName = "mapMembroToBasicInfo")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    // === USER PROFILE RESPONSE (para compatibilidade com auth existente) ===
    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "nome", source = "membro.nome")
    @Mapping(target = "foto", source = "membro.foto")
    @Mapping(target = "cargo", source = "membro.cargo")
    @Mapping(target = "dataNascimento", source = "membro.dataNascimento")
    UserProfileResponse toUserProfileResponse(User user);

    // === NAMED MAPPINGS ===
    @Named("mapMembroToBasicInfo")
    default MembroBasicInfo mapMembroToBasicInfo(Membro membro) {
        if (membro == null) {
            return null;
        }
        return MembroBasicInfo.builder()
                .id(membro.getId())
                .nome(membro.getNome())
                .foto(membro.getFoto())
                .cargo(membro.getCargo())
                .ativo(membro.getAtivo())
                .build();
    }

    // === POST-MAPPING CUSTOMIZATIONS ===
    @AfterMapping
    default void setDefaults(@MappingTarget User user) {
        if (user.getAtivo() == null) {
            user.setAtivo(true);
        }
        if (user.getAccountNonExpired() == null) {
            user.setAccountNonExpired(true);
        }
        if (user.getAccountNonLocked() == null) {
            user.setAccountNonLocked(true);
        }
        if (user.getCredentialsNonExpired() == null) {
            user.setCredentialsNonExpired(true);
        }
    }

    @AfterMapping
    default void setComputedFields(@MappingTarget UserResponse response, User user) {
        // Campos já mapeados via expressions, mas podemos adicionar lógica extra se necessário
    }
}