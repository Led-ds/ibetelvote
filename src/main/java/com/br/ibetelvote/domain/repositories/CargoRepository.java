package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Categoria;
import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CargoRepository {

    //OPERAÇÕES BÁSICAS
    List<Cargo> findAll();
    Page<Cargo> findAll(Pageable pageable);
    void deleteById(UUID id);
    long count();

    //CONSULTAS POR NOME
    Optional<Cargo> findByNome(String nome);
    boolean existsByNome(String nome);
    List<Cargo> findByNomeContainingIgnoreCase(String nome);
    boolean existsByNomeAndIdNot(String nome, UUID id);

    // Consultas específicas para disponibilidade eleitoral
    List<Cargo> findByDisponivelEleicaoTrue();
    List<Cargo> findByAtivoTrueAndDisponivelEleicaoTrue();
    long countByDisponivelEleicao(Boolean disponivel);

    //CONSULTAS ORDENADAS
    List<Cargo> findAllByOrderByNomeAsc();
    List<Cargo> findByAtivoTrueOrderByNomeAsc();
    Page<Cargo> findByAtivoTrueOrderByNomeAsc(Pageable pageable);

    //NOVAS CONSULTAS POR CATEGORIA
    List<Cargo> findByCategoria(Categoria categoria);
    List<Cargo> findByCategoriaId(UUID categoriaId);
    List<Cargo> findByCategoriaAndAtivoTrue(Categoria categoria);
    List<Cargo> findByCategoriaIdAndAtivoTrue(UUID categoriaId);
    Page<Cargo> findByCategoria(Categoria categoria, Pageable pageable);
    Page<Cargo> findByCategoriaId(UUID categoriaId, Pageable pageable);
    long countByCategoria(Categoria categoria);
    long countByCategoriaId(UUID categoriaId);

    //CONSULTAS POR STATUS
    List<Cargo> findByAtivoTrue();
    List<Cargo> findByAtivoFalse();
    Page<Cargo> findByAtivo(Boolean ativo, Pageable pageable);
    long countByAtivo(Boolean ativo);

    //CONSULTAS PARA RELATÓRIOS
    List<Cargo> findTop10ByOrderByCreatedAtDesc();
    List<Object[]> countCargosPorCategoria();
    List<Object[]> countCargosPorHierarquia();
    List<Object[]> getEstatisticasCompletas();
    List<Object[]> getRelatorioHierarquiaPorCategoria();

    //Consultas por categoria com ordenação
    List<Cargo> findByCategoriaOrderByOrdemPrecedenciaAscNomeAsc(Categoria categoria);
    List<Cargo> findByCategoriaIdOrderByOrdemPrecedenciaAscNomeAsc(UUID categoriaId);
    List<Cargo> findByCategoriaAndAtivoTrueOrderByOrdemPrecedenciaAscNomeAsc(Categoria categoria);

    //NOVAS CONSULTAS POR HIERARQUIA
    List<Cargo> findByHierarquia(HierarquiaCargo hierarquia);
    List<Cargo> findByHierarquiaAndAtivoTrue(HierarquiaCargo hierarquia);
    Page<Cargo> findByHierarquia(HierarquiaCargo hierarquia, Pageable pageable);
    long countByHierarquia(HierarquiaCargo hierarquia);

    //Hierarquia com categoria
    List<Cargo> findByCategoriaAndHierarquia(Categoria categoria, HierarquiaCargo hierarquia);
    List<Cargo> findByCategoriaIdAndHierarquia(UUID categoriaId, HierarquiaCargo hierarquia);

    //Hierarquias específicas
    List<Cargo> findByHierarquiaIn(List<HierarquiaCargo> hierarquias);
    List<Cargo> findByHierarquiaInAndAtivoTrue(List<HierarquiaCargo> hierarquias);

    //CONSULTAS POR ORDEM DE PRECEDÊNCIA
    List<Cargo> findByOrdemPrecedenciaBetween(Integer ordemMin, Integer ordemMax);
    Optional<Cargo> findByCategoriaAndOrdemPrecedencia(Categoria categoria, Integer ordem);
    Optional<Cargo> findByCategoriaIdAndOrdemPrecedencia(UUID categoriaId, Integer ordem);
    boolean existsByCategoriaAndOrdemPrecedencia(Categoria categoria, Integer ordem);
    boolean existsByCategoriaIdAndOrdemPrecedencia(UUID categoriaId, Integer ordem);
    boolean existsByCategoriaAndOrdemPrecedenciaAndIdNot(Categoria categoria, Integer ordem, UUID id);

    //Próxima ordem disponível
    Integer findNextOrdemPrecedenciaByCategoria(Categoria categoria);
    Integer findNextOrdemPrecedenciaByCategoriaId(UUID categoriaId);

    //CONSULTAS PARA ELEGIBILIDADE
    List<Cargo> findCargosElegiveisParaHierarquia(HierarquiaCargo hierarquia);
    List<Cargo> findCargosPorElegibilidade(String elegibilidade);
    List<Cargo> findCargosQuePermitemElegibilidade(String elegibilidade);
    boolean verificarElegibilidade(UUID cargoOrigemId, UUID cargoDestinoId);

    //CONSULTAS ESPECÍFICAS COMBINADAS
    List<Cargo> findCargosDisponiveis();
    Page<Cargo> findCargosDisponiveis(Pageable pageable);
    long countCargosDisponiveis();

    List<Cargo> findCargosDisponiveisByCategoria(Categoria categoria);
    List<Cargo> findCargosDisponiveisByCategoriaId(UUID categoriaId);

    List<Cargo> findCargosMinisteriais();
    List<Cargo> findCargosMinisteriaisAtivos();

    List<Cargo> findCargosLideranca();
    List<Cargo> findCargosLiderancaAtivos();

    //CONSULTAS COM FILTROS AVANÇADOS
    Page<Cargo> findByFiltros(String nome, UUID categoriaId, HierarquiaCargo hierarquia,
                              Boolean ativo, Boolean disponivelEleicao, Pageable pageable);

    List<Cargo> findByMultiplosCriterios(List<UUID> categoriaIds, List<HierarquiaCargo> hierarquias,
                                         Boolean ativo, Boolean disponivelEleicao);

    //CONSULTAS PARA VALIDAÇÃO
    boolean canDeleteCargo(UUID id);
    List<Cargo> findCargosComCandidatos();
    boolean existsCargoAtivoByNome(String nome);
    List<Cargo> findCargosIncompletos();

    //Validação de precedência
    boolean isOrdemPrecedenciaDisponivel(UUID categoriaId, Integer ordem);
    boolean isOrdemPrecedenciaDisponivel(UUID categoriaId, Integer ordem, UUID cargoId);

    //CONSULTAS ESPECÍFICAS DO CONTEXTO ECLESIÁSTICO
    List<Cargo> findCargosConselhoEclesiastico();
    List<Cargo> findCargosAdministrativos();
    List<Cargo> findCargosByFaixaHierarquia(HierarquiaCargo hierarquiaMin, HierarquiaCargo hierarquiaMax);
    List<Cargo> findCargosQuePodemElegerPara(UUID cargoDestinoId);

}