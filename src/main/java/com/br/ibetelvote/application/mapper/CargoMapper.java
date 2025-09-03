package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.cargo.dto.CargoBasicInfo;
import com.br.ibetelvote.application.cargo.dto.CargoResponse;
import com.br.ibetelvote.application.cargo.dto.CreateCargoRequest;
import com.br.ibetelvote.application.cargo.dto.UpdateCargoRequest;
import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Categoria;
import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import com.br.ibetelvote.infrastructure.repositories.CategoriaJpaRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {UUID.class, HierarquiaCargo.class}
)
public abstract class CargoMapper {

    @Autowired
    protected CategoriaJpaRepository categoriaRepository;

    // === CREATE MAPPING ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "categoria", source = "categoriaId", qualifiedByName = "mapCategoria")
    @Mapping(target = "elegibilidade", source = "elegibilidade") // Agora mapeia diretamente
    public abstract Cargo toEntity(CreateCargoRequest request);

    // === RESPONSE MAPPINGS ===
    @Mapping(target = "categoriaId", expression = "java(cargo.getCategoriaId())")
    @Mapping(target = "categoriaNome", expression = "java(cargo.getCategoriaNome())")
    @Mapping(target = "categoriaOrdemExibicao", source = "categoria.ordemExibicao")
    @Mapping(target = "hierarquiaDisplayName", expression = "java(cargo.getHierarquiaDisplayName())")
    @Mapping(target = "hierarquiaCor", expression = "java(cargo.getHierarquia() != null ? cargo.getHierarquia().getCor() : null)")
    @Mapping(target = "hierarquiaIcone", expression = "java(cargo.getHierarquia() != null ? cargo.getHierarquia().getIcone() : null)")
    @Mapping(target = "hierarquiaNivel", expression = "java(cargo.getHierarquia() != null ? cargo.getHierarquia().getOrdem() : null)")
    @Mapping(target = "requisitoResumo", expression = "java(cargo.getRequisitoResumo())")
    @Mapping(target = "elegibilidadeFormatada", expression = "java(cargo.getElegibilidadeFormatada())")
    @Mapping(target = "status", expression = "java(cargo.getStatus())")
    @Mapping(target = "displayName", expression = "java(cargo.getDisplayName())")
    @Mapping(target = "resumo", expression = "java(cargo.getResumo())")
    @Mapping(target = "temInformacoesCompletas", expression = "java(cargo.temInformacoesCompletas())")
    @Mapping(target = "podeSerUsadoEmEleicoes", expression = "java(cargo.podeSerUsadoEmEleicoes())")
    @Mapping(target = "temCategoria", expression = "java(cargo.temCategoria())")
    @Mapping(target = "proximaHierarquiaSugerida", expression = "java(cargo.getProximaHierarquiaSugerida())")
    @Mapping(target = "totalCandidatos", constant = "0L") // Será implementado quando houver candidatos
    @Mapping(target = "temCandidatos", constant = "false") // Será implementado quando houver candidatos
    @Mapping(target = "estatisticasResumo", expression = "java(cargo.getEstatisticasResumo())")
    @Mapping(target = "podeSerRemovido", constant = "true") // Validação será no service
    @Mapping(target = "podeSerEditado", constant = "true") // Validação será no service
    @Mapping(target = "podeSerPromovido", expression = "java(cargo.getProximaHierarquiaSugerida() != null)")
    @Mapping(target = "elegibilidade", source = "elegibilidade") // Mapeia diretamente a lista
    public abstract CargoResponse toResponse(Cargo cargo);

