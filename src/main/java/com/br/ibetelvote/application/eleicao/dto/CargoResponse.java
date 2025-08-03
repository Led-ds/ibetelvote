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
public class CargoResponse {

    private UUID id;
    private String nome;
    private String descricao;
    private Integer maxVotos;
    private Integer ordemVotacao;
    private Boolean permiteVotoBranco;
    private Boolean obrigatorio;
    private String instrucoesEspecificas;
    private UUID eleicaoId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Dados relacionados
    private EleicaoBasicInfo eleicao;
    private List<CandidatoBasicInfo> candidatos;

    // Campos computados
    private int totalVotos;
    private int totalCandidatos;
    private long totalVotosValidos;
    private long totalVotosBranco;
    private long totalVotosNulo;
    private String statusVotacao;
    private double percentualParticipacao;
    private String resumoVotacao;
    private boolean temCandidatos;
    private boolean podeReceberVotos;
}