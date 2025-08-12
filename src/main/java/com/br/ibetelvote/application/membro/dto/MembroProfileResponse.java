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
public class MembroProfileResponse {

    private UUID id;
    private String nome;
    private String email;
    private String cpf;
    private LocalDate dataNascimento;

    // Igreja
    private String cargo;
    private String departamento;
    private LocalDate dataBatismo;
    private LocalDate dataMembroDesde;

    // Contato
    private String telefone;
    private String celular;

    // Endereço
    private String endereco;
    private String cidade;
    private String estado;
    private String cep;

    // Outros
    private String foto;
    private String observacoes;
    private Boolean ativo;

    // Status
    private boolean hasUser;
    private boolean isBasicProfileComplete;
    private boolean isFullProfileComplete;
    private String photoUrl;

    // Auditoria
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}