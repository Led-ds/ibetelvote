package com.br.ibetelvote.application.eleicao.dto;

import com.br.ibetelvote.application.candidato.dto.CandidatoBasicInfo;
import com.br.ibetelvote.application.cargo.dto.CargoBasicInfo;
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

    private List<CandidatoBasicInfo> candidatos;
    private List<CargoBasicInfo> cargosComCandidatos; // Apenas cargos que têm candidatos aprovados

    // === CAMPOS COMPUTADOS ===
    private String statusDescricao;
    private double percentualParticipacao;
    private int totalVotosContabilizados;
    private long duracaoEmHoras;

    // Status da votação
    private boolean votacaoAberta;
    private boolean votacaoEncerrada;
    private boolean votacaoFutura;

    // Status dos candidatos
    private boolean temCandidatos;
    private boolean temCandidatosAprovados;
    private int totalCandidatosAprovados;

    private int totalCargosComCandidatos;

    // Validações
    private boolean podeSerAtivada;
}