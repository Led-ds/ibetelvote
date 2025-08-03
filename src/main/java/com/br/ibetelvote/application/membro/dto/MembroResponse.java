package com.br.ibetelvote.application.membro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroResponse {

    private UUID id;
    private String nome;
    private String email;
    private LocalDate dataNascimento;

    // Dados da igreja
    private String cargo;
    private String departamento;
    private LocalDate dataBatismo;
    private LocalDate dataMembroDesde;

    // Contato
    private String telefone;
    private String celular;
    private String endereco;
    private String cidade;
    private String estado;
    private String cep;

    // Outros
    private String foto;
    private String observacoes;
    private Boolean ativo;

    // Relacionamento
    private UUID userId;
    private UserBasicInfo user;

    // Auditoria
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Campos computados
    private String photoUrl;
    private String primaryPhone;
    private String fullAddress;
    private boolean hasUser;
    private boolean hasPhoto;
    private boolean hasCompleteAddress;
    private boolean hasContactInfo;
    private boolean isProfileComplete;
    private int idadeAproximada;
    private int tempoComoMembro;
}