    /**
     * Converte lista de entidades para lista de responses
     */
    public List<CargoResponse> toResponseList(List<Cargo> cargos) {
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
    @Mapping(target = "categoria", source = "categoriaId", qualifiedByName = "mapCategoria")
    @Mapping(target = "elegibilidade", source = "elegibilidade") // Agora mapeia diretamente
    public abstract void updateEntityFromRequest(UpdateCargoRequest request, @MappingTarget Cargo cargo);

    // === BASIC INFO MAPPING ===
    @Mapping(target = "categoriaId", expression = "java(cargo.getCategoriaId())")
    @Mapping(target = "categoriaNome", expression = "java(cargo.getCategoriaNome())")
    @Mapping(target = "hierarquiaDisplayName", expression = "java(cargo.getHierarquiaDisplayName())")
    @Mapping(target = "hierarquiaIcone", expression = "java(cargo.getHierarquia() != null ? cargo.getHierarquia().getIcone() : null)")
    @Mapping(target = "status", expression = "java(cargo.getStatus())")
    public abstract CargoBasicInfo toBasicInfo(Cargo cargo);

    /**
     * Converte lista de entidades para lista de basic info
     */
    public List<CargoBasicInfo> toBasicInfoList(List<Cargo> cargos) {
        if (cargos == null) {
            return null;
        }
        return cargos.stream()
                .map(this::toBasicInfo)
                .toList();
    }

    // === MÉTODOS AUXILIARES ===

    /**
     * Mapeia UUID da categoria para entidade Categoria
     */
    @Named("mapCategoria")
    protected Categoria mapCategoria(UUID categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categoriaRepository.findById(categoriaId).orElse(null);
    }

    /**
     * Prepara dados antes da criação da entidade
     */
    @BeforeMapping
    public void beforeCreateMapping(CreateCargoRequest request) {
        if (request.getNome() != null) {
            request.setNome(Cargo.normalizarNome(request.getNome()));
        }

        // Normalizar elegibilidade
        if (request.getElegibilidade() != null && !request.getElegibilidade().isEmpty()) {
            request.normalizarElegibilidade();
        }

        // Validar compatibilidade hierarquia-elegibilidade
        if (!request.isHierarquiaCompativelComElegibilidade()) {
            throw new IllegalArgumentException("Hierarquia " + request.getHierarquia() +
                    " não é compatível com a elegibilidade informada");
        }
    }

    /**
     * Prepara dados antes da atualização da entidade
     */
    @BeforeMapping
    public void beforeUpdateMapping(UpdateCargoRequest request) {
        if (request.getNome() != null) {
            request.setNome(Cargo.normalizarNome(request.getNome()));
        }

        // Normalizar elegibilidade se foi informada
        if (request.getElegibilidade() != null && !request.getElegibilidade().isEmpty()) {
            request.normalizarElegibilidade();
        }
    }

    /**
     * Processa dados após o mapeamento de criação
     */
    @AfterMapping
    public void afterCreateMapping(@MappingTarget Cargo cargo, CreateCargoRequest request) {
        // Garantir que o cargo tenha nome válido
        if (!Cargo.isNomeValido(cargo.getNome())) {
            throw new IllegalArgumentException("Nome do cargo inválido: " + cargo.getNome());
        }

        // A elegibilidade já foi mapeada diretamente, mas garantir que não seja null
        if (cargo.getElegibilidade() == null) {
            cargo.setElegibilidade(request.getElegibilidade());
        }

        // Validar se pode ser disponibilizado para eleições
        if (request.getDisponivelEleicao() != null && request.getDisponivelEleicao()) {
            if (!request.podeSerDisponibilizadoParaEleicoes()) {
                throw new IllegalArgumentException("Cargo não possui informações suficientes para ser disponibilizado para eleições");
            }
        }
    }

    /**
     * Processa dados após o mapeamento de atualização
     */
    @AfterMapping
    public void afterUpdateMapping(@MappingTarget Cargo cargo, UpdateCargoRequest request) {
        // Garantir que o cargo tenha nome válido se foi alterado
        if (request.getNome() != null && !Cargo.isNomeValido(cargo.getNome())) {
            throw new IllegalArgumentException("Nome do cargo inválido: " + cargo.getNome());
        }

        // Garantir que elegibilidade não seja null após atualização
        if (cargo.getElegibilidade() == null) {
            cargo.setElegibilidade(request.getElegibilidade());
        }

        // Validar requisitos se foram alterados
        if (request.getRequisitosCargo() != null && !Cargo.isRequisitosValido(request.getRequisitosCargo())) {
            throw new IllegalArgumentException("Requisitos do cargo são muito longos (máximo 2000 caracteres)");
        }

        // Validar ordem de precedência se foi alterada
        if (request.getOrdemPrecedencia() != null && !Cargo.isOrdemPrecedenciaValida(request.getOrdemPrecedencia())) {
            throw new IllegalArgumentException("Ordem de precedência deve ser um número positivo");
        }

        // Validar elegibilidade se foi alterada
        if (request.getElegibilidade() != null && !Cargo.isElegibilidadeValida(request.getElegibilidade())) {
            throw new IllegalArgumentException("Lista de elegibilidade contém valores inválidos ou duplicados");
        }
    }
}