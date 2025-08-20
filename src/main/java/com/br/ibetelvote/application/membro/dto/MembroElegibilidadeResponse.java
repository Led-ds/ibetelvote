package com.br.ibetelvote.application.membro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembroElegibilidadeResponse {

    private UUID membroId;
    private String nomeMembro;
    private String cargoAtual;
    private boolean podeVotar;
    private List<String> cargosElegiveis;
    private String motivoInelegibilidade;
}