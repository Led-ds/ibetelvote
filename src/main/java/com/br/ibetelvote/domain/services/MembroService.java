package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.membro.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface MembroService {
    MembroResponse createMembro(CreateMembroRequest request);
    MembroResponse getMembroById(UUID id);
    MembroResponse getMembroByEmail(String email);
    Page<MembroListResponse> getAllMembros(MembroFilterRequest filter);
    MembroResponse updateMembro(UUID id, UpdateMembroRequest request);
    void deleteMembro(UUID id);
    void activateMembro(UUID id);
    void deactivateMembro(UUID id);
    UploadPhotoResponse uploadPhoto(UUID id, MultipartFile file);
    long getTotalMembros();
    long getTotalMembrosAtivos();
}