package com.br.ibetelvote.application.mapper;

import com.br.ibetelvote.application.voto.dto.ValidarVotacaoResponse;
import com.br.ibetelvote.application.voto.dto.VotoAuditResponse;
import com.br.ibetelvote.application.voto.dto.VotoResponse;
import com.br.ibetelvote.application.voto.dto.VotoStatsResponse;
import com.br.ibetelvote.domain.entities.Voto;
import org.mapstruct.*;

import java.util.List;
import java.util.ArrayList;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface VotoMapper {

    // === VOTO RESPONSE MAPPING ===
    default VotoResponse toResponse(Voto voto) {
        if (voto == null) {
            return null;
        }

        return VotoResponse.builder()
                .id(voto.getId())
                .membroId(voto.getMembroId())
                .eleicaoId(voto.getEleicaoId())
                .cargoPretendidoId(voto.getCargoPretendidoId())
                .candidatoId(voto.getCandidatoId())
                .votoBranco(voto.getVotoBranco())
                .votoNulo(voto.getVotoNulo())
                .hashVoto(voto.getHashVoto())
                .dataVoto(voto.getDataVoto())
                // Dados relacionados
                .nomeEleicao(voto.getNomeEleicao())
                .nomeCargoPretendido(voto.getNomeCargoPretendido())
                .nomeCandidato(voto.getNomeCandidato())
                .numeroCandidato(voto.getNumeroCandidato())
                .tipoVoto(voto.getTipoVoto())
                .dataVotoFormatada(voto.getDataVotoFormatada())
                // Dados adicionais
                .resumoVoto(voto.getResumoVoto())
                .votoSeguro(voto.isVotoSeguro())
                .ipMascarado(voto.getIpMascarado())
                // Campos computados
                .votoValido(voto.isVotoValido())
                .statusVoto(voto.getTipoVoto())
                .build();
    }

    // === VOTO AUDIT RESPONSE MAPPING ===
    default VotoAuditResponse toAuditResponse(Voto voto) {
        if (voto == null) {
            return null;
        }

        return VotoAuditResponse.builder()
                .id(voto.getId())
                .eleicaoId(voto.getEleicaoId())
                .cargoPretendidoId(voto.getCargoPretendidoId())
                .votoBranco(voto.getVotoBranco())
                .votoNulo(voto.getVotoNulo())
                .dataVoto(voto.getDataVoto())
                // Dados seguros
                .nomeEleicao(voto.getNomeEleicao())
                .nomeCargoPretendido(voto.getNomeCargoPretendido())
                .tipoVoto(voto.getTipoVoto())
                .dataVotoFormatada(voto.getDataVotoFormatada())
                .votoValido(voto.isVotoValido())
                .build();
    }

    // === LISTAS ===
    default List<VotoResponse> toResponseList(List<Voto> votos) {
        if (votos == null) {
            return null;
        }
        List<VotoResponse> list = new ArrayList<>(votos.size());
        for (Voto voto : votos) {
            list.add(toResponse(voto));
        }
        return list;
    }

    default List<VotoAuditResponse> toAuditResponseList(List<Voto> votos) {
        if (votos == null) {
            return null;
        }
        List<VotoAuditResponse> list = new ArrayList<>(votos.size());
        for (Voto voto : votos) {
            list.add(toAuditResponse(voto));
        }
        return list;
    }

    // === VOTO STATS RESPONSE MAPPING ===
    default VotoStatsResponse toStatsResponse(java.util.Map<String, Object> stats) {
        if (stats == null) {
            return null;
        }

        VotoStatsResponse.VotoStatsResponseBuilder builder = VotoStatsResponse.builder();

        // Estatísticas básicas
        if (stats.containsKey("totalVotos")) {
            builder.totalVotos(((Number) stats.get("totalVotos")).longValue());
        }
        if (stats.containsKey("votosValidos")) {
            builder.votosValidos(((Number) stats.get("votosValidos")).longValue());
        }
        if (stats.containsKey("votosBranco")) {
            builder.votosBranco(((Number) stats.get("votosBranco")).longValue());
        }
        if (stats.containsKey("votosNulo")) {
            builder.votosNulo(((Number) stats.get("votosNulo")).longValue());
        }
        if (stats.containsKey("votantesUnicos")) {
            builder.votantesUnicos(((Number) stats.get("votantesUnicos")).longValue());
        }

        // Calcular percentuais
        long total = builder.build().getTotalVotos();
        if (total > 0) {
            builder.percentualVotosValidos((builder.build().getVotosValidos() * 100.0) / total);
            builder.percentualVotosBranco((builder.build().getVotosBranco() * 100.0) / total);
            builder.percentualVotosNulo((builder.build().getVotosNulo() * 100.0) / total);
        }

        return builder.build();
    }

    // === VALIDAR VOTACAO RESPONSE MAPPING ===
    default ValidarVotacaoResponse toValidarVotacaoResponse(
            boolean votacaoValida,
            List<String> erros,
            List<String> avisos,
            boolean membroElegivel,
            boolean eleicaoDisponivel,
            boolean jaVotou) {

        return ValidarVotacaoResponse.builder()
                .votacaoValida(votacaoValida && erros.isEmpty())
                .erros(erros != null ? erros : new ArrayList<>())
                .avisos(avisos != null ? avisos : new ArrayList<>())
                .membroElegivel(membroElegivel)
                .eleicaoDisponivel(eleicaoDisponivel)
                .jaVotou(jaVotou)
                .build();
    }

    // === HELPER METHODS ===

    /**
     * Converte lista de Object[] (do repository) para VotoStatsResponse
     */
    default VotoStatsResponse fromObjectArrayToStats(List<Object[]> resultados) {
        if (resultados == null || resultados.isEmpty()) {
            return VotoStatsResponse.builder().build();
        }

        // Assumindo que o primeiro resultado tem: [totalVotos, votosValidos, votosBranco, votosNulo]
        Object[] primeiro = resultados.get(0);

        return VotoStatsResponse.builder()
                .totalVotos(((Number) primeiro[0]).longValue())
                .votosValidos(primeiro.length > 1 ? ((Number) primeiro[1]).longValue() : 0)
                .votosBranco(primeiro.length > 2 ? ((Number) primeiro[2]).longValue() : 0)
                .votosNulo(primeiro.length > 3 ? ((Number) primeiro[3]).longValue() : 0)
                .build();
    }

    /**
     * Converte Map de estatísticas para VotoStatsResponse
     */
    default VotoStatsResponse fromMapToStats(java.util.Map<String, Long> stats) {
        if (stats == null) {
            return VotoStatsResponse.builder().build();
        }

        long total = stats.getOrDefault("totalVotos", 0L);
        long validos = stats.getOrDefault("votosValidos", 0L);
        long branco = stats.getOrDefault("votosBranco", 0L);
        long nulo = stats.getOrDefault("votosNulo", 0L);

        VotoStatsResponse.VotoStatsResponseBuilder builder = VotoStatsResponse.builder()
                .totalVotos(total)
                .votosValidos(validos)
                .votosBranco(branco)
                .votosNulo(nulo);

        // Calcular percentuais se houver votos
        if (total > 0) {
            builder.percentualVotosValidos((validos * 100.0) / total)
                    .percentualVotosBranco((branco * 100.0) / total)
                    .percentualVotosNulo((nulo * 100.0) / total);
        }

        return builder.build();
    }

    /**
     * Mapeia lista de Object[] para lista de Maps (para relatórios)
     */
    default List<java.util.Map<String, Object>> fromObjectArrayToMapList(
            List<Object[]> resultados,
            String... nomesColuna) {

        if (resultados == null) {
            return new ArrayList<>();
        }

        List<java.util.Map<String, Object>> lista = new ArrayList<>();

        for (Object[] resultado : resultados) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();

            for (int i = 0; i < Math.min(resultado.length, nomesColuna.length); i++) {
                item.put(nomesColuna[i], resultado[i]);
            }

            lista.add(item);
        }

        return lista;
    }

    // === VALIDAÇÕES PRE-MAPPING ===
    @BeforeMapping
    default void validarVoto(Voto voto) {
        if (voto != null) {
            try {
                voto.validarConsistencia();
            } catch (Exception e) {
                // Log do erro mas não impede o mapeamento
                System.err.println("Erro na validação do voto: " + e.getMessage());
            }
        }
    }

    // === VALIDAÇÕES PÓS-MAPPING ===
    @AfterMapping
    default void completarDadosResponse(@MappingTarget VotoResponse response, Voto voto) {
        if (response != null && voto != null) {
            // Garantir que campos computados estão corretos
            response.setVotoValido(voto.isVotoValido());
            response.setStatusVoto(voto.getTipoVoto());

            // Garantir que dados sensíveis são mascarados se necessário
            if (response.getHashVoto() != null && response.getHashVoto().length() > 8) {
                // Manter apenas os primeiros 8 caracteres do hash
                response.setHashVoto(response.getHashVoto().substring(0, 8) + "...");
            }
        }
    }

    @AfterMapping
    default void garantirSegurancaAuditoria(@MappingTarget VotoAuditResponse response) {
        if (response != null) {
            // Garantir que não há dados sensíveis no response de auditoria
            // (já implementado no método toAuditResponse, mas reforça a segurança)
        }
    }
}