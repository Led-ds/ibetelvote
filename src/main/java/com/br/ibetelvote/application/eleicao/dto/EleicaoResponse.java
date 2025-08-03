package com.br.ibetelvote.application.eleicao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EleicaoResponse {

    private UUID id;
    private String nome;
    private String descricao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private Boolean ativa;
    private Integer totalElegiveis;
    private Integer totalVotantes;
    private Boolean permiteVotoBranco;
    private Boolean permiteVotoNulo;
    private Boolean exibeResultadosParciais;
    private String instrucoesVotacao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Dados relacionados
    private List<CargoBasicInfo> cargos;
    private List<CandidatoBasicInfo> candidatos;

    // Campos computados
    private String statusDescricao;
    private double percentualParticipacao;
    private int totalVotosContabilizados;
    private long duracaoEmHoras;
    private boolean votacaoAberta;
    private boolean votacaoEncerrada;
    private boolean votacaoFutura;
    private boolean temCargos;
    private boolean temCandidatos;
    private boolean podeSerAtivada;
}