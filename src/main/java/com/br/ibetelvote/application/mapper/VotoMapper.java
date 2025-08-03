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
    @Named("toVotoResponse")
    @Mapping(target = "nomeEleicao", expression = "java(voto.getNomeEleicao())")
    @Mapping(target = "nomeCargo", expression = "java(voto.getNomeCargo())")
    @Mapping(target = "tipoVoto", expression = "java(voto.getTipoVoto())")
    @Mapping(target = "dataVotoFormatada", expression = "java(voto.getDataVotoFormatada())")
    VotoResponse toResponse(Voto voto);

    @IterableMapping(qualifiedByName = "toVotoResponse")
    List<VotoResponse> toResponseList(List<Voto> votos);

    // === AUDIT MAPPINGS ===
    @Named("toAuditVotoResponse")
    @Mapping(target = "membroId", ignore = true)
    @Mapping(target = "candidatoId", ignore = true)
    @Mapping(target = "hashVoto", ignore = true)
    VotoResponse toAuditResponse(Voto voto);

    @IterableMapping(qualifiedByName = "toAuditVotoResponse")
    List<VotoResponse> toAuditResponseList(List<Voto> votos);
}

