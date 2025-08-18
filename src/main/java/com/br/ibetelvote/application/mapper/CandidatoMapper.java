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
    @Mapping(target = "fotoCampanhaData", ignore = true)
    @Mapping(target = "fotoCampanhaTipo", ignore = true)
    @Mapping(target = "fotoCampanhaNome", ignore = true)
    @Mapping(target = "motivoReprovacao", ignore = true)
    @Mapping(target = "dataAprovacao", ignore = true)
    @Mapping(target = "membro", ignore = true)
    @Mapping(target = "eleicao", ignore = true)
    @Mapping(target = "cargo", ignore = true)
    @Mapping(target = "votos", ignore = true)
    @Mapping(target = "numeroCandidato", ignore = true)
    Candidato toEntity(CreateCandidatoRequest request);

    // === RESPONSE MAPPING ===
    @Mapping(target = "membro", source = "membro")
    @Mapping(target = "eleicao", source = "eleicao")
    @Mapping(target = "cargo", source = "cargo")
    @Mapping(target = "totalVotos", expression = "java(candidato.getTotalVotos())")
    @Mapping(target = "statusCandidatura", expression = "java(candidato.getStatusCandidatura())")
    @Mapping(target = "numeroFormatado", expression = "java(candidato.getNumeroFormatado())")
    @Mapping(target = "percentualVotos", expression = "java(candidato.getPercentualVotos())")
    @Mapping(target = "resumoVotacao", expression = "java(candidato.getResumoVotacao())")
    @Mapping(target = "temFotoCampanha", expression = "java(candidato.temFotoCampanha())")
    @Mapping(target = "temNumero", expression = "java(candidato.temNumero())")
    @Mapping(target = "candidaturaCompleta", expression = "java(candidato.isCandidaturaCompleta())")
    @Mapping(target = "podeReceberVotos", expression = "java(candidato.podeReceberVotos())")
    // === CAMPOS DE FOTO ===
    @Mapping(target = "fotoCampanhaTipo", source = "fotoCampanhaTipo")
    @Mapping(target = "fotoCampanhaNome", source = "fotoCampanhaNome")
    @Mapping(target = "fotoCampanhaSize", expression = "java(candidato.getFotoCampanhaSize())")
    @Mapping(target = "fotoCampanhaUrl", expression = "java(buildFotoCampanhaUrl(candidato))")
    @Mapping(target = "fotoCampanhaBase64", ignore = true) // Não incluir por padrão para performance
    CandidatoResponse toResponse(Candidato candidato);

    // === RESPONSE MAPPING COM BASE64 (para quando precisar da imagem) ===
    @Mapping(target = "membro", source = "membro")
    @Mapping(target = "eleicao", source = "eleicao")
    @Mapping(target = "cargo", source = "cargo")
    @Mapping(target = "totalVotos", expression = "java(candidato.getTotalVotos())")
    @Mapping(target = "statusCandidatura", expression = "java(candidato.getStatusCandidatura())")
    @Mapping(target = "numeroFormatado", expression = "java(candidato.getNumeroFormatado())")
    @Mapping(target = "percentualVotos", expression = "java(candidato.getPercentualVotos())")
    @Mapping(target = "resumoVotacao", expression = "java(candidato.getResumoVotacao())")
    @Mapping(target = "temFotoCampanha", expression = "java(candidato.temFotoCampanha())")
    @Mapping(target = "temNumero", expression = "java(candidato.temNumero())")
    @Mapping(target = "candidaturaCompleta", expression = "java(candidato.isCandidaturaCompleta())")
    @Mapping(target = "podeReceberVotos", expression = "java(candidato.podeReceberVotos())")
    // === CAMPOS DE FOTO COM BASE64 ===
    @Mapping(target = "fotoCampanhaTipo", source = "fotoCampanhaTipo")
    @Mapping(target = "fotoCampanhaNome", source = "fotoCampanhaNome")
    @Mapping(target = "fotoCampanhaSize", expression = "java(candidato.getFotoCampanhaSize())")
    @Mapping(target = "fotoCampanhaUrl", expression = "java(buildFotoCampanhaUrl(candidato))")
    @Mapping(target = "fotoCampanhaBase64", expression = "java(candidato.getFotoCampanhaDataUri())")
    @Named("withPhoto")
    CandidatoResponse toResponseWithPhoto(Candidato candidato);

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
    @Mapping(target = "fotoCampanhaData", ignore = true)
    @Mapping(target = "fotoCampanhaTipo", ignore = true)
    @Mapping(target = "fotoCampanhaNome", ignore = true)
    @Mapping(target = "numeroCandidato", ignore = true)
    @Mapping(target = "membro", ignore = true)
    @Mapping(target = "eleicao", ignore = true)
    @Mapping(target = "cargo", ignore = true)
    @Mapping(target = "votos", ignore = true)
    void updateEntityFromRequest(UpdateCandidatoRequest request, @MappingTarget Candidato candidato);

    // === BASIC INFO RESPONSE ===
    @Mapping(target = "totalVotos", expression = "java(candidato.getTotalVotos())")
    @Mapping(target = "nomeCargoRetendido", source = "nomeCargoRetendido")
    @Mapping(target = "temFotoCampanha", expression = "java(candidato.temFotoCampanha())")
    CandidatoBasicInfo toBasicInfo(Candidato candidato);

    // === HELPER METHODS ===
    default String buildFotoCampanhaUrl(Candidato candidato) {
        if (candidato == null) return null;

        if (candidato.temFotoCampanha()) {
            return "/api/v1/candidatos/" + candidato.getId() + "/foto-campanha";
        }

        return null; // Não tem foto
    }
}