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
public interface EleicaoMapper {

    // === CREATE MAPPING ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ativa", constant = "false")
    @Mapping(target = "totalVotantes", constant = "0")
    @Mapping(target = "cargos", ignore = true)
    @Mapping(target = "candidatos", ignore = true)
    @Mapping(target = "votos", ignore = true)
    Eleicao toEntity(CreateEleicaoRequest request);

    // === RESPONSE MAPPINGS ===
    @Mapping(target = "cargos", source = "cargos", qualifiedByName = "mapCargosToBasicInfo")
    @Mapping(target = "candidatos", source = "candidatos", qualifiedByName = "mapCandidatosToBasicInfo")
    @Mapping(target = "statusDescricao", expression = "java(eleicao.getStatusDescricao())")
    @Mapping(target = "percentualParticipacao", expression = "java(eleicao.getPercentualParticipacao())")
    @Mapping(target = "totalVotosContabilizados", expression = "java(eleicao.getTotalVotosContabilizados())")
    @Mapping(target = "duracaoEmHoras", expression = "java(eleicao.getDuracaoEmHoras())")
    @Mapping(target = "votacaoAberta", expression = "java(eleicao.isVotacaoAberta())")
    @Mapping(target = "votacaoEncerrada", expression = "java(eleicao.isVotacaoEncerrada())")
    @Mapping(target = "votacaoFutura", expression = "java(eleicao.isVotacaoFutura())")
    @Mapping(target = "temCargos", expression = "java(eleicao.temCargos())")
    @Mapping(target = "temCandidatos", expression = "java(eleicao.temCandidatos())")
    @Mapping(target = "podeSerAtivada", expression = "java(eleicao.podeSerAtivada())")
    EleicaoResponse toResponse(Eleicao eleicao);

    @Mapping(target = "statusDescricao", expression = "java(eleicao.getStatusDescricao())")
    @Mapping(target = "percentualParticipacao", expression = "java(eleicao.getPercentualParticipacao())")
    @Mapping(target = "totalCargos", expression = "java(eleicao.getCargos() != null ? eleicao.getCargos().size() : 0)")
    @Mapping(target = "totalCandidatos", expression = "java(eleicao.getCandidatos() != null ? eleicao.getCandidatos().size() : 0)")
    @Mapping(target = "votacaoAberta", expression = "java(eleicao.isVotacaoAberta())")
    EleicaoListResponse toListResponse(Eleicao eleicao);

    List<EleicaoResponse> toResponseList(List<Eleicao> eleicoes);
    List<EleicaoListResponse> toListResponseList(List<Eleicao> eleicoes);

    // === UPDATE MAPPING ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ativa", ignore = true)
    @Mapping(target = "totalVotantes", ignore = true)
    @Mapping(target = "cargos", ignore = true)
    @Mapping(target = "candidatos", ignore = true)
    @Mapping(target = "votos", ignore = true)
    void updateEntityFromRequest(UpdateEleicaoRequest request, @MappingTarget Eleicao eleicao);

    // === NAMED MAPPINGS ===
    @Named("mapCargosToBasicInfo")
    default List<CargoBasicInfo> mapCargosToBasicInfo(List<Cargo> cargos) {
        if (cargos == null) return null;
        return cargos.stream()
                .map(cargo -> CargoBasicInfo.builder()
                        .id(cargo.getId())
                        .nome(cargo.getNome())
                        .maxVotos(cargo.getMaxVotos())
                        .ordemVotacao(cargo.getOrdemVotacao())
                        .obrigatorio(cargo.getObrigatorio())
                        .totalCandidatos(cargo.getTotalCandidatos())
                        .totalVotos(cargo.getTotalVotos())
                        .build())
                .toList();
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
    default EleicaoBasicInfo toBasicInfo(Eleicao eleicao) {
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
}