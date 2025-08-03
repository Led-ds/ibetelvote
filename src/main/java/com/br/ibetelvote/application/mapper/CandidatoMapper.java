package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.eleicao.dto.*;
import com.br.ibetelvote.domain.entities.Candidato;
import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Eleicao;
import com.br.ibetelvote.domain.entities.Membro;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CandidatoMapper {

    // === CREATE MAPPING ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ativo", constant = "true")
    @Mapping(target = "aprovado", constant = "false")
    @Mapping(target = "fotoCampanha", ignore = true)
    @Mapping(target = "motivoReprovacao", ignore = true)
    @Mapping(target = "dataAprovacao", ignore = true)
    @Mapping(target = "membro", ignore = true)
    @Mapping(target = "eleicao", ignore = true)
    @Mapping(target = "cargo", ignore = true)
    @Mapping(target = "votos", ignore = true)
    Candidato toEntity(CreateCandidatoRequest request);

    // === RESPONSE MAPPINGS ===
    @Mapping(target = "membro", source = "membro", qualifiedByName = "mapMembroToBasicInfo")
    @Mapping(target = "eleicao", source = "eleicao", qualifiedByName = "mapEleicaoToBasicInfo")
    @Mapping(target = "cargo", source = "cargo", qualifiedByName = "mapCargoToBasicInfo")
    @Mapping(target = "totalVotos", expression = "java(candidato.getTotalVotos())")
    @Mapping(target = "statusCandidatura", expression = "java(candidato.getStatusCandidatura())")
    @Mapping(target = "numeroFormatado", expression = "java(candidato.getNumeroFormatado())")
    @Mapping(target = "fotoCampanhaUrl", expression = "java(candidato.getFotoCampanhaUrl())")
    @Mapping(target = "percentualVotos", expression = "java(candidato.getPercentualVotos())")
    @Mapping(target = "resumoVotacao", expression = "java(candidato.getResumoVotacao())")
    @Mapping(target = "temFotoCampanha", expression = "java(candidato.temFotoCampanha())")
    @Mapping(target = "temNumero", expression = "java(candidato.temNumero())")
    @Mapping(target = "candidaturaCompleta", expression = "java(candidato.isCandidaturaCompleta())")
    @Mapping(target = "podeReceberVotos", expression = "java(candidato.podeReceberVotos())")
    CandidatoResponse toResponse(Candidato candidato);

    List<CandidatoResponse> toResponseList(List<Candidato> candidatos);

    // === UPDATE MAPPING ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "membroId", ignore = true)
    @Mapping(target = "eleicaoId", ignore = true)
    @Mapping(target = "cargoId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "aprovado", ignore = true)
    @Mapping(target = "motivoReprovacao", ignore = true)
    @Mapping(target = "dataAprovacao", ignore = true)
    @Mapping(target = "fotoCampanha", ignore = true)
    @Mapping(target = "membro", ignore = true)
    @Mapping(target = "eleicao", ignore = true)
    @Mapping(target = "cargo", ignore = true)
    @Mapping(target = "votos", ignore = true)
    void updateEntityFromRequest(UpdateCandidatoRequest request, @MappingTarget Candidato candidato);

    // === NAMED MAPPINGS ===
    @Named("mapMembroToBasicInfo")
    default MembroBasicInfo mapMembroToBasicInfo(Membro membro) {
        if (membro == null) return null;
        return MembroBasicInfo.builder()
                .id(membro.getId())
                .nome(membro.getNome())
                .email(membro.getEmail())
                .cargo(membro.getCargo())
                .foto(membro.getFoto())
                .ativo(membro.getAtivo())
                .build();
    }

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

    @Named("mapCargoToBasicInfo")
    default CargoBasicInfo mapCargoToBasicInfo(Cargo cargo) {
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

    // Basic info mapping
    default CandidatoBasicInfo toBasicInfo(Candidato candidato) {
        if (candidato == null) return null;
        return CandidatoBasicInfo.builder()
                .id(candidato.getId())
                .nomeCandidato(candidato.getNomeCandidato())
                .numeroCandidato(candidato.getNumeroCandidato())
                .fotoCampanha(candidato.getFotoCampanha())
                .aprovado(candidato.getAprovado())
                .totalVotos(candidato.getTotalVotos())
                .nomeCargoRetendido(candidato.getNomeCargoRetendido())
                .build();
    }
}