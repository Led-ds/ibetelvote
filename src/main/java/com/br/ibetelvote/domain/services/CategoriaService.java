package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.categoria.dto.CategoriaBasicInfo;
import com.br.ibetelvote.application.categoria.dto.CategoriaResponse;
import com.br.ibetelvote.application.categoria.dto.CreateCategoriaRequest;
import com.br.ibetelvote.application.categoria.dto.UpdateCategoriaRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public interface CategoriaService {

    CategoriaResponse createCategoria(CreateCategoriaRequest request);
    CategoriaResponse getCategoriaById(UUID id);
    Page<CategoriaResponse> getAllCategorias(Pageable pageable);
    List<CategoriaResponse> getAllCategorias();
    CategoriaResponse updateCategoria(UUID id, UpdateCategoriaRequest request);

    void deleteCategoria(UUID id);

    CategoriaResponse ativarCategoria(UUID id);
    CategoriaResponse desativarCategoria(UUID id);

    // CONSULTAS ESPECÍFICAS
    List<CategoriaResponse> getCategoriasAtivas();
    Page<CategoriaResponse> getCategoriasAtivas(Pageable pageable);
    List<CategoriaResponse> getCategoriasInativas();
    List<CategoriaResponse> getCategoriasOrderByExibicao();
    List<CategoriaBasicInfo> getCategoriasParaSelecao();
    List<CategoriaResponse> getCategoriasByNome(String nome);
    List<CategoriaResponse> getCategoriasComCargos();

    List<CategoriaResponse> getCategoriasSemCargos();
    List<CategoriaResponse> getCategoriasComCargosAtivos();
    List<CategoriaResponse> getCategoriasComCargosDisponiveis();

    //VALIDAÇÕES
    boolean existsCategoriaByNome(String nome);
    boolean isNomeDisponivel(String nome);
    boolean isNomeDisponivelParaAtualizacao(String nome, UUID categoriaId);
    boolean canDeleteCategoria(UUID id);
    boolean isOrdemExibicaoDisponivel(Integer ordem);
    boolean isOrdemDisponivelParaAtualizacao(Integer ordem, UUID categoriaId);

    // OPERAÇÕES DE ORDEM
    Integer getProximaOrdemExibicao();
    void reorganizarOrdens();
    CategoriaResponse alterarOrdem(UUID id, Integer novaOrdem);
    CategoriaResponse moverParaCima(UUID id);
    CategoriaResponse moverParaBaixo(UUID id);

    //ESTATÍSTICAS
    long getTotalCategorias();
    long getTotalCategoriasAtivas();
    long getTotalCategoriasInativas();
    long getTotalCategoriasComCargos();
    long getTotalCategoriasComCargosDisponiveis();


    Map<String, Object> getEstatisticasGerais();
    Map<String, Map<String, Long>> getEstatisticasCargosPorCategoria();
    Map<String, Map<String, Long>> getDistribuicaoHierarquias();

    // RELATÓRIOS
    List<CategoriaResponse> getCategoriasMaisUtilizadas();
    List<CategoriaResponse> getCategoriasRecentes();
    List<CategoriaResponse> getCategoriasNaoRemoviveis();
    List<Map<String, Object>> getRelatorioCompleto();
    List<CategoriaResponse> getCategoriasPorPeriodo(LocalDateTime inicio, LocalDateTime fim);

    // OPERAÇÕES EM LOTE
    List<CategoriaResponse> ativarCategorias(List<UUID> ids);
    List<CategoriaResponse> desativarCategorias(List<UUID> ids);

    void deleteCategorias(List<UUID> ids);

    //VALIDAÇÃO DE DADOS
    void validarDadosCategoria(String nome, String descricao, Integer ordem);
    void validarEstadoCategoria(UUID id);
    void validarIntegridadeParaRemocao(UUID id);

    //OPERAÇÕES ADMINISTRATIVAS
    int limparCategoriasVazias();
    void reorganizarTodasAsOrdens();
    void sincronizarEstatisticas();

    List<String> verificarECorrigirInconsistencias();
}