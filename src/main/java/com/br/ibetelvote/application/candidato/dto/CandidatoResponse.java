package com.br.ibetelvote.application.candidato.dto;

import com.br.ibetelvote.application.auth.dto.MembroBasicInfo;
import com.br.ibetelvote.application.cargo.dto.CargoBasicInfo;
import com.br.ibetelvote.application.eleicao.dto.EleicaoBasicInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidatoResponse {

    private UUID id;
    private UUID membroId;
    private UUID eleicaoId;
    private UUID cargoPretendidoId;
    private String numeroCandidato;
    private String nomeCandidato;
    private String descricaoCandidatura;
    private String propostas;
    private String experiencia;
    private Boolean ativo;
    private Boolean aprovado;
    private String motivoReprovacao;
    private LocalDateTime dataAprovacao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Relacionamentos
    private MembroBasicInfo membro;
    private EleicaoBasicInfo eleicao;
    private CargoBasicInfo cargoPretendido;

    // Foto de campanha
    private boolean temFotoCampanha;
    private String fotoBase64; // Apenas quando necessário
    private long fotoSize;

    // Campos computados
    private String displayName;
    private String nomeCargoPretendido;
    private String nomeMembro;
    private String nomeEleicao;
    private String cargoAtualMembro;
    private String emailMembro;
    private int totalVotos;
    private double percentualVotos;
    private String resumoVotacao;
    private String statusCandidatura;
    private String numeroFormatado;
    private boolean candidaturaCompleta;
    private boolean elegivel;
    private boolean podeReceberVotos;
    private boolean membroAtivo;
    private boolean membroPodeSeCandidarParaCargo;
}