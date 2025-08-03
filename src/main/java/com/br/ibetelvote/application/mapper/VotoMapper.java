package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.eleicao.dto.VotoResponse;
import com.br.ibetelvote.domain.entities.Voto;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface VotoMapper {

    // === RESPONSE MAPPINGS ===
    @Mapping(target = "nomeEleicao", expression = "java(voto.getNomeEleicao())")
    @Mapping(target = "nomeCargo", expression = "java(voto.getNomeCargo())")
    @Mapping(target = "tipoVoto", expression = "java(voto.getTipoVoto())")
    @Mapping(target = "dataVotoFormatada", expression = "java(voto.getDataVotoFormatada())")
    VotoResponse toResponse(Voto voto);

    List<VotoResponse> toResponseList(List<Voto> votos);

    // === UTILITY MAPPINGS ===

    // Mapping para auditoria (sem dados sensíveis)
    @Mapping(target = "membroId", ignore = true) // Proteger identidade do votante
    @Mapping(target = "candidatoId", ignore = true) // Proteger escolha específica
    @Mapping(target = "hashVoto", ignore = true) // Proteger hash
    VotoResponse toAuditResponse(Voto voto);
}