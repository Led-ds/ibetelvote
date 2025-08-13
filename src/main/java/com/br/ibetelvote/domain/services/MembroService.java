package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.membro.dto.*;
import com.br.ibetelvote.application.shared.dto.UploadPhotoResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MembroService {

    // === OPERAÇÕES BÁSICAS ===
    MembroResponse createMembro(CreateMembroRequest request);
    MembroResponse getMembroById(UUID id);
    MembroResponse getMembroByEmail(String email);
    Page<MembroListResponse> getAllMembros(MembroFilterRequest filter);
    MembroResponse updateMembro(UUID id, UpdateMembroRequest request);
    void deleteMembro(UUID id);

    // === OPERAÇÕES DE CONTROLE ===
    void activateMembro(UUID id);
    void deactivateMembro(UUID id);

    // === OPERAÇÕES DE ASSOCIAÇÃO ===
    void associateUser(UUID membroId, AssociateUserRequest request);
    void dissociateUser(UUID membroId);

    // === OPERAÇÕES DE FOTO ===
    UploadPhotoResponse uploadPhoto(UUID userId, MultipartFile file);
    void removePhoto(UUID id);

    // === CONSULTAS ESPECÍFICAS ===
    List<MembroListResponse> getMembrosWithoutUser();
    List<MembroListResponse> getMembrosWithUser();
    List<MembroListResponse> getMembrosWithoutPhoto();
    List<MembroListResponse> getMembrosWithIncompleteProfile();

    // === ESTATÍSTICAS ===
    long getTotalMembros();
    long getTotalMembrosAtivos();
    long getTotalMembrosWithUser();
    long getTotalMembrosWithoutUser();
}