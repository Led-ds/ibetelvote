package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.candidato.dto.CandidatoBasicInfo;
import com.br.ibetelvote.application.cargo.dto.CargoBasicInfo;
import com.br.ibetelvote.application.eleicao.dto.*;
import com.br.ibetelvote.domain.entities.Candidato;
import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Eleicao;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @Mapping(target = "candidatos", ignore = true)
    @Mapping(target = "votos", ignore = true)
    @BeforeMapping
    default void validateAndNormalizeCreate(CreateEleicaoRequest request) {
        if (request.getNome() != null) {
            request.setNome(request.getNome().trim().replaceAll("\\s+", " "));
        }
    }
    Eleicao toEntity(CreateEleicaoRequest request);

    // === RESPONSE MAPPINGS ===
    @Mapping(target = "candidatos", source = "candidatos", qualifiedByName = "mapCandidatosToBasicInfo")
    @Mapping(target = "cargosComCandidatos", source = "eleicao", qualifiedByName = "mapCargosComCandidatos")
    @Mapping(target = "statusDescricao", expression = "java(eleicao.getStatusDescricao())")
    @Mapping(target = "percentualParticipacao", expression = "java(eleicao.getPercentualParticipacao())")
    @Mapping(target = "totalVotosContabilizados", expression = "java(eleicao.getTotalVotosContabilizados())")
    @Mapping(target = "duracaoEmHoras", expression = "java(eleicao.getDuracaoEmHoras())")
    @Mapping(target = "votacaoAberta", expression = "java(eleicao.isVotacaoAberta())")
    @Mapping(target = "votacaoEncerrada", expression = "java(eleicao.isVotacaoEncerrada())")
    @Mapping(target = "votacaoFutura", expression = "java(eleicao.isVotacaoFutura())")
    @Mapping(target = "temCandidatos", expression = "java(eleicao.temCandidatos())")
    @Mapping(target = "temCandidatosAprovados", expression = "java(eleicao.temCandidatosAprovados())")
    @Mapping(target = "totalCandidatosAprovados", expression = "java(eleicao.getTotalCandidatosAprovados())")
    @Mapping(target = "totalCargosComCandidatos", expression = "java(eleicao.getTotalCargosComCandidatos())")
    @Mapping(target = "podeSerAtivada", expression = "java(eleicao.podeSerAtivada())")
    EleicaoResponse toResponse(Eleicao eleicao);

    @Mapping(target = "statusDescricao", expression = "java(eleicao.getStatusDescricao())")
    @Mapping(target = "percentualParticipacao", expression = "java(eleicao.getPercentualParticipacao())")
    @Mapping(target = "totalCandidatos", expression = "java(eleicao.getCandidatos() != null ? eleicao.getCandidatos().size() : 0)")
    @Mapping(target = "totalCandidatosAprovados", expression = "java(eleicao.getTotalCandidatosAprovados())")
    @Mapping(target = "totalCargosComCandidatos", expression = "java(eleicao.getTotalCargosComCandidatos())")
    @Mapping(target = "votacaoAberta", expression = "java(eleicao.isVotacaoAberta())")
    @Mapping(target = "temCandidatosAprovados", expression = "java(eleicao.temCandidatosAprovados())")
    EleicaoListResponse toListResponse(Eleicao eleicao);

    List<EleicaoResponse> toResponseList(List<Eleicao> eleicoes);
    List<EleicaoListResponse> toListResponseList(List<Eleicao> eleicoes);

    // === UPDATE MAPPING ===
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ativa", ignore = true)
    @Mapping(target = "totalVotantes", ignore = true)
    @Mapping(target = "candidatos", ignore = true)
    @Mapping(target = "votos", ignore = true)
    @BeforeMapping
    default void validateAndNormalizeUpdate(UpdateEleicaoRequest request) {
        if (request.getNome() != null) {
            request.setNome(request.getNome().trim().replaceAll("\\s+", " "));
        }
    }
    @AfterMapping
    default void validateUpdatedEntity(@MappingTarget Eleicao eleicao) {
        eleicao.validarDados();
    }
    void updateEntityFromRequest(UpdateEleicaoRequest request, @MappingTarget Eleicao eleicao);

    // === BASIC INFO MAPPING ===
    @Mapping(target = "statusDescricao", expression = "java(eleicao.getStatusDescricao())")
    @Mapping(target = "votacaoAberta", expression = "java(eleicao.isVotacaoAberta())")
    @Mapping(target = "temCandidatosAprovados", expression = "java(eleicao.temCandidatosAprovados())")
    EleicaoBasicInfo toBasicInfo(Eleicao eleicao);

    // === STATS MAPPING ===
    @Mapping(target = "percentualParticipacao", expression = "java(eleicao.getPercentualParticipacao())")
    @Mapping(target = "totalCandidatos", expression = "java(eleicao.getCandidatos() != null ? eleicao.getCandidatos().size() : 0)")
    @Mapping(target = "totalCandidatosAprovados", expression = "java(eleicao.getTotalCandidatosAprovados())")
    @Mapping(target = "totalCandidatosPendentes", source = "eleicao", qualifiedByName = "countCandidatosPendentes")
    @Mapping(target = "totalCandidatosReprovados", source = "eleicao", qualifiedByName = "countCandidatosReprovados")
    @Mapping(target = "totalCargosComCandidatos", expression = "java(eleicao.getTotalCargosComCandidatos())")
    @Mapping(target = "totalVotosContabilizados", expression = "java(eleicao.getTotalVotosContabilizados())")
    @Mapping(target = "totalVotosBrancos", source = "eleicao", qualifiedByName = "countVotosBrancos")
    @Mapping(target = "totalVotosNulos", source = "eleicao", qualifiedByName = "countVotosNulos")
    @Mapping(target = "statusDescricao", expression = "java(eleicao.getStatusDescricao())")
    @Mapping(target = "votacaoAberta", expression = "java(eleicao.isVotacaoAberta())")
    @Mapping(target = "duracaoEmHoras", expression = "java(eleicao.getDuracaoEmHoras())")
    EleicaoStatsResponse toStatsResponse(Eleicao eleicao);

    // === VALIDAÇÃO MAPPING ===
    default EleicaoValidacaoResponse toValidacaoResponse(Eleicao eleicao) {
        if (eleicao == null) return null;

        boolean podeSerAtivada = eleicao.podeSerAtivada();
        List<String> motivosImpedimento = getMotivosImpedimento(eleicao);
        List<Cargo> cargosComCandidatos = eleicao.getCargosComCandidatos();

        return EleicaoValidacaoResponse.builder()
                .podeSerAtivada(podeSerAtivada)
                .motivosImpedimento(motivosImpedimento)
                .temCandidatosAprovados(eleicao.temCandidatosAprovados())
                .totalCandidatosAprovados(eleicao.getTotalCandidatosAprovados())
                .totalCargosComCandidatos(eleicao.getTotalCargosComCandidatos())
                .cargosComCandidatos(cargosComCandidatos.stream()
                        .map(Cargo::getNome)
                        .collect(Collectors.toList()))
                .cargosSemCandidatos(getCargosSemCandidatos(eleicao))
                .datasValidas(validarDatas(eleicao))
                .periodoValido(validarPeriodo(eleicao))
                .totalElegiveisDefinido(eleicao.getTotalElegiveis() != null && eleicao.getTotalElegiveis() > 0)
                .resumoValidacao(gerarResumoValidacao(eleicao, podeSerAtivada))
                .build();
    }

    @Named("mapCargosComCandidatos")
    default List<CargoBasicInfo> mapCargosComCandidatos(Eleicao eleicao) {
        if (eleicao == null) return List.of();

        return eleicao.getCargosComCandidatos().stream()
                .map(cargo -> CargoBasicInfo.builder()
                        .id(cargo.getId())
                        .nome(cargo.getNome())
                        .ativo(cargo.getAtivo())
                        .build())
                .collect(Collectors.toList());
    }

    @Named("mapCandidatosToBasicInfo")
    default List<CandidatoBasicInfo> mapCandidatosToBasicInfo(List<Candidato> candidatos) {
        if (candidatos == null) return List.of();

        return candidatos.stream()
                .map(candidato -> CandidatoBasicInfo.builder()
                        .id(candidato.getId())
                        .nomeCandidato(candidato.getNomeCandidato())
                        .numeroCandidato(candidato.getNumeroCandidato())
                        .temFotoCampanha(candidato.temFotoCampanha())
                        .aprovado(candidato.getAprovado())
                        .totalVotos(candidato.getTotalVotos())
                        .nomeCargoPretendido(candidato.getCargoPretendido() != null ?
                                candidato.getCargoPretendido().getNome() : null)
                        .build())
                .collect(Collectors.toList());
    }

    // === MÉTODOS AUXILIARES ===

    @Named("countCandidatosPendentes")
    default int countCandidatosPendentes(Eleicao eleicao) {
        if (eleicao.getCandidatos() == null) return 0;
        return (int) eleicao.getCandidatos().stream()
                .filter(c -> c.getAprovado() == null)
                .count();
    }

    @Named("countCandidatosReprovados")
    default int countCandidatosReprovados(Eleicao eleicao) {
        if (eleicao.getCandidatos() == null) return 0;
        return (int) eleicao.getCandidatos().stream()
                .filter(c -> c.getAprovado() != null && !c.getAprovado())
                .count();
    }

    @Named("countVotosBrancos")
    default int countVotosBrancos(Eleicao eleicao) {
        if (eleicao.getVotos() == null) return 0;
        return (int) eleicao.getVotos().stream()
                .filter(v -> v.getTipoVoto() != null && "BRANCO".equals(v.getTipoVoto()))
                .count();
    }

    @Named("countVotosNulos")
    default int countVotosNulos(Eleicao eleicao) {
        if (eleicao.getVotos() == null) return 0;
        return (int) eleicao.getVotos().stream()
                .filter(v -> v.getTipoVoto() != null && "NULO".equals(v.getTipoVoto()))
                .count();
    }

    // === MÉTODOS PRIVADOS DE VALIDAÇÃO ===

    default List<String> getMotivosImpedimento(Eleicao eleicao) {
        List<String> motivos = new java.util.ArrayList<>();

        if (!eleicao.temCandidatosAprovados()) {
            motivos.add("Não há candidatos aprovados");
        }

        if (eleicao.getDataInicio() == null || eleicao.getDataFim() == null) {
            motivos.add("Datas de início e fim são obrigatórias");
        } else if (eleicao.getDataInicio().isAfter(eleicao.getDataFim())) {
            motivos.add("Data de início deve ser anterior à data de fim");
        }

        if (eleicao.getTotalElegiveis() == null || eleicao.getTotalElegiveis() <= 0) {
            motivos.add("Total de elegíveis deve ser informado e maior que zero");
        }

        if (eleicao.getCargosComCandidatos().isEmpty()) {
            motivos.add("Deve haver pelo menos um cargo com candidatos aprovados");
        }

        return motivos;
    }

    default List<String> getCargosSemCandidatos(Eleicao eleicao) {
        // Esta implementação dependeria de um serviço para buscar todos os cargos ativos
        // e comparar com os que têm candidatos. Para simplicidade, retornamos lista vazia.
        return List.of();
    }

    default boolean validarDatas(Eleicao eleicao) {
        return eleicao.getDataInicio() != null &&
                eleicao.getDataFim() != null &&
                eleicao.getDataInicio().isBefore(eleicao.getDataFim());
    }

    default boolean validarPeriodo(Eleicao eleicao) {
        if (!validarDatas(eleicao)) return false;

        // Validar duração mínima de 1 hora
        return eleicao.getDataInicio().plusHours(1).isBefore(eleicao.getDataFim()) ||
                eleicao.getDataInicio().plusHours(1).equals(eleicao.getDataFim());
    }

    default String gerarResumoValidacao(Eleicao eleicao, boolean podeSerAtivada) {
        if (podeSerAtivada) {
            return String.format("Eleição '%s' está pronta para ser ativada com %d candidatos aprovados em %d cargos.",
                    eleicao.getNome(),
                    eleicao.getTotalCandidatosAprovados(),
                    eleicao.getTotalCargosComCandidatos());
        } else {
            return String.format("Eleição '%s' não pode ser ativada. Verifique os motivos de impedimento.",
                    eleicao.getNome());
        }
    }

    default VagasEleicaoResponse toVagasResponse(Eleicao eleicao) {
        if (eleicao == null) return null;

        List<VagasEleicaoResponse.VagaCargoInfo> vagasInfo = eleicao.getCargosComCandidatos()
                .stream()
                .map(cargo -> VagasEleicaoResponse.VagaCargoInfo.builder()
                        .cargoId(cargo.getId())
                        .nomeCargo(cargo.getNome())
                        .numeroVagas(eleicao.getLimiteVotosPorCargo(cargo.getId()))
                        .temCandidatos(eleicao.temCandidatosParaCargo(cargo.getId()))
                        .totalCandidatos(eleicao.getCandidatosPorCargo(cargo.getId()).size())
                        .build())
                .collect(Collectors.toList());

        return VagasEleicaoResponse.builder()
                .eleicaoId(eleicao.getId())
                .nomeEleicao(eleicao.getNome())
                .vagasConfiguradas(vagasInfo)
                .totalVagasConfiguradas(vagasInfo.stream()
                        .mapToInt(VagasEleicaoResponse.VagaCargoInfo::getNumeroVagas)
                        .sum())
                .build();
    }

    default LimiteVotacaoResponse toLimiteVotacaoResponse(Eleicao.LimiteVotacaoInfo info, String nomeCargo) {
        if (info == null) return null;

        return LimiteVotacaoResponse.builder()
                .cargoId(info.cargoId)
                .nomeCargo(nomeCargo)
                .limiteVotos(info.limiteVotos)
                .votosJaDados(info.votosJaDados)
                .votosRestantes(info.getVotosRestantes())
                .podeVotarMais(info.podeVotarMais)
                .candidatosJaVotados(info.candidatosJaVotados)
                .build();
    }

    default void aplicarConfigVagas(Eleicao eleicao, ConfigurarVagasEleicaoRequest request) {
        if (eleicao == null || request == null) return;

        Map<UUID, Integer> vagasMap = request.toVagasMap();
        eleicao.configurarVagasMultiplosCargos(vagasMap);
    }
}