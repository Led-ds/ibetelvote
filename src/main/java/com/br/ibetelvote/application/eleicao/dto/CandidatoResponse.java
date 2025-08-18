package com.br.ibetelvote.application.eleicao.dto;

import com.br.ibetelvote.application.auth.dto.MembroBasicInfo;
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
    private UUID cargoId;
    private String numeroCandidato;
    private String nomeCandidato;
    private String nomeCargoRetendido;
    private String descricaoCandidatura;
    private String propostas;
    private String experiencia;

    // === CAMPOS DE FOTO REFATORADOS ===
    // Removido: private String fotoCampanha;
    private String fotoCampanhaTipo;      // Tipo MIME da imagem (image/jpeg, image/png, etc.)
    private String fotoCampanhaNome;      // Nome original do arquivo
    private String fotoCampanhaBase64;    // Dados da imagem em Base64 (opcional, para quando precisar)
    private Long fotoCampanhaSize;        // Tamanho do arquivo em bytes

    private Boolean ativo;
    private Boolean aprovado;
    private String motivoReprovacao;
    private LocalDateTime dataAprovacao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Dados relacionados
    private MembroBasicInfo membro;
    private EleicaoBasicInfo eleicao;
    private CargoBasicInfo cargo;

    // Campos computados
    private int totalVotos;
    private String statusCandidatura;
    private String numeroFormatado;
    private String fotoCampanhaUrl;       // URL para endpoint que serve a foto
    private double percentualVotos;
    private String resumoVotacao;
    private boolean temFotoCampanha;
    private boolean temNumero;
    private boolean candidaturaCompleta;
    private boolean podeReceberVotos;
}