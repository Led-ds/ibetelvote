package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.auth.dto.MembroBasicInfo;
import com.br.ibetelvote.application.candidato.dto.*;
import com.br.ibetelvote.application.cargo.dto.CargoBasicInfo;
import com.br.ibetelvote.application.eleicao.dto.EleicaoBasicInfo;
import com.br.ibetelvote.domain.entities.Candidato;
import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Eleicao;
import com.br.ibetelvote.domain.entities.Membro;
import org.mapstruct.*;

import java.util.List;
import java.util.ArrayList;

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
    @Mapping(target = "fotoCampanhaData", ignore = true)
    @Mapping(target = "fotoCampanhaTipo", ignore = true)
    @Mapping(target = "fotoCampanhaNome", ignore = true)
    @Mapping(target = "motivoReprovacao", ignore = true)
    @Mapping(target = "dataAprovacao", ignore = true)
    @Mapping(target = "numeroCandidato", ignore = true)
    @Mapping(target = "membro", ignore = true)
    @Mapping(target = "eleicao", ignore = true)
    @Mapping(target = "cargoPretendido", ignore = true)
    @Mapping(target = "votos", ignore = true)
    Candidato toEntity(CreateCandidatoRequest request);

    // === CANDIDATO RESPONSE ===
    default CandidatoResponse toResponse(Candidato candidato) {
        if (candidato == null) {
            return null;
        }

        return CandidatoResponse.builder()
                .id(candidato.getId())
                .membroId(candidato.getMembroId())
                .eleicaoId(candidato.getEleicaoId())
                .cargoPretendidoId(candidato.getCargoPretendidoId())
                .numeroCandidato(candidato.getNumeroCandidato())
                .nomeCandidato(candidato.getNomeCandidato())
                .descricaoCandidatura(candidato.getDescricaoCandidatura())
                .propostas(candidato.getPropostas())
                .experiencia(candidato.getExperiencia())
                .ativo(candidato.isAtivo())
                .aprovado(candidato.isAprovado())
                .motivoReprovacao(candidato.getMotivoReprovacao())
                .dataAprovacao(candidato.getDataAprovacao())
                .createdAt(candidato.getCreatedAt())
                .updatedAt(candidato.getUpdatedAt())
                // Relacionamentos
                .membro(mapMembroToBasicInfo(candidato.getMembro()))
                .eleicao(mapEleicaoToBasicInfo(candidato.getEleicao()))
                .cargoPretendido(mapCargoToBasicInfo(candidato.getCargoPretendido()))
                // Foto
                .temFotoCampanha(candidato.temFotoCampanha())
                .fotoSize(candidato.getFotoCampanhaSize())
                // Campos computados
                .displayName(candidato.getDisplayName())
                .nomeCargoPretendido(candidato.getNomeCargoPretendido())
                .nomeMembro(candidato.getNomeMembro())
                .nomeEleicao(candidato.getNomeEleicao())
                .cargoAtualMembro(candidato.getCargoAtualMembro())
                .emailMembro(candidato.getEmailMembro())
                .totalVotos(candidato.getTotalVotos())
                .percentualVotos(candidato.getPercentualVotos())
                .resumoVotacao(candidato.getResumoVotacao())
                .statusCandidatura(candidato.getStatusCandidatura())
                .numeroFormatado(candidato.getNumeroFormatado())
                .candidaturaCompleta(candidato.isCandidaturaCompleta())
                .elegivel(candidato.isElegivel())
                .podeReceberVotos(candidato.podeReceberVotos())
                .membroAtivo(candidato.isMembroAtivo())
                .membroPodeSeCandidarParaCargo(candidato.membroPodeSeCandidarParaCargo())
                .build();
    }

    // === CANDIDATO RESPONSE WITH PHOTO ===
    @Named("withPhoto")
    default CandidatoResponse toResponseWithPhoto(Candidato candidato) {
        if (candidato == null) {
            return null;
        }

        CandidatoResponse response = toResponse(candidato);

        // Adicionar foto Base64 se existir
        if (candidato.temFotoCampanha()) {
            response.setFotoBase64(candidato.getFotoCampanhaDataUri());
        }

        return response;
    }

    // === CANDIDATO BASIC INFO ===
    default CandidatoBasicInfo toBasicInfo(Candidato candidato) {
        if (candidato == null) {
            return null;
        }

        return CandidatoBasicInfo.builder()
                .id(candidato.getId())
                .nomeCandidato(candidato.getNomeCandidato())
                .numeroCandidato(candidato.getNumeroCandidato())
                .cargoPretendidoId(candidato.getCargoPretendidoId())
                .nomeCargoPretendido(candidato.getNomeCargoPretendido())
                .ativo(candidato.isAtivo())
                .aprovado(candidato.isAprovado())
                .temFotoCampanha(candidato.temFotoCampanha())
                .totalVotos(candidato.getTotalVotos())
                .statusCandidatura(candidato.getStatusCandidatura())
                .build();
    }

    // === CANDIDATO LIST RESPONSE ===
    default CandidatoListResponse toListResponse(Candidato candidato) {
        if (candidato == null) {
            return null;
        }

        return CandidatoListResponse.builder()
                .id(candidato.getId())
                .nomeCandidato(candidato.getNomeCandidato())
                .numeroCandidato(candidato.getNumeroCandidato())
                .membroId(candidato.getMembroId())
                .nomeMembro(candidato.getNomeMembro())
                .cargoPretendidoId(candidato.getCargoPretendidoId())
                .nomeCargoPretendido(candidato.getNomeCargoPretendido())
                .cargoAtualMembro(candidato.getCargoAtualMembro())
                .ativo(candidato.isAtivo())
                .aprovado(candidato.isAprovado())
                .temFotoCampanha(candidato.temFotoCampanha())
                .totalVotos(candidato.getTotalVotos())
                .percentualVotos(candidato.getPercentualVotos())
                .statusCandidatura(candidato.getStatusCandidatura())
                .createdAt(candidato.getCreatedAt())
                .build();
    }

    // === CANDIDATO RANKING RESPONSE ===
    default CandidatoRankingResponse toRankingResponse(Candidato candidato) {
        if (candidato == null) {
            return null;
        }

        return CandidatoRankingResponse.builder()
                .candidatoId(candidato.getId())
                .nomeCandidato(candidato.getNomeCandidato())
                .numeroCandidato(candidato.getNumeroCandidato())
                .nomeCargoPretendido(candidato.getNomeCargoPretendido())
                .totalVotos(candidato.getTotalVotos())
                .percentualVotos(candidato.getPercentualVotos())
                .temFotoCampanha(candidato.temFotoCampanha())
                .build();
    }

    // === CANDIDATO ELEGIBILIDADE RESPONSE ===
    default CandidatoElegibilidadeResponse toElegibilidadeResponse(Candidato candidato) {
        if (candidato == null) {
            return null;
        }

        return CandidatoElegibilidadeResponse.builder()
                .membroId(candidato.getMembroId())
                .nomeMembro(candidato.getNomeMembro())
                .cargoAtualMembro(candidato.getCargoAtualMembro())
                .cargoPretendidoId(candidato.getCargoPretendidoId())
                .nomeCargoPretendido(candidato.getNomeCargoPretendido())
                .elegivel(candidato.isElegivel())
                .membroAtivo(candidato.isMembroAtivo())
                .cargoAtivo(candidato.getCargoPretendido() != null && candidato.getCargoPretendido().isAtivo())
                .membroPodeSeCandidarParaCargo(candidato.membroPodeSeCandidarParaCargo())
                .motivosInelegibilidade(buildMotivosInelegibilidade(candidato))
                .resumoElegibilidade(buildResumoElegibilidade(candidato))
                .build();
    }

    // === UPDATE MAPPING ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "membroId", ignore = true)
    @Mapping(target = "eleicaoId", ignore = true)
    @Mapping(target = "cargoPretendidoId", ignore = true)
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
    @Mapping(target = "cargoPretendido", ignore = true)
    @Mapping(target = "votos", ignore = true)
    void updateEntityFromRequest(UpdateCandidatoRequest request, @MappingTarget Candidato candidato);

    // === LISTAS ===
    default List<CandidatoResponse> toResponseList(List<Candidato> candidatos) {
        if (candidatos == null) {
            return null;
        }
        List<CandidatoResponse> list = new ArrayList<>(candidatos.size());
        for (Candidato candidato : candidatos) {
            list.add(toResponse(candidato));
        }
        return list;
    }

    default List<CandidatoListResponse> toListResponseList(List<Candidato> candidatos) {
        if (candidatos == null) {
            return null;
        }
        List<CandidatoListResponse> list = new ArrayList<>(candidatos.size());
        for (Candidato candidato : candidatos) {
            list.add(toListResponse(candidato));
        }
        return list;
    }

    default List<CandidatoRankingResponse> toRankingResponseList(List<Candidato> candidatos) {
        if (candidatos == null) {
            return null;
        }
        List<CandidatoRankingResponse> list = new ArrayList<>(candidatos.size());
        for (int i = 0; i < candidatos.size(); i++) {
            CandidatoRankingResponse ranking = toRankingResponse(candidatos.get(i));
            ranking.setPosicao(i + 1); // Definir posição no ranking
            list.add(ranking);
        }
        return list;
    }

    // === MAPEAMENTOS DE RELACIONAMENTOS ===
    default MembroBasicInfo mapMembroToBasicInfo(Membro membro) {
        if (membro == null) {
            return null;
        }

        return MembroBasicInfo.builder()
                .id(membro.getId())
                .nome(membro.getNome())
                .fotoBase64(membro.getFotoBase64()) // Se o método existir
                .cargo(membro.getNomeCargoAtual())
                .ativo(membro.isActive())
                .build();
    }

    default CargoBasicInfo mapCargoToBasicInfo(Cargo cargo) {
        if (cargo == null) {
            return null;
        }

        return CargoBasicInfo.builder()
                .id(cargo.getId())
                .nome(cargo.getNome())
                .ativo(cargo.isAtivo())
                .build();
    }

    default EleicaoBasicInfo mapEleicaoToBasicInfo(Eleicao eleicao) {
        if (eleicao == null) {
            return null;
        }

        return EleicaoBasicInfo.builder()
                .id(eleicao.getId())
                .nome(eleicao.getNome())
                .ativa(eleicao.isAtiva())
                .dataInicio(eleicao.getDataInicio())
                .dataFim(eleicao.getDataFim())
                .build();
    }

    // === HELPER METHODS ===
    default String buildFotoCampanhaUrl(Candidato candidato) {
        if (candidato == null || !candidato.temFotoCampanha()) {
            return null;
        }
        return "/api/v1/candidatos/" + candidato.getId() + "/foto-campanha";
    }

    default List<String> buildMotivosInelegibilidade(Candidato candidato) {
        List<String> motivos = new ArrayList<>();

        if (candidato == null) {
            motivos.add("Candidato não encontrado");
            return motivos;
        }

        if (!candidato.isAtivo()) {
            motivos.add("Candidatura está inativa");
        }

        if (candidato.getMembro() == null) {
            motivos.add("Membro não encontrado");
        } else if (!candidato.getMembro().isActive()) {
            motivos.add("Membro está inativo");
        }

        if (candidato.getCargoPretendido() == null) {
            motivos.add("Cargo pretendido não encontrado");
        } else if (!candidato.getCargoPretendido().isAtivo()) {
            motivos.add("Cargo pretendido está inativo");
        }

        if (!candidato.membroPodeSeCandidarParaCargo()) {
            motivos.add("Membro não atende aos requisitos hierárquicos para o cargo pretendido");
        }

        if (!candidato.isCandidaturaCompleta()) {
            motivos.add("Candidatura incompleta - faltam dados obrigatórios");
        }

        return motivos;
    }

    default String buildResumoElegibilidade(Candidato candidato) {
        if (candidato == null) {
            return "Candidato não encontrado";
        }

        if (candidato.isElegivel()) {
            return "Candidato está elegível para participar da eleição";
        }

        List<String> motivos = buildMotivosInelegibilidade(candidato);
        return "Candidato não está elegível. Motivos: " + String.join(", ", motivos);
    }

    // === VALIDAÇÕES PRE-MAPPING ===
    @BeforeMapping
    default void normalizarNomeCandidato(CreateCandidatoRequest request) {
        if (request != null && request.getNomeCandidato() != null) {
            request.setNomeCandidato(request.getNomeCandidato().trim());
        }
    }

    @BeforeMapping
    default void normalizarDescricoes(CreateCandidatoRequest request) {
        if (request == null) return;

        if (request.getDescricaoCandidatura() != null) {
            request.setDescricaoCandidatura(request.getDescricaoCandidatura().trim());
        }
        if (request.getPropostas() != null) {
            request.setPropostas(request.getPropostas().trim());
        }
        if (request.getExperiencia() != null) {
            request.setExperiencia(request.getExperiencia().trim());
        }
    }

    @BeforeMapping
    default void normalizarNomeCandidatoUpdate(UpdateCandidatoRequest request) {
        if (request != null && request.getNomeCandidato() != null) {
            request.setNomeCandidato(request.getNomeCandidato().trim());
        }
    }

    // === VALIDAÇÕES PÓS-MAPPING ===
    @AfterMapping
    default void validarDadosObrigatorios(@MappingTarget Candidato candidato) {
        if (candidato == null) return;

        try {
            candidato.validarDadosObrigatorios();
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("Dados inválidos para candidatura: " + e.getMessage());
        }
    }
}