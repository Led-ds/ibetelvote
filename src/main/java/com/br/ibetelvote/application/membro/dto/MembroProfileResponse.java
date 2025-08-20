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
    private UUID cargoAtualId; // MUDANÇA: era String cargo
    private String nomeCargoAtual; // ADICIONADO: nome do cargo para exibição
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

    // Foto
    private String fotoBase64;
    private String fotoTipo;
    private String fotoNome;
    private boolean temFoto;

    // Outros
    private String observacoes;
    private Boolean ativo;

    // Status
    private boolean hasUser;
    private boolean isBasicProfileComplete;
    private boolean isFullProfileComplete;

    // Auditoria
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}