package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.mapper.MembroMapper;
import com.br.ibetelvote.application.membro.dto.*;
import com.br.ibetelvote.domain.entities.Membro;
import com.br.ibetelvote.domain.repositories.MembroRepository;
import com.br.ibetelvote.domain.services.FileStorageService;
import com.br.ibetelvote.domain.services.MembroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MembroServiceImpl implements MembroService {

    private final MembroRepository membroRepository;
    private final MembroMapper membroMapper;
    private final FileStorageService fileStorageService;

    @Override
    @CacheEvict(value = {"membros", "membros-stats"}, allEntries = true)
    public MembroResponse createMembro(CreateMembroRequest request) {
        log.info("Criando novo membro com email: {}", request.getEmail());

        // Verificar se email já existe
        if (membroRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado: " + request.getEmail());
        }

        Membro membro = membroMapper.toEntity(request);
        Membro savedMembro = membroRepository.save(membro);

        log.info("Membro criado com sucesso - ID: {}", savedMembro.getId());
        return membroMapper.toResponse(savedMembro);
    }

    @Override
    @Cacheable(value = "membros", key = "#id")
    @Transactional(readOnly = true)
    public MembroResponse getMembroById(UUID id) {
        log.debug("Buscando membro por ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        return membroMapper.toResponse(membro);
    }

    @Override
    @Cacheable(value = "membros", key = "#email")
    @Transactional(readOnly = true)
    public MembroResponse getMembroByEmail(String email) {
        log.debug("Buscando membro por email: {}", email);

        Membro membro = membroRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com email: " + email));

        return membroMapper.toResponse(membro);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MembroListResponse> getAllMembros(MembroFilterRequest filter) {
        log.debug("Listando membros com filtros: {}", filter);

        Pageable pageable = createPageable(filter);

        Page<Membro> membrosPage;

        if (hasFilters(filter)) {
            // Usando query customizada com filtros
            membrosPage = ((com.br.ibetelvote.infrastructure.repositories.MembroJpaRepository) membroRepository)
                    .findByFilters(filter.getNome(), filter.getEmail(), filter.getAtivo(), pageable);
        } else {
            // Busca sem filtros
            membrosPage = membroRepository.findAll(pageable);
        }

        return membrosPage.map(membroMapper::toListResponse);
    }

    @Override
    @CacheEvict(value = {"membros", "membros-stats"}, allEntries = true)
    public MembroResponse updateMembro(UUID id, UpdateMembroRequest request) {
        log.info("Atualizando membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        membroMapper.updateEntityFromRequest(request, membro);
        Membro updatedMembro = membroRepository.save(membro);

        log.info("Membro atualizado com sucesso - ID: {}", updatedMembro.getId());
        return membroMapper.toResponse(updatedMembro);
    }

    @Override
    @CacheEvict(value = {"membros", "membros-stats"}, allEntries = true)
    public void deleteMembro(UUID id) {
        log.info("Removendo membro ID: {}", id);

        if (!membroRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Membro não encontrado com ID: " + id);
        }

        membroRepository.deleteById(id);
        log.info("Membro removido com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = {"membros", "membros-stats"}, allEntries = true)
    public void activateMembro(UUID id) {
        log.info("Ativando membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        membro.activate();
        membroRepository.save(membro);

        log.info("Membro ativado com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = {"membros", "membros-stats"}, allEntries = true)
    public void deactivateMembro(UUID id) {
        log.info("Desativando membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        membro.deactivate();
        membroRepository.save(membro);

        log.info("Membro desativado com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = "membros", key = "#id")
    public UploadPhotoResponse uploadPhoto(UUID id, MultipartFile file) {
        log.info("Fazendo upload de foto para membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        try {
            String fileName = fileStorageService.storeFile(file, "membros/fotos");
            String fileUrl = "/api/v1/files/" + fileName;

            membro.updatePhoto(fileUrl);
            membroRepository.save(membro);

            log.info("Upload de foto concluído para membro ID: {} - Arquivo: {}", id, fileName);

            return UploadPhotoResponse.builder()
                    .fileName(fileName)
                    .fileUrl(fileUrl)
                    .message("Upload realizado com sucesso")
                    .build();

        } catch (Exception e) {
            log.error("Erro ao fazer upload de foto para membro ID: {}", id, e);
            throw new RuntimeException("Erro ao fazer upload da foto: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "membros-stats", key = "'total'")
    @Transactional(readOnly = true)
    public long getTotalMembros() {
        return membroRepository.count();
    }

    @Override
    @Cacheable(value = "membros-stats", key = "'total-ativos'")
    @Transactional(readOnly = true)
    public long getTotalMembrosAtivos() {
        return membroRepository.countByAtivoTrue();
    }

    // Métodos auxiliares
    private Pageable createPageable(MembroFilterRequest filter) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(filter.getDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSort()
        );
        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }

    private boolean hasFilters(MembroFilterRequest filter) {
        return filter.getNome() != null || filter.getEmail() != null || filter.getAtivo() != null;
    }
}