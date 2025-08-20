package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.eleicao.dto.CargoBasicInfo;
import com.br.ibetelvote.application.eleicao.dto.MembroBasicInfo;
import com.br.ibetelvote.application.membro.dto.*;
import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Membro;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MembroMapper {

    // === CREATE MAPPING ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cargoAtual", ignore = true)
    @Mapping(target = "user", ignore = true)
    Membro toEntity(CreateMembroRequest request);

    // === RESPONSE MAPPINGS ===
    @Mapping(target = "cargoAtual", source = "cargoAtual", qualifiedByName = "mapCargoToBasicInfo")
    @Mapping(target = "nomeCargoAtual", expression = "java(membro.getNomeCargoAtual())")
    @Mapping(target = "displayName", expression = "java(membro.getDisplayName())")
    @Mapping(target = "primaryPhone", expression = "java(membro.getPrimaryPhone())")
    @Mapping(target = "fullAddress", expression = "java(membro.getFullAddress())")
    @Mapping(target = "idadeAproximada", expression = "java(membro.getIdadeAproximada())")
    @Mapping(target = "tempoComoMembro", expression = "java(membro.getTempoComoMembro())")
    @Mapping(target = "basicProfileComplete", expression = "java(membro.isBasicProfileComplete())")
    @Mapping(target = "hasCompleteAddress", expression = "java(membro.hasCompleteAddress())")
    @Mapping(target = "hasContactInfo", expression = "java(membro.hasContactInfo())")
    @Mapping(target = "canCreateUser", expression = "java(membro.canCreateUser())")
    @Mapping(target = "hasCargoAtual", expression = "java(membro.hasCargoAtual())")
    @Mapping(target = "hasPhoto", expression = "java(membro.hasPhoto())")
    @Mapping(target = "hasUser", expression = "java(membro.hasUser())")
    @Mapping(target = "fotoBase64", ignore = true) // Controlado manualmente quando necessário
    MembroResponse toResponse(Membro membro);

    /**
     * Converte lista de entidades para lista de responses
     */
    default List<MembroResponse> toResponseList(List<Membro> membros) {
        if (membros == null) {
            return null;
        }
        return membros.stream()
                .map(this::toResponse)
                .toList();
    }

    // === UPDATE MAPPING ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cargoAtual", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromRequest(UpdateMembroRequest request, @MappingTarget Membro membro);

    // === BASIC INFO MAPPING ===
    @Mapping(target = "nomeCargoAtual", expression = "java(membro.getNomeCargoAtual())")
    @Mapping(target = "hasUser", expression = "java(membro.hasUser())")
    MembroBasicInfo toBasicInfo(Membro membro);

    /**
     * Converte lista de entidades para lista de basic info
     */
    default List<MembroBasicInfo> toBasicInfoList(List<Membro> membros) {
        if (membros == null) {
            return null;
        }
        return membros.stream()
                .map(this::toBasicInfo)
                .toList();
    }

    // === LIST RESPONSE MAPPING ===
    @Mapping(target = "nomeCargoAtual", expression = "java(membro.getNomeCargoAtual())")
    @Mapping(target = "primaryPhone", expression = "java(membro.getPrimaryPhone())")
    @Mapping(target = "hasPhoto", expression = "java(membro.hasPhoto())")
    @Mapping(target = "hasUser", expression = "java(membro.hasUser())")
    @Mapping(target = "fotoBase64", ignore = true) // Controlado manualmente
    @Mapping(target = "userRole", ignore = true) // Será preenchido pelo service
    MembroListResponse toListResponse(Membro membro);

    /**
     * Converte lista de entidades para lista de list responses
     */
    default List<MembroListResponse> toListResponseList(List<Membro> membros) {
        if (membros == null) {
            return null;
        }
        return membros.stream()
                .map(this::toListResponse)
                .toList();
    }

    // === PROFILE RESPONSE MAPPING ===
    @Mapping(target = "nomeCargoAtual", expression = "java(membro.getNomeCargoAtual())")
    @Mapping(target = "temFoto", expression = "java(membro.hasPhoto())")
    @Mapping(target = "fotoBase64", expression = "java(membro.getFotoBase64())")
    @Mapping(target = "hasUser", expression = "java(membro.hasUser())")
    @Mapping(target = "isBasicProfileComplete", expression = "java(membro.isBasicProfileComplete())")
    @Mapping(target = "isFullProfileComplete", expression = "java(membro.isBasicProfileComplete() && membro.hasCompleteAddress())")
    MembroProfileResponse toProfileResponse(Membro membro);

    // === PROFILE UPDATE MAPPING ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cpf", ignore = true) // CPF não pode ser alterado via profile
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ativo", ignore = true) // Status não alterado via profile
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "cargoAtual", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "fotoData", ignore = true)
    @Mapping(target = "fotoTipo", ignore = true)
    @Mapping(target = "fotoNome", ignore = true)
    void updateEntityFromProfileRequest(UpdateMembroProfileRequest request, @MappingTarget Membro membro);

    // === ELEGIBILIDADE MAPPING ===
    @Mapping(target = "membroId", source = "id")
    @Mapping(target = "nomeMembro", source = "nome")
    @Mapping(target = "cargoAtual", expression = "java(membro.getNomeCargoAtual())")
    @Mapping(target = "podeVotar", expression = "java(membro.isActive())")
    @Mapping(target = "cargosElegiveis", ignore = true) // Será preenchido pelo service
    @Mapping(target = "motivoInelegibilidade", ignore = true) // Será preenchido pelo service
    MembroElegibilidadeResponse toElegibilidadeResponse(Membro membro);

    // === NAMED MAPPINGS ===

    /**
     * Mapeia Cargo para CargoBasicInfo
     */
    @Named("mapCargoToBasicInfo")
    default CargoBasicInfo mapCargoToBasicInfo(Cargo cargo) {
        if (cargo == null) return null;
        return CargoBasicInfo.builder()
                .id(cargo.getId())
                .nome(cargo.getNome())
                .ativo(cargo.getAtivo())
                .status(cargo.getStatus())
                .build();
    }

    // === MÉTODOS AUXILIARES ===

    /**
     * Prepara dados antes da criação da entidade
     */
    @BeforeMapping
    default void beforeCreateMapping(CreateMembroRequest request) {
        // Normalizar CPF - remover caracteres especiais
        if (request.getCpf() != null) {
            request.setCpf(request.getCpf().replaceAll("[^0-9]", ""));
        }

        // Normalizar email - converter para lowercase
        if (request.getEmail() != null) {
            request.setEmail(request.getEmail().toLowerCase().trim());
        }

        // Normalizar nome - primeira letra maiúscula
        if (request.getNome() != null) {
            request.setNome(normalizarNome(request.getNome()));
        }
    }

    /**
     * Prepara dados antes da atualização da entidade
     */
    @BeforeMapping
    default void beforeUpdateMapping(UpdateMembroRequest request) {
        // Normalizar CPF
        if (request.getCpf() != null) {
            request.setCpf(request.getCpf().replaceAll("[^0-9]", ""));
        }

        // Normalizar email
        if (request.getEmail() != null) {
            request.setEmail(request.getEmail().toLowerCase().trim());
        }

        // Normalizar nome
        if (request.getNome() != null) {
            request.setNome(normalizarNome(request.getNome()));
        }
    }

    /**
     * Prepara dados antes da atualização do perfil
     */
    @BeforeMapping
    default void beforeProfileUpdateMapping(UpdateMembroProfileRequest request) {
        // Normalizar email
        if (request.getEmail() != null) {
            request.setEmail(request.getEmail().toLowerCase().trim());
        }

        // Normalizar nome
        if (request.getNome() != null) {
            request.setNome(normalizarNome(request.getNome()));
        }
    }

    /**
     * Aplica validações após o mapeamento de criação
     */
    @AfterMapping
    default void afterCreateMapping(@MappingTarget Membro membro, CreateMembroRequest request) {
        // Validar CPF
        if (!Membro.isValidCPF(membro.getCpf())) {
            throw new IllegalArgumentException("CPF inválido: " + membro.getCpf());
        }

        // Validar dados básicos
        if (membro.getNome() == null || membro.getNome().trim().length() < 2) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 2 caracteres");
        }
    }

    /**
     * Aplica validações após o mapeamento de atualização
     */
    @AfterMapping
    default void afterUpdateMapping(@MappingTarget Membro membro, UpdateMembroRequest request) {
        // Validar CPF se foi fornecido
        if (request.getCpf() != null && !Membro.isValidCPF(membro.getCpf())) {
            throw new IllegalArgumentException("CPF inválido: " + membro.getCpf());
        }
    }

    // === MÉTODOS UTILITÁRIOS ===

    /**
     * Normaliza nome próprio
     */
    default String normalizarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return nome;
        }

        String[] palavras = nome.trim().split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (String palavra : palavras) {
            if (palavra.length() > 0) {
                if (resultado.length() > 0) {
                    resultado.append(" ");
                }
                // Primeira letra maiúscula, restante minúscula
                resultado.append(palavra.substring(0, 1).toUpperCase())
                        .append(palavra.substring(1).toLowerCase());
            }
        }

        return resultado.toString();
    }

    /**
     * Converte membro para response com foto
     */
    default MembroResponse toResponseWithPhoto(Membro membro) {
        MembroResponse response = toResponse(membro);
        if (membro.hasPhoto()) {
            response.setFotoBase64(membro.getFotoBase64());
        }
        return response;
    }

    /**
     * Converte membro para list response com foto
     */
    default MembroListResponse toListResponseWithPhoto(Membro membro) {
        MembroListResponse response = toListResponse(membro);
        if (membro.hasPhoto()) {
            response.setFotoBase64(membro.getFotoBase64());
        }
        return response;
    }

    /**
     * Cria basic info a partir de dados mínimos
     */
    default MembroBasicInfo createBasicInfo(java.util.UUID id, String nome, String email,
                                            String nomeCargoAtual, Boolean ativo) {
        return MembroBasicInfo.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .nomeCargoAtual(nomeCargoAtual)
                .ativo(ativo)
                .hasUser(false) // Será definido pelo service se necessário
                .build();
    }
}