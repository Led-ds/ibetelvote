package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.cargo.dto.CargoBasicInfo;
import com.br.ibetelvote.application.cargo.dto.CargoResponse;
import com.br.ibetelvote.application.cargo.dto.CreateCargoRequest;
import com.br.ibetelvote.application.cargo.dto.UpdateCargoRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CargoService {
    /**
     * Cria um novo cargo
     */
    CargoResponse createCargo(CreateCargoRequest request);

    /**
     * Busca cargo por ID
     */
    CargoResponse getCargoById(UUID id);

    /**
     * Lista todos os cargos com paginação
     */
    Page<CargoResponse> getAllCargos(Pageable pageable);

    /**
     * Lista todos os cargos
     */
    List<CargoResponse> getAllCargos();

    /**
     * Atualiza um cargo existente
     */
    CargoResponse updateCargo(UUID id, UpdateCargoRequest request);

    /**
     * Remove um cargo
     */
    void deleteCargo(UUID id);

    /**
     * Lista cargos ativos
     */
    List<CargoResponse> getCargosAtivos();

    /**
     * Lista cargos ativos com paginação
     */
    Page<CargoResponse> getCargosAtivos(Pageable pageable);

    /**
     * Lista cargos inativos
     */
    List<CargoResponse> getCargosInativos();

    /**
     * Busca cargos por nome (busca parcial)
     */
    List<CargoResponse> getCargosByNome(String nome);

    /**
     * Lista cargos disponíveis para eleições
     */
    List<CargoResponse> getCargosDisponiveis();

    /**
     * Ativa um cargo
     */
    CargoResponse ativarCargo(UUID id);

    /**
     * Desativa um cargo
     */
    CargoResponse desativarCargo(UUID id);

    /**
     * Verifica se cargo existe por nome
     */
    boolean existsCargoByNome(String nome);

    /**
     * Verifica se nome está disponível para uso
     */
    boolean isNomeDisponivel(String nome);

    /**
     * Verifica se nome está disponível para atualização
     */
    boolean isNomeDisponivelParaAtualizacao(String nome, UUID cargoId);

    /**
     * Verifica se cargo pode ser removido
     */
    boolean canDeleteCargo(UUID id);

    /**
     * Conta total de cargos
     */
    long getTotalCargos();

    /**
     * Conta cargos ativos
     */
    long getTotalCargosAtivos();

    /**
     * Conta cargos inativos
     */
    long getTotalCargosInativos();

    /**
     * Conta cargos disponíveis para eleições
     */
    long getTotalCargosDisponiveis();

    /**
     * Retorna informações básicas de todos os cargos ativos
     */
    List<CargoBasicInfo> getCargosBasicInfo();

    /**
     * Valida se dados do cargo são válidos
     */
    void validarDadosCargo(String nome, String descricao);
}