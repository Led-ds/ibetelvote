package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.eleicao.dto.CargoBasicInfo;
import com.br.ibetelvote.application.eleicao.dto.EleicaoBasicInfo;
import com.br.ibetelvote.application.eleicao.dto.MembroBasicInfo;
import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Eleicao;
import com.br.ibetelvote.domain.entities.Membro;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BasicInfoMapper {

    MembroBasicInfo mapMembroToBasicInfo(Membro membro);

    EleicaoBasicInfo mapEleicaoToBasicInfo(Eleicao eleicao);

    CargoBasicInfo mapCargoToBasicInfo(Cargo cargo);
}