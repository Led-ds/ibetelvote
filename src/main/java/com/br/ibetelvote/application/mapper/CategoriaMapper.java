package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.categoria.dto.CategoriaBasicInfo;
import com.br.ibetelvote.application.categoria.dto.CategoriaResponse;
import com.br.ibetelvote.application.categoria.dto.CreateCategoriaRequest;
import com.br.ibetelvote.application.categoria.dto.UpdateCategoriaRequest;
import com.br.ibetelvote.domain.entities.Categoria;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CategoriaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cargos", ignore = true)
    Categoria toEntity(CreateCategoriaRequest request);

    @Mapping(target = "status", expression = "java(categoria.getStatus())")
    @Mapping(target = "displayName", expression = "java(categoria.getDisplayName())")
    @Mapping(target = "resumo", expression = "java(categoria.getResumo())")
    @Mapping(target = "temInformacoesCompletas", expression = "java(categoria.temInformacoesCompletas())")
    @Mapping(target = "totalCargos", expression = "java(categoria.getTotalCargos())")
    @Mapping(target = "totalCargosAtivos", expression = "java(categoria.getTotalCargosAtivos())")
    @Mapping(target = "totalCargosDisponiveis", expression = "java(categoria.getTotalCargosDisponiveis())")
    @Mapping(target = "estatisticas", expression = "java(categoria.getEstatisticas())")
    @Mapping(target = "podeSerRemovida", expression = "java(categoria.podeSerRemovida())")
    CategoriaResponse toResponse(Categoria categoria);

    default List<CategoriaResponse> toResponseList(List<Categoria> categorias) {
        if (categorias == null) {
            return null;
        }
        return categorias.stream()
                .map(this::toResponse)
                .toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cargos", ignore = true)
    void updateEntityFromRequest(UpdateCategoriaRequest request, @MappingTarget Categoria categoria);

    @Mapping(target = "status", expression = "java(categoria.getStatus())")
    @Mapping(target = "totalCargos", expression = "java(categoria.getTotalCargos())")
    CategoriaBasicInfo toBasicInfo(Categoria categoria);


    default List<CategoriaBasicInfo> toBasicInfoList(List<Categoria> categorias) {
        if (categorias == null) {
            return null;
        }
        return categorias.stream()
                .map(this::toBasicInfo)
                .toList();
    }


    @BeforeMapping
    default void beforeCreateMapping(CreateCategoriaRequest request) {
        if (request.getNome() != null) {
            request.setNome(Categoria.normalizarNome(request.getNome()));
        }

        // Se ordem não informada, será definida no service
        if (request.getOrdemExibicao() == null) {
            request.setOrdemExibicao(0); // Valor temporário
        }
    }


    @BeforeMapping
    default void beforeUpdateMapping(UpdateCategoriaRequest request) {
        if (request.getNome() != null) {
            request.setNome(Categoria.normalizarNome(request.getNome()));
        }
    }


    @AfterMapping
    default void afterCreateMapping(@MappingTarget Categoria categoria, CreateCategoriaRequest request) {
        // Garantir que a categoria tenha nome válido
        if (!Categoria.isNomeValido(categoria.getNome())) {
            throw new IllegalArgumentException("Nome da categoria inválido: " + categoria.getNome());
        }

        // Garantir que ordem seja válida
        if (!Categoria.isOrdemValida(categoria.getOrdemExibicao())) {
            throw new IllegalArgumentException("Ordem de exibição inválida: " + categoria.getOrdemExibicao());
        }
    }


    @AfterMapping
    default void afterUpdateMapping(@MappingTarget Categoria categoria, UpdateCategoriaRequest request) {
        // Validar nome se foi alterado
        if (request.getNome() != null && !Categoria.isNomeValido(categoria.getNome())) {
            throw new IllegalArgumentException("Nome da categoria inválido: " + categoria.getNome());
        }

        // Validar ordem se foi alterada
        if (request.getOrdemExibicao() != null && !Categoria.isOrdemValida(categoria.getOrdemExibicao())) {
            throw new IllegalArgumentException("Ordem de exibição inválida: " + categoria.getOrdemExibicao());
        }
    }


    @Named("toSimpleResponse")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "displayName", ignore = true)
    @Mapping(target = "resumo", ignore = true)
    @Mapping(target = "temInformacoesCompletas", ignore = true)
    @Mapping(target = "totalCargos", ignore = true)
    @Mapping(target = "totalCargosAtivos", ignore = true)
    @Mapping(target = "totalCargosDisponiveis", ignore = true)
    @Mapping(target = "estatisticas", ignore = true)
    @Mapping(target = "podeSerRemovida", ignore = true)
    CategoriaResponse toSimpleResponse(Categoria categoria);


    @Named("toEssentialResponse")
    default CategoriaResponse toEssentialResponse(Categoria categoria) {
        if (categoria == null) {
            return null;
        }

        return CategoriaResponse.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .descricao(categoria.getDescricao())
                .ordemExibicao(categoria.getOrdemExibicao())
                .ativo(categoria.getAtivo())
                .createdAt(categoria.getCreatedAt())
                .updatedAt(categoria.getUpdatedAt())
                .build();
    }


    default CategoriaBasicInfo createBasicInfo(java.util.UUID id, String nome,
                                               Integer ordemExibicao, Boolean ativo,
                                               long totalCargos) {
        return CategoriaBasicInfo.builder()
                .id(id)
                .nome(nome)
                .ordemExibicao(ordemExibicao)
                .ativo(ativo)
                .status(ativo != null && ativo ? "Ativa" : "Inativa")
                .totalCargos(totalCargos)
                .build();
    }


    @Named("toOptimizedResponseList")
    default List<CategoriaResponse> toOptimizedResponseList(List<Categoria> categorias) {
        if (categorias == null) {
            return null;
        }
        return categorias.stream()
                .map(this::toSimpleResponse)
                .toList();
    }


    default CategoriaResponse toResponseWithComputedFields(Categoria categoria,
                                                           boolean incluirEstatisticas,
                                                           boolean incluirValidacao) {
        if (categoria == null) {
            return null;
        }

        CategoriaResponse.CategoriaResponseBuilder builder = CategoriaResponse.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .descricao(categoria.getDescricao())
                .ordemExibicao(categoria.getOrdemExibicao())
                .ativo(categoria.getAtivo())
                .createdAt(categoria.getCreatedAt())
                .updatedAt(categoria.getUpdatedAt())
                .status(categoria.getStatus())
                .displayName(categoria.getDisplayName())
                .resumo(categoria.getResumo())
                .temInformacoesCompletas(categoria.temInformacoesCompletas());

        if (incluirEstatisticas) {
            builder.totalCargos(categoria.getTotalCargos())
                    .totalCargosAtivos(categoria.getTotalCargosAtivos())
                    .totalCargosDisponiveis(categoria.getTotalCargosDisponiveis())
                    .estatisticas(categoria.getEstatisticas());
        }

        if (incluirValidacao) {
            builder.podeSerRemovida(categoria.podeSerRemovida());
        }

        return builder.build();
    }
}