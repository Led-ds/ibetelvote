package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.candidato.dto.*;
import com.br.ibetelvote.application.shared.dto.UploadPhotoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CandidatoService {

    // === OPERAÇÕES BÁSICAS ===
    CandidatoResponse createCandidato(CreateCandidatoRequest request);
    CandidatoResponse getCandidatoById(UUID id);
    CandidatoResponse getCandidatoByIdWithPhoto(UUID id);
    List<CandidatoResponse> getCandidatosByEleicaoId(UUID eleicaoId);
    List<CandidatoResponse> getCandidatosByCargoPretendidoId(UUID cargoId);
    CandidatoResponse updateCandidato(UUID id, UpdateCandidatoRequest request);
    void deleteCandidato(UUID id);

    // === OPERAÇÕES DE APROVAÇÃO ===
    void aprovarCandidato(UUID id);
    void reprovarCandidato(UUID id, String motivo);
    void aprovarCandidatos(List<UUID> candidatoIds);

    // === OPERAÇÕES DE CONTROLE ===
    void ativarCandidato(UUID id);
    void desativarCandidato(UUID id);
    void definirNumeroCandidato(UUID id, String numero);
    void updateCargoPretendido(UUID id, UUID novoCargoPretendidoId);

    // === OPERAÇÕES DE FOTO ===
    UploadPhotoResponse uploadFotoCampanha(UUID id, MultipartFile file);
    void removeFotoCampanha(UUID id);
    String getFotoCampanhaBase64(UUID id);

    // === CONSULTAS ESPECÍFICAS ===
    List<CandidatoResponse> getCandidatosAprovados(UUID cargoId);
    List<CandidatoResponse> getCandidatosPendentesAprovacao();
    List<CandidatoResponse> getCandidatosByMembroId(UUID membroId);
    CandidatoResponse getCandidatoByNumero(String numero, UUID eleicaoId);
    List<CandidatoListResponse> getCandidatosParaListagem(UUID eleicaoId);
    Page<CandidatoListResponse> getCandidatosParaListagem(UUID eleicaoId, Pageable pageable);
    List<CandidatoResponse> getCandidatosElegiveis(UUID eleicaoId);
    List<CandidatoRankingResponse> getRankingCandidatosPorCargo(UUID cargoId, UUID eleicaoId);
    List<CandidatoResponse> buscarCandidatosPorNome(String nome);
    List<CandidatoResponse> getCandidatosSemNumero(UUID eleicaoId);
    Page<CandidatoResponse> buscarCandidatosComFiltros(CandidatoFilterRequest filtros, Pageable pageable);

    // === VALIDAÇÕES ===
    boolean existsCandidatoByMembroAndCargo(UUID membroId, UUID cargoId, UUID eleicaoId);
    boolean existsCandidatoByNumero(String numero, UUID eleicaoId);
    boolean canDeleteCandidato(UUID id);
    CandidatoElegibilidadeResponse verificarElegibilidade(UUID id);

    // === ESTATÍSTICAS ===
    long getTotalCandidatosByEleicao(UUID eleicaoId);
    long getTotalCandidatosByCargo(UUID cargoId);
    long getTotalCandidatosAprovados();
    long getTotalCandidatosAtivos();
    CandidatoStatsResponse getEstatisticasCandidatos(UUID eleicaoId);
}