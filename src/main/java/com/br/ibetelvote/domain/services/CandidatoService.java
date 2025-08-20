package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.candidato.dto.CandidatoResponse;
import com.br.ibetelvote.application.candidato.dto.CreateCandidatoRequest;
import com.br.ibetelvote.application.candidato.dto.UpdateCandidatoRequest;
import com.br.ibetelvote.application.shared.dto.UploadPhotoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CandidatoService {

    // === OPERAÇÕES BÁSICAS ===
    CandidatoResponse createCandidato(CreateCandidatoRequest request);
    CandidatoResponse getCandidatoById(UUID id);
    List<CandidatoResponse> getCandidatosByEleicaoId(UUID eleicaoId);
    List<CandidatoResponse> getCandidatosByCargoId(UUID cargoId);
    CandidatoResponse updateCandidato(UUID id, UpdateCandidatoRequest request);
    void deleteCandidato(UUID id);

    // === OPERAÇÕES DE APROVAÇÃO ===
    void aprovarCandidato(UUID id);
    void reprovarCandidato(UUID id, String motivo);

    // === OPERAÇÕES DE CONTROLE ===
    void ativarCandidato(UUID id);
    void desativarCandidato(UUID id);
    void definirNumeroCandidato(UUID id, String numero);

    // === OPERAÇÕES DE FOTO ===
    UploadPhotoResponse uploadFotoCampanha(UUID id, MultipartFile file);
    void removeFotoCampanha(UUID id);

    // === CONSULTAS ESPECÍFICAS ===
    List<CandidatoResponse> getCandidatosAprovados(UUID cargoId);
    List<CandidatoResponse> getCandidatosPendentesAprovacao();
    List<CandidatoResponse> getCandidatosByMembroId(UUID membroId);
    CandidatoResponse getCandidatoByNumero(String numero, UUID eleicaoId);

    // === VALIDAÇÕES ===
    boolean existsCandidatoByMembroAndCargo(UUID membroId, UUID cargoId);
    boolean existsCandidatoByNumero(String numero, UUID eleicaoId);
    boolean canDeleteCandidato(UUID id);

    // === ESTATÍSTICAS ===
    long getTotalCandidatosByEleicao(UUID eleicaoId);
    long getTotalCandidatosByCargo(UUID cargoId);
    long getTotalCandidatosAprovados();
}