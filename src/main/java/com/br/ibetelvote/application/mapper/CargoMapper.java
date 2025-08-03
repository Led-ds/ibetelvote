package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.eleicao.dto.*;
import com.br.ibetelvote.domain.entities.Candidato;
import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Eleicao;
import org.mapstruct.*;

import java.util.List;

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
    @Mapping(target = "eleicao", ignore = true)
    @Mapping(target = "candidatos", ignore = true)
    @Mapping(target = "votos", ignore = true)
    Cargo toEntity(CreateCargoRequest request);

    // === RESPONSE MAPPINGS ===
    @Mapping(target = "eleicao", source = "eleicao", qualifiedByName = "mapEleicaoToBasicInfo")
    @Mapping(target = "candidatos", source = "candidatos", qualifiedByName = "mapCandidatosToBasicInfo")
    @Mapping(target = "totalVotos", expression = "java(cargo.getTotalVotos())")
    @Mapping(target = "totalCandidatos", expression = "java(cargo.getTotalCandidatos())")
    @Mapping(target = "totalVotosValidos", expression = "java(cargo.getTotalVotosValidos())")
    @Mapping(target = "totalVotosBranco", expression = "java(cargo.getTotalVotosBranco())")
    @Mapping(target = "totalVotosNulo", expression = "java(cargo.getTotalVotosNulo())")
    @Mapping(target = "statusVotacao", expression = "java(cargo.getStatusVotacao())")
    @Mapping(target = "percentualParticipacao", expression = "java(cargo.getPercentualParticipacao())")
    @Mapping(target = "resumoVotacao", expression = "java(cargo.getResumoVotacao())")
    @Mapping(target = "temCandidatos", expression = "java(cargo.temCandidatos())")
    @Mapping(target = "podeReceberVotos", expression = "java(cargo.podeReceberVotos())")
    CargoResponse toResponse(Cargo cargo);

    List<CargoResponse> toResponseList(List<Cargo> cargos);

    // === UPDATE MAPPING ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "eleicaoId", ignore = true)
    @Mapping(target = "eleicao", ignore = true)
    @Mapping(target = "candidatos", ignore = true)
    @Mapping(target = "votos", ignore = true)
    void updateEntityFromRequest(UpdateCargoRequest request, @MappingTarget Cargo cargo);

    // === NAMED MAPPINGS ===
    @Named("mapEleicaoToBasicInfo")
    default EleicaoBasicInfo mapEleicaoToBasicInfo(Eleicao eleicao) {
        if (eleicao == null) return null;
        return EleicaoBasicInfo.builder()
                .id(eleicao.getId())
                .nome(eleicao.getNome())
                .ativa(eleicao.getAtiva())
                .dataInicio(eleicao.getDataInicio())
                .dataFim(eleicao.getDataFim())
                .statusDescricao(eleicao.getStatusDescricao())
                .build();
    }

    @Named("mapCandidatosToBasicInfo")
    default List<CandidatoBasicInfo> mapCandidatosToBasicInfo(List<Candidato> candidatos) {
        if (candidatos == null) return null;
        return candidatos.stream()
                .map(candidato -> CandidatoBasicInfo.builder()
                        .id(candidato.getId())
                        .nomeCandidato(candidato.getNomeCandidato())
                        .numeroCandidato(candidato.getNumeroCandidato())
                        .fotoCampanha(candidato.getFotoCampanha())
                        .aprovado(candidato.getAprovado())
                        .totalVotos(candidato.getTotalVotos())
                        .nomeCargoRetendido(candidato.getNomeCargoRetendido())
                        .build())
                .toList();
    }

    // Basic info mapping
    default CargoBasicInfo toBasicInfo(Cargo cargo) {
        if (cargo == null) return null;
        return CargoBasicInfo.builder()
                .id(cargo.getId())
                .nome(cargo.getNome())
                .maxVotos(cargo.getMaxVotos())
                .ordemVotacao(cargo.getOrdemVotacao())
                .obrigatorio(cargo.getObrigatorio())
                .totalCandidatos(cargo.getTotalCandidatos())
                .totalVotos(cargo.getTotalVotos())
                .build();
    }
}