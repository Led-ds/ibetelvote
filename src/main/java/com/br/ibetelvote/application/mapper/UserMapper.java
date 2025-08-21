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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ativo", constant = "true")
    @Mapping(target = "accountNonExpired", constant = "true")
    @Mapping(target = "accountNonLocked", constant = "true")
    @Mapping(target = "credentialsNonExpired", constant = "true")
    @Mapping(target = "membro", ignore = true)
    User toEntity(CreateUserRequest request);

    @Mapping(target = "formattedRole", expression = "java(user.getFormattedRole())")
    @Mapping(target = "membro", source = "membro", qualifiedByName = "mapMembroToBasicInfo")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "nome", source = "membro.nome")
    @Mapping(target = "fotoBase64", expression = "java(user.getMembro() != null ? user.getMembro().getFotoBase64() : null)")
    @Mapping(target = "cargo", expression = "java(user.getMembro() != null ? user.getMembro().getNomeCargoAtual() : null)")
    @Mapping(target = "dataNascimento", source = "membro.dataNascimento")
    UserProfileResponse toUserProfileResponse(User user);

    @Named("mapMembroToBasicInfo")
    default MembroBasicInfo mapMembroToBasicInfo(Membro membro) {
        if (membro == null) {
            return null;
        }
        return MembroBasicInfo.builder()
                .id(membro.getId())
                .nome(membro.getNome())
                .fotoBase64(membro.getFotoBase64())
                .cargo(membro.getNomeCargoAtual())
                .ativo(membro.getAtivo())
                .build();
    }

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