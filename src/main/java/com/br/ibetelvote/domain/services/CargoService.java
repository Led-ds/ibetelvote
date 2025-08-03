package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.eleicao.dto.*;

import java.util.List;
import java.util.UUID;

public interface CargoService {

    // === OPERAÇÕES BÁSICAS ===
    CargoResponse createCargo(CreateCargoRequest request);
    CargoResponse getCargoById(UUID id);
    List<CargoResponse> getCargosByEleicaoId(UUID eleicaoId);
    CargoResponse updateCargo(UUID id, UpdateCargoRequest request);
    void deleteCargo(UUID id);

    // === OPERAÇÕES ESPECÍFICAS ===
    List<CargoResponse> getCargosByEleicaoIdOrdenados(UUID eleicaoId);
    void reordernarCargos(UUID eleicaoId, List<UUID> cargoIds);

    // === VALIDAÇÕES ===
    boolean existsCargoByNomeAndEleicao(String nome, UUID eleicaoId);
    boolean canDeleteCargo(UUID id);

    // === ESTATÍSTICAS ===
    long getTotalCargosByEleicao(UUID eleicaoId);
    List<CargoResponse> getCargosObrigatorios();
}