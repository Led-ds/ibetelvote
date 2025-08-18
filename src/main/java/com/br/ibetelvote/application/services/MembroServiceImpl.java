package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.membro.dto.*;
import com.br.ibetelvote.application.mapper.MembroMapper;
import com.br.ibetelvote.application.shared.dto.UploadPhotoResponse;
import com.br.ibetelvote.domain.entities.Membro;
import com.br.ibetelvote.domain.entities.User;
import com.br.ibetelvote.domain.services.MembroService;
import com.br.ibetelvote.infrastructure.repositories.MembroJpaRepository;
import com.br.ibetelvote.infrastructure.repositories.UserJpaRepository;
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

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MembroServiceImpl implements MembroService {

    private final MembroJpaRepository membroRepository;
    private final UserJpaRepository userRepository;
    private final MembroMapper membroMapper;

    // Remover FileStorageService - não precisamos mais
    // private final FileStorageService fileStorageService;

    private static final long MAX_FILE_SIZE = 500 * 1024; // 500KB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    // === OPERAÇÕES BÁSICAS (mantém como está) ===

    @Override
    @CacheEvict(value = {"membros", "membro-stats"}, allEntries = true)
    public MembroResponse createMembro(CreateMembroRequest request) {
        log.info("Criando novo membro com email: {}", request.getEmail());

        // Verificar se email já existe
        if (membroRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado: " + request.getEmail());
        }

        // Verificar se userId já está associado a outro membro
        if (request.getUserId() != null && membroRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("Usuário já está associado a outro membro");
        }

        // Verificar se user existe (se informado)
        if (request.getUserId() != null && !userRepository.existsById(request.getUserId())) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + request.getUserId());
        }

        Membro membro = membroMapper.toEntity(request);
        Membro savedMembro = membroRepository.save(membro);

        log.info("Membro criado com sucesso - ID: {}, Nome: {}", savedMembro.getId(), savedMembro.getNome());
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

        Page<Membro> membrosPage = membroRepository.findByFilters(
                filter.getNome(),
                filter.getEmail(),
                filter.getCargo(),
                filter.getAtivo(),
                filter.getHasUser(),
                pageable
        );

        return membrosPage.map(membroMapper::toListResponse);
    }

    @Override
    @CacheEvict(value = {"membros", "membro-stats"}, allEntries = true)
    public MembroResponse updateMembro(UUID id, UpdateMembroRequest request) {
        log.info("Atualizando membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        membroMapper.updateEntityFromRequest(request, membro);
        Membro updatedMembro = membroRepository.save(membro);

        log.info("Membro atualizado com sucesso - ID: {}, Nome: {}", updatedMembro.getId(), updatedMembro.getNome());
        return membroMapper.toResponse(updatedMembro);
    }

    @Override
    @CacheEvict(value = {"membros", "membro-stats"}, allEntries = true)
    public void deleteMembro(UUID id) {
        log.info("Removendo membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        // Não precisa mais remover arquivo físico
        membroRepository.delete(membro);
        log.info("Membro removido com sucesso - ID: {}", id);
    }

    // === OPERAÇÕES DE CONTROLE (mantém como está) ===

    @Override
    @CacheEvict(value = {"membros", "membro-stats"}, allEntries = true)
    public void activateMembro(UUID id) {
        log.info("Ativando membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        membro.activate();
        membroRepository.save(membro);

        log.info("Membro ativado com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = {"membros", "membro-stats"}, allEntries = true)
    public void deactivateMembro(UUID id) {
        log.info("Desativando membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        membro.deactivate();
        membroRepository.save(membro);

        log.info("Membro desativado com sucesso - ID: {}", id);
    }

    // === OPERAÇÕES DE ASSOCIAÇÃO (mantém como está) ===

    @Override
    @CacheEvict(value = {"membros", "membro-stats"}, allEntries = true)
    public void associateUser(UUID membroId, AssociateUserRequest request) {
        log.info("Associando usuário {} ao membro {}", request.getUserId(), membroId);

        Membro membro = membroRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + membroId));

        if (!userRepository.existsById(request.getUserId())) {
            throw new IllegalArgumentException("Usuário não encontrado com ID: " + request.getUserId());
        }

        Optional<User> userOpt = userRepository.findById(request.getUserId());
        User user = userOpt.orElse(null);

        if (membroRepository.existsByUserId(user.getId())) {
            throw new IllegalArgumentException("Usuário já está associado a outro membro");
        }

        if (!user.isActive()) {
            throw new IllegalArgumentException("Não é possível associar usuário inativo");
        }

        membro.associateUser(request.getUserId());
        membroRepository.save(membro);

        log.info("Usuário associado com sucesso - Membro: {}, User: {}", membroId, request.getUserId());
    }

    @Override
    @CacheEvict(value = {"membros", "membro-stats"}, allEntries = true)
    public void dissociateUser(UUID membroId) {
        log.info("Desassociando usuário do membro {}", membroId);

        Membro membro = membroRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + membroId));

        if (!membro.hasUser()) {
            throw new IllegalArgumentException("Membro não possui usuário associado");
        }

        UUID oldUserId = membro.getUserId();
        membro.dissociateUser();
        membroRepository.save(membro);

        log.info("Usuário desassociado com sucesso - Membro: {}, Ex-User: {}", membroId, oldUserId);
    }

    // === OPERAÇÕES DE FOTO (REFATORADA) ===

    @Override
    @CacheEvict(value = "membros", key = "#userId")
    public UploadPhotoResponse uploadPhoto(UUID userId, MultipartFile file) {
        log.info("Fazendo upload de foto para membro ID: {}", userId);

        // Buscar membro
        Membro membro = membroRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com userId: " + userId));

        try {
            // Validar arquivo
            validatePhotoFile(file);

            // Converter para bytes
            byte[] fotoData = file.getBytes();
            String contentType = file.getContentType();
            String fileName = file.getOriginalFilename();

            // Atualizar foto do membro
            membro.updatePhoto(fotoData, contentType, fileName);
            membroRepository.save(membro);

            log.info("Upload de foto concluído para membro ID: {} - Arquivo: {}", userId, fileName);

            return UploadPhotoResponse.builder()
                    .fileName(fileName)
                    .fotoBase64(membro.getFotoBase64()) // Retorna Base64
                    .message("Upload realizado com sucesso")
                    .build();

        } catch (IOException e) {
            log.error("Erro ao fazer upload de foto para membro ID: {}", userId, e);
            throw new RuntimeException("Erro ao fazer upload da foto: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "membros", key = "#id")
    public void removePhoto(UUID id) {
        log.info("Removendo foto do membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        if (membro.hasPhoto()) {
            membro.removePhoto();
            membroRepository.save(membro);
            log.info("Foto removida com sucesso - ID: {}", id);
        }
    }

    /**
     * Valida o arquivo de foto
     */
    private void validatePhotoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode estar vazio");
        }

        // Validar tamanho (máximo 500KB)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("Arquivo muito grande. Tamanho máximo permitido: %d KB",
                            MAX_FILE_SIZE / 1024)
            );
        }

        // Validar tipo
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Tipo de arquivo não permitido. Permitidos: JPEG, PNG, WEBP"
            );
        }
    }

    // === CONSULTAS ESPECÍFICAS (mantém como está) ===

    @Override
    @Cacheable(value = "membros-without-user")
    @Transactional(readOnly = true)
    public List<MembroListResponse> getMembrosWithoutUser() {
        List<Membro> membros = membroRepository.findByUserIdIsNull();
        return membroMapper.toListResponseList(membros);
    }

    @Override
    @Cacheable(value = "membros-with-user")
    @Transactional(readOnly = true)
    public List<MembroListResponse> getMembrosWithUser() {
        List<Membro> membros = membroRepository.findByUserIdIsNotNull();
        return membroMapper.toListResponseList(membros);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembroListResponse> getMembrosWithoutPhoto() {
        // Ajustar query para buscar onde fotoData é null
        List<Membro> membros = membroRepository.findByFotoDataIsNull();
        return membroMapper.toListResponseList(membros);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembroListResponse> getMembrosWithIncompleteProfile() {
        List<Membro> membros = membroRepository.findMembrosWithIncompleteProfile();
        return membroMapper.toListResponseList(membros);
    }

    // === ESTATÍSTICAS (mantém como está) ===

    @Override
    @Cacheable(value = "membro-stats", key = "'total'")
    @Transactional(readOnly = true)
    public long getTotalMembros() {
        return membroRepository.count();
    }

    @Override
    @Cacheable(value = "membro-stats", key = "'total-ativos'")
    @Transactional(readOnly = true)
    public long getTotalMembrosAtivos() {
        return membroRepository.countByAtivoTrue();
    }

    @Override
    @Cacheable(value = "membro-stats", key = "'total-with-user'")
    @Transactional(readOnly = true)
    public long getTotalMembrosWithUser() {
        return membroRepository.findByUserIdIsNotNull().size();
    }

    @Override
    @Cacheable(value = "membro-stats", key = "'total-without-user'")
    @Transactional(readOnly = true)
    public long getTotalMembrosWithoutUser() {
        return membroRepository.findByUserIdIsNull().size();
    }

    // === MÉTODOS AUXILIARES ===

    private Pageable createPageable(MembroFilterRequest filter) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(filter.getDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSort()
        );
        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }
}