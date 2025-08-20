package com.br.ibetelvote.application.cargo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoBasicInfo {

    private UUID id;
    private String nome;
    private Boolean ativo;
    private String status;
}
