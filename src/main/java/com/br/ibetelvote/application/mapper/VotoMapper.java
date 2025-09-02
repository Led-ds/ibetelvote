package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.voto.dto.ValidarVotacaoResponse;
import com.br.ibetelvote.application.voto.dto.VotoAuditResponse;
import com.br.ibetelvote.application.voto.dto.VotoResponse;
import com.br.ibetelvote.application.voto.dto.VotoStatsResponse;
import com.br.ibetelvote.domain.entities.Voto;
import com.br.ibetelvote.domain.entities.enums.TipoVoto;
import org.mapstruct.*;

import java.util.List;
import java.util.ArrayList;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface VotoMapper {

    default VotoResponse toResponse(Voto voto) {
        if (voto == null) {
            return null;
        }

        return VotoResponse.builder()
                .id(voto.getId())
                .membroId(voto.getMembro() != null ? voto.getMembro().getId() : null)
                .eleicaoId(voto.getEleicao() != null ? voto.getEleicao().getId() : null)
                .cargoPretendidoId(voto.getCargoPretendido() != null ? voto.getCargoPretendido().getId() : null)
                .candidatoId(voto.getCandidato() != null ? voto.getCandidato().getId() : null)

                .tipoVoto(voto.getTipoVoto())

                // Mapear boolean para compatibilidade
                .votoBranco(TipoVoto.BRANCO.equals(voto.getTipoVoto()))
                .votoNulo(TipoVoto.NULO.equals(voto.getTipoVoto()))

                .hashVoto(voto.getHashVoto())
                .dataVoto(voto.getDataVoto())

                .nomeEleicao(voto.getNomeEleicao())
                .nomeCargoPretendido(voto.getNomeCargoPretendido())
                .nomeCandidato(voto.getNomeCandidato())
                .numeroCandidato(voto.getNumeroCandidato())
                .nomeMembro(voto.getNomeMembro())
                .dataVotoFormatada(voto.getDataVotoFormatada())

                // Dados adicionais
                .resumoVoto(voto.getResumoVoto())
                .votoSeguro(voto.isVotoSeguro())
                .ipMascarado(voto.getIpMascarado())

                // Campos computados
                .votoValido(voto.isVotoValido())
                .statusVoto(voto.getTipoVotoDescricao())
                .tipoVotoDescricao(voto.getTipoVotoDescricao())
                .build();
    }

    default VotoAuditResponse toAuditResponse(Voto voto) {
        if (voto == null) {
            return null;
        }

        return VotoAuditResponse.builder()
                .id(voto.getId())
                .eleicaoId(voto.getEleicao() != null ? voto.getEleicao().getId() : null)
                .cargoPretendidoId(voto.getCargoPretendido() != null ? voto.getCargoPretendido().getId() : null)

                .tipoVoto(voto.getTipoVoto())

                //COMPATIBILIDADE
                .votoBranco(TipoVoto.BRANCO.equals(voto.getTipoVoto()))
                .votoNulo(TipoVoto.NULO.equals(voto.getTipoVoto()))

                .dataVoto(voto.getDataVoto())

                // Dados seguros
                .nomeEleicao(voto.getNomeEleicao())
                .nomeCargoPretendido(voto.getNomeCargoPretendido())
                .dataVotoFormatada(voto.getDataVotoFormatada())
                .votoValido(voto.isVotoValido())
                .tipoVotoDescricao(voto.getTipoVotoDescricao())
                .statusVoto(voto.getTipoVotoDescricao())
                .build();
    }

    default List<VotoResponse> toResponseList(List<Voto> votos) {
        if (votos == null) {
            return new ArrayList<>();
        }
        return votos.stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    default List<VotoAuditResponse> toAuditResponseList(List<Voto> votos) {
        if (votos == null) {
            return new ArrayList<>();
        }
        return votos.stream()
                .map(this::toAuditResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    default VotoStatsResponse fromMapToStats(java.util.Map<String, Long> stats) {
        if (stats == null) {
            return VotoStatsResponse.builder().build();
        }

        long total = stats.getOrDefault("totalVotos", 0L);
        long validos = stats.getOrDefault("votosValidos", 0L);
        long branco = stats.getOrDefault("votosBranco", 0L);
        long nulo = stats.getOrDefault("votosNulo", 0L);
        long votantes = stats.getOrDefault("votantesUnicos", 0L);

        VotoStatsResponse.VotoStatsResponseBuilder builder = VotoStatsResponse.builder()
                .totalVotos(total)
                .votosValidos(validos)
                .votosBranco(branco)
                .votosNulo(nulo)
                .votantesUnicos(votantes);

        // Calcular percentuais
        if (total > 0) {
            builder.percentualVotosValidos((validos * 100.0) / total)
                    .percentualVotosBranco((branco * 100.0) / total)
                    .percentualVotosNulo((nulo * 100.0) / total);
        }

        return builder.build();
    }

    default ValidarVotacaoResponse toValidarVotacaoResponse(
            boolean votacaoValida,
            List<String> erros,
            List<String> avisos,
            int totalVotos,
            boolean membroElegivel,
            boolean eleicaoDisponivel,
            boolean jaVotou) {

        return ValidarVotacaoResponse.builder()
                .votacaoValida(votacaoValida && (erros == null || erros.isEmpty()))
                .erros(erros != null ? erros : new ArrayList<>())
                .avisos(avisos != null ? avisos : new ArrayList<>())
                .totalVotos(totalVotos)
                .membroElegivel(membroElegivel)
                .eleicaoDisponivel(eleicaoDisponivel)
                .jaVotou(jaVotou)
                .build();
    }

    default List<java.util.Map<String, Object>> fromObjectArrayToMapList(
            List<Object[]> resultados,
            String... nomesColuna) {

        if (resultados == null) {
            return new ArrayList<>();
        }

        return resultados.stream()
                .map(resultado -> {
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    for (int i = 0; i < Math.min(resultado.length, nomesColuna.length); i++) {
                        item.put(nomesColuna[i], resultado[i]);
                    }
                    return item;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @BeforeMapping
    default void validarVoto(Voto voto) {
        if (voto != null) {
            try {
                voto.validarConsistencia();
            } catch (Exception e) {
                // Log mas não impede mapeamento
                System.err.println("Aviso na validação do voto: " + e.getMessage());
            }
        }
    }

    @AfterMapping
    default void completarDadosResponse(@MappingTarget VotoResponse response, Voto voto) {
        if (response != null && voto != null) {
            // Garantir consistência dos campos computados
            response.setVotoValido(voto.isVotoValido());
            response.setStatusVoto(voto.getTipoVotoDescricao());

            // Mascarar hash se muito longo (segurança)
            if (response.getHashVoto() != null && response.getHashVoto().length() > 8) {
                response.setHashVoto(response.getHashVoto().substring(0, 8) + "...");
            }

            response.setVotoBranco(TipoVoto.BRANCO.equals(response.getTipoVoto()));
            response.setVotoNulo(TipoVoto.NULO.equals(response.getTipoVoto()));
        }
    }

    @AfterMapping
    default void garantirSegurancaAuditoria(@MappingTarget VotoAuditResponse response, Voto voto) {
        if (response != null) {
            // Garantir que não há vazamento de dados sensíveis
            // (método já é seguro, mas reforça)

            if (response.getTipoVoto() != null) {
                response.setVotoBranco(TipoVoto.BRANCO.equals(response.getTipoVoto()));
                response.setVotoNulo(TipoVoto.NULO.equals(response.getTipoVoto()));
            }
        }
    }
}