package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.eleicao.dto.*;
import com.br.ibetelvote.domain.entities.Cargo;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CargoMapper {

    // === CREATE MAPPING ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Cargo toEntity(CreateCargoRequest request);

    // === RESPONSE MAPPINGS ===
    @Mapping(target = "status", expression = "java(cargo.getStatus())")
    @Mapping(target = "displayName", expression = "java(cargo.getDisplayName())")
    @Mapping(target = "resumo", expression = "java(cargo.getResumo())")
    @Mapping(target = "temInformacoesCompletas", expression = "java(cargo.temInformacoesCompletas())")
    @Mapping(target = "podeSerUsadoEmEleicoes", expression = "java(cargo.podeSerUsadoEmEleicoes())")
    CargoResponse toResponse(Cargo cargo);

    /**
     * Converte lista de entidades para lista de responses (usa toResponse)
     */
    default List<CargoResponse> toResponseList(List<Cargo> cargos) {
        if (cargos == null) {
            return null;
        }
        return cargos.stream()
                .map(this::toResponse)
                .toList();
    }

    // === UPDATE MAPPING ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateCargoRequest request, @MappingTarget Cargo cargo);

    // === BASIC INFO MAPPING ===
    @Mapping(target = "status", expression = "java(cargo.getStatus())")
    CargoBasicInfo toBasicInfo(Cargo cargo);

    /**
     * Converte lista de entidades para lista de basic info
     */
    default List<CargoBasicInfo> toBasicInfoList(List<Cargo> cargos) {
        if (cargos == null) {
            return null;
        }
        return cargos.stream()
                .map(this::toBasicInfo)
                .toList();
    }

    // === MÉTODOS AUXILIARES ===

    /**
     * Prepara dados antes da criação da entidade
     */
    @BeforeMapping
    default void beforeCreateMapping(CreateCargoRequest request) {
        if (request.getNome() != null) {
            request.setNome(Cargo.normalizarNome(request.getNome()));
        }
    }

    /**
     * Prepara dados antes da atualização da entidade
     */
    @BeforeMapping
    default void beforeUpdateMapping(UpdateCargoRequest request) {
        if (request.getNome() != null) {
            request.setNome(Cargo.normalizarNome(request.getNome()));
        }
    }

    /**
     * Aplica validações após o mapeamento
     */
    @AfterMapping
    default void afterCreateMapping(@MappingTarget Cargo cargo, CreateCargoRequest request) {
        // Garantir que o cargo tenha nome válido
        if (!Cargo.isNomeValido(cargo.getNome())) {
            throw new IllegalArgumentException("Nome do cargo inválido: " + cargo.getNome());
        }
    }

    /**
     * Converte cargo para response simplificado (sem campos computados)
     * Uso específico quando não precisamos de campos calculados
     */
    @Named("toSimpleResponse")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "displayName", ignore = true)
    @Mapping(target = "resumo", ignore = true)
    @Mapping(target = "temInformacoesCompletas", ignore = true)
    @Mapping(target = "podeSerUsadoEmEleicoes", ignore = true)
    CargoResponse toSimpleResponse(Cargo cargo);

    /**
     * Converte apenas os dados essenciais do cargo
     * Uso específico para operações que precisam apenas dos dados básicos
     */
    @Named("toEssentialResponse")
    default CargoResponse toEssentialResponse(Cargo cargo) {
        if (cargo == null) {
            return null;
        }

        return CargoResponse.builder()
                .id(cargo.getId())
                .nome(cargo.getNome())
                .descricao(cargo.getDescricao())
                .ativo(cargo.getAtivo())
                .createdAt(cargo.getCreatedAt())
                .updatedAt(cargo.getUpdatedAt())
                .build();
    }

    /**
     * Cria um basic info a partir de dados mínimos
     */
    default CargoBasicInfo createBasicInfo(UUID id, String nome, Boolean ativo) {
        return CargoBasicInfo.builder()
                .id(id)
                .nome(nome)
                .ativo(ativo)
                .status(ativo != null && ativo ? "Ativo" : "Inativo")
                .build();
    }
}