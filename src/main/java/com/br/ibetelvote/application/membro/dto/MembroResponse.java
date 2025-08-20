package com.br.ibetelvote.application.membro.dto;

import com.br.ibetelvote.application.cargo.dto.CargoBasicInfo;
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
    private String cpf;
    private String email;
    private LocalDate dataNascimento;

    // Cargo atual
    private UUID cargoAtualId;
    private CargoBasicInfo cargoAtual;
    private String nomeCargoAtual;

    // Dados da igreja
    private String departamento;
    private LocalDate dataBatismo;
    private LocalDate dataMembroDesde;

    // Dados de contato
    private String telefone;
    private String celular;
    private String endereco;
    private String cidade;
    private String estado;
    private String cep;

    // Foto
    private boolean hasPhoto;
    private String fotoBase64; // Apenas quando necessário

    // Observações
    private String observacoes;

    // Controle
    private Boolean ativo;

    // User
    private UUID userId;
    private boolean hasUser;

    // Auditoria
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Campos computados
    private String displayName;
    private String primaryPhone;
    private String fullAddress;
    private int idadeAproximada;
    private int tempoComoMembro;
    private boolean basicProfileComplete;
    private boolean hasCompleteAddress;
    private boolean hasContactInfo;
    private boolean canCreateUser;
    private boolean hasCargoAtual;
}