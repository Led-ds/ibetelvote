package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.cargo.dto.CargoBasicInfo;
import com.br.ibetelvote.application.cargo.dto.CargoResponse;
import com.br.ibetelvote.application.cargo.dto.CreateCargoRequest;
import com.br.ibetelvote.application.cargo.dto.UpdateCargoRequest;
import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Interface de serviço para operações com cargos ministeriais.
 * Atualizada para incluir funcionalidades de categoria, hierarquia e elegibilidade.
 */
public interface CargoService {

    // === OPERAÇÕES BÁSICAS ===

    CargoResponse createCargo(CreateCargoRequest request);
    CargoResponse getCargoById(UUID id);
    Page<CargoResponse> getAllCargos(Pageable pageable);
    List<CargoResponse> getAllCargos();
    CargoResponse updateCargo(UUID id, UpdateCargoRequest request);
    void deleteCargo(UUID id);

    // === CONSULTAS COM FILTROS ===

    /**
     * Lista cargos com filtros múltiplos
     */
    Page<CargoResponse> getAllCargosComFiltros(String nome, UUID categoriaId, HierarquiaCargo hierarquia,
                                               Boolean ativo, Boolean disponivelEleicao, Pageable pageable);

    // === CONSULTAS POR CATEGORIA ===

    /**
     * Lista cargos de uma categoria específica
     */
    List<CargoResponse> getCargosByCategoria(UUID categoriaId);

    /**
     * Lista cargos ordenados por precedência dentro da categoria
     */
    List<CargoResponse> getCargosOrdenadosByCategoria(UUID categoriaId);

    /**
     * Lista cargos ativos de uma categoria
     */
    List<CargoResponse> getCargosAtivosByCategoria(UUID categoriaId);

    /**
     * Lista cargos disponíveis para eleições de uma categoria
     */
    List<CargoResponse> getCargosDisponiveisByCategoria(UUID categoriaId);

    // === CONSULTAS POR HIERARQUIA ===

    /**
     * Lista cargos de uma hierarquia específica
     */
    List<CargoResponse> getCargosByHierarquia(HierarquiaCargo hierarquia);

    /**
     * Lista cargos ativos de uma hierarquia específica
     */
    List<CargoResponse> getCargosAtivosByHierarquia(HierarquiaCargo hierarquia);

    /**
     * Lista cargos ministeriais (Pastoral, Presbiteral, Diaconal)
     */
    List<CargoResponse> getCargosMinisteriais();

    /**
     * Lista cargos ministeriais ativos
     */
    List<CargoResponse> getCargosMinisteriaisAtivos();

    /**
     * Lista cargos de liderança
     */
    List<CargoResponse> getCargosLideranca();

    /**
     * Lista cargos administrativos
     */
    List<CargoResponse> getCargosAdministrativos();

    // === CONSULTAS ESPECÍFICAS ===

    List<CargoResponse> getCargosAtivos();
    List<CargoResponse> getCargosDisponiveis();
    Page<CargoResponse> getCargosDisponiveis(Pageable pageable);
    List<CargoResponse> getCargosInativos();
    List<CargoResponse> getCargosIncompletos();
    List<CargoResponse> getCargosComCandidatos();
    List<CargoResponse> getCargosByNome(String nome);

    // === OPERAÇÕES DE STATUS ===

    CargoResponse ativarCargo(UUID id);
    CargoResponse desativarCargo(UUID id);
    CargoResponse ativarParaEleicao(UUID id);
    CargoResponse desativarParaEleicao(UUID id);

    // === OPERAÇÕES DE PRECEDÊNCIA ===

    /**
     * Altera ordem de precedência de um cargo na categoria
     */
    CargoResponse alterarOrdemPrecedencia(UUID id, Integer novaOrdem);

    /**
     * Retorna próxima ordem disponível na categoria
     */
    Integer getProximaOrdemPrecedencia(UUID categoriaId);

    /**
     * Reorganiza ordens de precedência na categoria
     */
    List<CargoResponse> reorganizarOrdensCategoria(UUID categoriaId);

    // === OPERAÇÕES DE ELEGIBILIDADE ===

    /**
     * Lista cargos que podem se candidatar ao cargo especificado
     */
    List<CargoResponse> getCargosElegiveisParaCargo(UUID cargoId);

    /**
     * Lista cargos elegíveis para uma hierarquia
     */
    List<CargoResponse> getCargosElegiveisParaHierarquia(HierarquiaCargo hierarquia);

    /**
     * Lista cargos que um membro de determinado nível pode se candidatar
     */
    List<CargoResponse> getCargosElegiveisParaMembro(String nivelMembro);

    /**
     * Verifica se um cargo pode candidatar-se a outro
     */
    boolean verificarElegibilidade(UUID cargoOrigemId, UUID cargoDestinoId);

    // === VALIDAÇÕES ===

    boolean existsCargoByNome(String nome);
    boolean isNomeDisponivel(String nome);
    boolean isNomeDisponivelParaAtualizacao(String nome, UUID cargoId);
    boolean canDeleteCargo(UUID id);
    boolean isOrdemPrecedenciaDisponivel(UUID categoriaId, Integer ordem);

    // === ESTATÍSTICAS ===

    long getTotalCargos();
    long getTotalCargosAtivos();
    long getTotalCargosInativos();
    long getTotalCargosDisponiveis();

    /**
     * Retorna estatísticas por categoria
     */
    Map<String, Object> getEstatisticasPorCategoria();

    /**
     * Retorna estatísticas por hierarquia
     */
    Map<String, Object> getEstatisticasPorHierarquia();

    /**
     * Retorna estatísticas gerais
     */
    Map<String, Object> getEstatisticasGerais();

    // === RELATÓRIOS ===

    /**
     * Gera relatório completo dos cargos
     */
    List<Map<String, Object>> getRelatorioCompleto();

    /**
     * Relatório detalhado por categoria e hierarquia
     */
    Map<String, List<CargoResponse>> getRelatorioHierarquiaPorCategoria();

    /**
     * Lista cargos criados recentemente
     */
    List<CargoResponse> getCargosRecentes();

    /**
     * Lista cargos por período
     */
    List<CargoResponse> getCargosPorPeriodo(LocalDateTime inicio, LocalDateTime fim);

    // === UTILITÁRIOS ===

    List<CargoBasicInfo> getCargosBasicInfo();
    List<CargoBasicInfo> getCargosBasicInfoByCategoria(UUID categoriaId);

    /**
     * Lista hierarquias disponíveis
     */
    List<Map<String, Object>> getHierarquiasDisponiveis();

    /**
     * Lista níveis de elegibilidade
     */
    List<Map<String, Object>> getNiveisElegibilidade();

    // === OPERAÇÕES EM LOTE ===

    List<CargoResponse> ativarCargos(List<UUID> ids);
    List<CargoResponse> desativarCargos(List<UUID> ids);
    void deleteCargos(List<UUID> ids);

    // === VALIDAÇÃO DE DADOS ===

    void validarDadosCargo(String nome, String descricao, UUID categoriaId,
                           HierarquiaCargo hierarquia, List<String> elegibilidade);
}