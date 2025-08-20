package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.mapper.MembroMapper;
import com.br.ibetelvote.application.membro.dto.MembroBasicInfo;
import com.br.ibetelvote.application.membro.dto.*;
import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Membro;
import com.br.ibetelvote.domain.services.MembroService;
import com.br.ibetelvote.infrastructure.repositories.CargoJpaRepository;
import com.br.ibetelvote.infrastructure.repositories.MembroJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MembroServiceImpl implements MembroService {

    private final MembroJpaRepository membroRepository;
    private final CargoJpaRepository cargoRepository;
    private final MembroMapper membroMapper;

    // === OPERAÇÕES BÁSICAS ===

    @Override
    @CacheEvict(value = "membros", allEntries = true)
    public MembroResponse createMembro(CreateMembroRequest request) {
        log.info("Criando novo membro: {}", request.getEmail());

        // Validar dados básicos
        validarDadosMembro(request.getNome(), request.getEmail(), request.getCpf());

        // Verificar se email e CPF já existem
        if (!isEmailDisponivel(request.getEmail())) {
            throw new IllegalArgumentException("Já existe um membro com o email: " + request.getEmail());
        }

        if (!isCpfDisponivel(request.getCpf())) {
            throw new IllegalArgumentException("Já existe um membro com o CPF: " + request.getCpf());
        }

        // Validar cargo se fornecido
        if (request.getCargoAtualId() != null) {
            validarCargoExiste(request.getCargoAtualId());
        }

        Membro membro = membroMapper.toEntity(request);
        Membro savedMembro = membroRepository.save(membro);

        log.info("Membro criado com sucesso - ID: {}, Email: {}", savedMembro.getId(), savedMembro.getEmail());
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
    @Cacheable(value = "membros-page")
    @Transactional(readOnly = true)
    public Page<MembroResponse> getAllMembros(Pageable pageable) {
        log.debug("Buscando todos os membros com paginação");

        Page<Membro> membros = membroRepository.findAll(pageable);
        return membros.map(membroMapper::toResponse);
    }

    @Override
    @Cacheable(value = "membros-all")
    @Transactional(readOnly = true)
    public List<MembroResponse> getAllMembros() {
        log.debug("Buscando todos os membros");

        List<Membro> membros = membroRepository.findAllByOrderByNomeAsc();
        return membroMapper.toResponseList(membros);
    }

    @Override
    @CacheEvict(value = "membros", allEntries = true)
    public MembroResponse updateMembro(UUID id, UpdateMembroRequest request) {
        log.info("Atualizando membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        // Validar email se foi alterado
        if (request.getEmail() != null && !request.getEmail().equals(membro.getEmail())) {
            if (!isEmailDisponivelParaAtualizacao(request.getEmail(), id)) {
                throw new IllegalArgumentException("Já existe um membro com o email: " + request.getEmail());
            }
        }

        // Validar CPF se foi alterado
        if (request.getCpf() != null && !request.getCpf().equals(membro.getCpf())) {
            if (!isCpfDisponivelParaAtualizacao(request.getCpf(), id)) {
                throw new IllegalArgumentException("Já existe um membro com o CPF: " + request.getCpf());
            }
        }

        // Validar cargo se fornecido
        if (request.getCargoAtualId() != null) {
            validarCargoExiste(request.getCargoAtualId());
        }

        membroMapper.updateEntityFromRequest(request, membro);
        Membro updatedMembro = membroRepository.save(membro);

        log.info("Membro atualizado com sucesso - ID: {}", updatedMembro.getId());
        return membroMapper.toResponse(updatedMembro);
    }

    @Override
    @CacheEvict(value = "membros", allEntries = true)
    public void deleteMembro(UUID id) {
        log.info("Removendo membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        // Validar se pode ser removido
        if (!canDeleteMembro(id)) {
            throw new IllegalStateException("Membro não pode ser removido pois está em uso");
        }

        membroRepository.delete(membro);
        log.info("Membro removido com sucesso - ID: {}", id);
    }

    // === CONSULTAS ESPECÍFICAS ===

    @Override
    @Cacheable(value = "membros-ativos")
    @Transactional(readOnly = true)
    public List<MembroResponse> getMembrosAtivos() {
        log.debug("Buscando membros ativos");

        List<Membro> membros = membroRepository.findByAtivoTrueOrderByNomeAsc();
        return membroMapper.toResponseList(membros);
    }

    @Override
    @Cacheable(value = "membros-ativos-page")
    @Transactional(readOnly = true)
    public Page<MembroResponse> getMembrosAtivos(Pageable pageable) {
        log.debug("Buscando membros ativos com paginação");

        Page<Membro> membros = membroRepository.findByAtivoTrueOrderByNomeAsc(pageable);
        return membros.map(membroMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembroResponse> getMembrosInativos() {
        log.debug("Buscando membros inativos");

        List<Membro> membros = membroRepository.findByAtivoFalse();
        return membroMapper.toResponseList(membros);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembroResponse> getMembrosByNome(String nome) {
        log.debug("Buscando membros por nome: {}", nome);

        List<Membro> membros = membroRepository.findByNomeContainingIgnoreCase(nome);
        return membroMapper.toResponseList(membros);
    }

    @Override
    @Transactional(readOnly = true)
    public MembroResponse getMembroByEmail(String email) {
        log.debug("Buscando membro por email: {}", email);

        Membro membro = membroRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com email: " + email));

        return membroMapper.toResponse(membro);
    }

    @Override
    @Transactional(readOnly = true)
    public MembroResponse getMembroByCpf(String cpf) {
        log.debug("Buscando membro por CPF: {}", cpf);

        // Normalizar CPF
        String cpfNormalizado = cpf.replaceAll("[^0-9]", "");

        Membro membro = membroRepository.findByCpf(cpfNormalizado)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com CPF: " + cpf));

        return membroMapper.toResponse(membro);
    }

    // === CONSULTAS POR CARGO ===

    @Override
    @Transactional(readOnly = true)
    public List<MembroResponse> getMembrosPorCargo(UUID cargoId) {
        log.debug("Buscando membros por cargo: {}", cargoId);

        List<Membro> membros = membroRepository.findByCargoAtualId(cargoId);
        return membroMapper.toResponseList(membros);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembroResponse> getMembrosSemCargo() {
        log.debug("Buscando membros sem cargo");

        List<Membro> membros = membroRepository.findMembrosSemCargo();
        return membroMapper.toResponseList(membros);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembroResponse> getMembrosElegiveisParaCargo(String nomeCargo) {
        log.debug("Buscando membros elegíveis para cargo: {}", nomeCargo);

        List<Membro> membros = membroRepository.findMembrosElegiveisParaCargo(nomeCargo);
        return membroMapper.toResponseList(membros);
    }

    // === OPERAÇÕES DE STATUS ===

    @Override
    @CacheEvict(value = "membros", allEntries = true)
    public MembroResponse ativarMembro(UUID id) {
        log.info("Ativando membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        membro.activate();
        Membro savedMembro = membroRepository.save(membro);

        log.info("Membro ativado com sucesso - ID: {}", savedMembro.getId());
        return membroMapper.toResponse(savedMembro);
    }

    @Override
    @CacheEvict(value = "membros", allEntries = true)
    public MembroResponse desativarMembro(UUID id) {
        log.info("Desativando membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        membro.deactivate();
        Membro savedMembro = membroRepository.save(membro);

        log.info("Membro desativado com sucesso - ID: {}", savedMembro.getId());
        return membroMapper.toResponse(savedMembro);
    }

    // === OPERAÇÕES DE CARGO ===

    @Override
    @CacheEvict(value = "membros", allEntries = true)
    public MembroResponse updateCargoMembro(UUID id, UpdateCargoMembroRequest request) {
        log.info("Atualizando cargo do membro ID: {} para cargo: {}", id, request.getCargoAtualId());

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        // Validar se cargo existe
        validarCargoExiste(request.getCargoAtualId());

        membro.updateCargoAtual(request.getCargoAtualId());
        Membro savedMembro = membroRepository.save(membro);

        log.info("Cargo do membro atualizado com sucesso - ID: {}", savedMembro.getId());
        return membroMapper.toResponse(savedMembro);
    }

    @Override
    @CacheEvict(value = "membros", allEntries = true)
    public MembroResponse removeCargoMembro(UUID id) {
        log.info("Removendo cargo do membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        membro.removeCargoAtual();
        Membro savedMembro = membroRepository.save(membro);

        log.info("Cargo do membro removido com sucesso - ID: {}", savedMembro.getId());
        return membroMapper.toResponse(savedMembro);
    }

    // === OPERAÇÕES DE FOTO ===

    @Override
    @CacheEvict(value = "membros", allEntries = true)
    public MembroResponse uploadFotoMembro(UUID id, MembroUploadFotoRequest request) {
        log.info("Fazendo upload da foto do membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        // Validar tamanho da foto (ex: máximo 5MB)
        if (request.getFotoData().length > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Foto deve ter no máximo 5MB");
        }

        membro.updatePhoto(request.getFotoData(), request.getFotoTipo(), request.getFotoNome());
        Membro savedMembro = membroRepository.save(membro);

        log.info("Foto do membro atualizada com sucesso - ID: {}", savedMembro.getId());
        return membroMapper.toResponse(savedMembro);
    }

    @Override
    @CacheEvict(value = "membros", allEntries = true)
    public MembroResponse removeFotoMembro(UUID id) {
        log.info("Removendo foto do membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        membro.removePhoto();
        Membro savedMembro = membroRepository.save(membro);

        log.info("Foto do membro removida com sucesso - ID: {}", savedMembro.getId());
        return membroMapper.toResponse(savedMembro);
    }

    @Override
    @Transactional(readOnly = true)
    public String getFotoMembroBase64(UUID id) {
        log.debug("Buscando foto Base64 do membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        return membro.getFotoBase64();
    }

    // === OPERAÇÕES DE PERFIL ===

    @Override
    @Transactional(readOnly = true)
    public MembroProfileResponse getMembroProfile(UUID id) {
        log.debug("Buscando perfil do membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        return membroMapper.toProfileResponse(membro);
    }

    @Override
    @CacheEvict(value = "membros", allEntries = true)
    public MembroProfileResponse updateMembroProfile(UUID id, UpdateMembroProfileRequest request) {
        log.info("Atualizando perfil do membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        // Validar email se foi alterado
        if (request.getEmail() != null && !request.getEmail().equals(membro.getEmail())) {
            if (!isEmailDisponivelParaAtualizacao(request.getEmail(), id)) {
                throw new IllegalArgumentException("Já existe um membro com o email: " + request.getEmail());
            }
        }

        // Validar cargo se fornecido
        if (request.getCargoAtualId() != null) {
            validarCargoExiste(request.getCargoAtualId());
        }

        membroMapper.updateEntityFromProfileRequest(request, membro);
        Membro updatedMembro = membroRepository.save(membro);

        log.info("Perfil do membro atualizado com sucesso - ID: {}", updatedMembro.getId());
        return membroMapper.toProfileResponse(updatedMembro);
    }

    // === VALIDAÇÕES E ELEGIBILIDADE ===

    @Override
    @Transactional(readOnly = true)
    public ValidarMembroResponse validarMembro(ValidarMembroRequest request) {
        log.debug("Validando membro - Email: {}, CPF: {}", request.getEmail(), request.getCpf());

        // Normalizar CPF
        String cpfNormalizado = request.getCpf().replaceAll("[^0-9]", "");

        // Buscar por email ou CPF
        Membro membro = membroRepository.findByEmail(request.getEmail())
                .or(() -> membroRepository.findByCpf(cpfNormalizado))
                .orElse(null);

        if (membro == null) {
            return ValidarMembroResponse.builder()
                    .podeCreateUser(false)
                    .message("Membro não encontrado com os dados fornecidos")
                    .build();
        }

        return ValidarMembroResponse.builder()
                .membroId(membro.getId())
                .nome(membro.getNome())
                .email(membro.getEmail())
                .cpf(membro.getCpf())
                .podeCreateUser(membro.canCreateUser())
                .message(membro.canCreateUser() ? "Membro pode criar usuário" : "Membro não pode criar usuário")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MembroElegibilidadeResponse verificarElegibilidade(UUID id, List<String> cargosDisponiveis) {
        log.debug("Verificando elegibilidade do membro ID: {}", id);

        Membro membro = membroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + id));

        List<String> cargosElegiveis = new ArrayList<>();
        String motivoInelegibilidade = null;

        if (!membro.isActive()) {
            motivoInelegibilidade = "Membro inativo";
        } else if (cargosDisponiveis != null) {
            for (String cargo : cargosDisponiveis) {
                // Buscar cargo por nome
                Cargo cargoEntity = cargoRepository.findByNome(cargo).orElse(null);
                if (cargoEntity != null && membro.podeSeCandidarPara(cargoEntity)) {
                    cargosElegiveis.add(cargo);
                }
            }

            if (cargosElegiveis.isEmpty()) {
                motivoInelegibilidade = "Não atende aos requisitos hierárquicos para os cargos disponíveis";
            }
        }

        return MembroElegibilidadeResponse.builder()
                .membroId(membro.getId())
                .nomeMembro(membro.getNome())
                .cargoAtual(membro.getNomeCargoAtual())
                .podeVotar(membro.isActive())
                .cargosElegiveis(cargosElegiveis)
                .motivoInelegibilidade(motivoInelegibilidade)
                .build();
    }

    @Override
    @Cacheable(value = "membros-aptos-votacao")
    @Transactional(readOnly = true)
    public List<MembroResponse> getMembrosAptosParaVotacao() {
        log.debug("Buscando membros aptos para votação");

        List<Membro> membros = membroRepository.findMembrosAptosParaVotacao();
        return membroMapper.toResponseList(membros);
    }

    // === FILTROS E BUSCAS ===

    @Override
    @Transactional(readOnly = true)
    public Page<MembroListResponse> buscarMembrosComFiltros(MembroFilterRequest filtros, Pageable pageable) {
        log.debug("Buscando membros com filtros: {}", filtros);

        // Implementação simplificada - em um caso real, usaria Specifications ou Criteria API
        Page<Membro> membros;

        if (filtros.getCargoAtualId() != null) {
            membros = membroRepository.findByCargoAtualId(filtros.getCargoAtualId(), pageable);
        } else if (filtros.getAtivo() != null) {
            membros = membroRepository.findByAtivo(filtros.getAtivo(), pageable);
        } else {
            membros = membroRepository.findAll(pageable);
        }

        return membros.map(membroMapper::toListResponse);
    }

    @Override
    @Cacheable(value = "membros-listagem")
    @Transactional(readOnly = true)
    public List<MembroListResponse> getMembrosParaListagem() {
        log.debug("Buscando membros para listagem");

        List<Membro> membros = membroRepository.findByAtivoTrueOrderByNomeAsc();
        return membroMapper.toListResponseList(membros);
    }

    // === VALIDAÇÕES ===

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailDisponivel(String email) {
        return !membroRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailDisponivelParaAtualizacao(String email, UUID membroId) {
        return !membroRepository.existsByEmailAndIdNot(email, membroId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCpfDisponivel(String cpf) {
        String cpfNormalizado = cpf.replaceAll("[^0-9]", "");
        return !membroRepository.existsByCpf(cpfNormalizado);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCpfDisponivelParaAtualizacao(String cpf, UUID membroId) {
        String cpfNormalizado = cpf.replaceAll("[^0-9]", "");
        return !membroRepository.existsByCpfAndIdNot(cpfNormalizado, membroId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteMembro(UUID id) {
        // Por enquanto, sempre pode deletar se o membro existir
        // Futuramente verificar se está sendo usado em candidaturas ou votos
        return membroRepository.existsById(id);
    }

    // === OPERAÇÕES DE USUÁRIO ===

    @Override
    @CacheEvict(value = "membros", allEntries = true)
    public MembroResponse associarUsuario(UUID membroId, UUID userId) {
        log.info("Associando usuário {} ao membro {}", userId, membroId);

        Membro membro = membroRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + membroId));

        if (!membro.canCreateUser()) {
            throw new IllegalStateException("Membro não pode ter usuário associado");
        }

        if (membroRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("Usuário já está associado a outro membro");
        }

        membro.associateUser(userId);
        Membro savedMembro = membroRepository.save(membro);

        log.info("Usuário associado com sucesso - Membro ID: {}", savedMembro.getId());
        return membroMapper.toResponse(savedMembro);
    }

    @Override
    @CacheEvict(value = "membros", allEntries = true)
    public MembroResponse desassociarUsuario(UUID membroId) {
        log.info("Desassociando usuário do membro {}", membroId);

        Membro membro = membroRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + membroId));

        membro.dissociateUser();
        Membro savedMembro = membroRepository.save(membro);

        log.info("Usuário desassociado com sucesso - Membro ID: {}", savedMembro.getId());
        return membroMapper.toResponse(savedMembro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembroResponse> getMembrosQuePodemCriarUsuario() {
        log.debug("Buscando membros que podem criar usuário");

        List<Membro> membros = membroRepository.findMembrosQuePodemCriarUsuario();
        return membroMapper.toResponseList(membros);
    }

    // === ESTATÍSTICAS ===

    @Override
    @Cacheable(value = "membro-stats-total")
    @Transactional(readOnly = true)
    public long getTotalMembros() {
        return membroRepository.count();
    }

    @Override
    @Cacheable(value = "membro-stats-ativos")
    @Transactional(readOnly = true)
    public long getTotalMembrosAtivos() {
        return membroRepository.countByAtivo(true);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalMembrosInativos() {
        return membroRepository.countByAtivo(false);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalMembrosPorCargo(UUID cargoId) {
        return membroRepository.countMembrosPorCargo(cargoId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalMembrosSemCargo() {
        return membroRepository.findMembrosSemCargo().size();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalMembrosAptosParaVotacao() {
        return membroRepository.findMembrosAptosParaVotacao().size();
    }

    // === UTILITÁRIOS ===

    @Override
    @Cacheable(value = "membros-basic-info")
    @Transactional(readOnly = true)
    public List<MembroBasicInfo> getMembrosBasicInfo() {
        log.debug("Buscando informações básicas dos membros ativos");

        List<Membro> membros = membroRepository.findByAtivoTrueOrderByNomeAsc();
        return membroMapper.toBasicInfoList(membros);
    }

    @Override
    public void validarDadosMembro(String nome, String email, String cpf) {
        // Validar nome
        if (nome == null || nome.trim().length() < 2) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 2 caracteres");
        }

        if (nome.trim().length() > 100) {
            throw new IllegalArgumentException("Nome deve ter no máximo 100 caracteres");
        }

        // Validar email
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email deve ter formato válido");
        }

        // Validar CPF
        if (!Membro.isValidCPF(cpf)) {
            throw new IllegalArgumentException("CPF inválido: " + cpf);
        }
    }

    // === MÉTODOS PRIVADOS ===

    /**
     * Valida se o cargo existe e está ativo
     */
    private void validarCargoExiste(UUID cargoId) {
        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + cargoId));

        if (!cargo.isAtivo()) {
            throw new IllegalArgumentException("Cargo inativo não pode ser atribuído: " + cargo.getNome());
        }
    }
}