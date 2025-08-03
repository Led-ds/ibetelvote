package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.eleicao.dto.*;
import com.br.ibetelvote.domain.entities.Candidato;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = BasicInfoMapper.class
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

    // === RESPONSE MAPPING ===
    @Mapping(target = "membro", source = "membro")
    @Mapping(target = "eleicao", source = "eleicao")
    @Mapping(target = "cargo", source = "cargo")
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

    // === BASIC INFO RESPONSE ===
    CandidatoBasicInfo toBasicInfo(Candidato candidato);
}