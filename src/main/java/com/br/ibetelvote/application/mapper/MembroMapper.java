package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.membro.dto.*;
import com.br.ibetelvote.domain.entities.Membro;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MembroMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ativo", constant = "true")
    @Mapping(target = "foto", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    Membro toEntity(CreateMembroRequest request);

    MembroResponse toResponse(Membro membro);

    @Mapping(target = "hasUser", expression = "java(membro.hasUser())")
    MembroListResponse toListResponse(Membro membro);

    @Mapping(target = "hasUser", expression = "java(membro.hasUser())")
    @Mapping(target = "isBasicProfileComplete", expression = "java(membro.isBasicProfileComplete())")
    @Mapping(target = "isFullProfileComplete", expression = "java(membro.isFullProfileComplete())")
    @Mapping(target = "photoUrl", ignore = true)
    MembroProfileResponse toProfileResponse(Membro membro);

    List<MembroResponse> toResponseList(List<Membro> membros);

    List<MembroListResponse> toListResponseList(List<Membro> membros);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "foto", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromRequest(UpdateMembroRequest request, @MappingTarget Membro membro);

    @AfterMapping
    default void setDefaults(@MappingTarget Membro membro) {
        if (membro.getAtivo() == null) {
            membro.setAtivo(true);
        }
    }
}
