package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.eleicao.dto.*;
import com.br.ibetelvote.application.mapper.CandidatoMapper;
import com.br.ibetelvote.application.shared.dto.UploadPhotoResponse;
import com.br.ibetelvote.domain.entities.Candidato;
import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Eleicao;
import com.br.ibetelvote.domain.entities.Membro;
import com.br.ibetelvote.domain.services.CandidatoService;
import com.br.ibetelvote.domain.services.FileStorageService;
import com.br.ibetelvote.infrastructure.repositories.CandidatoJpaRepository;
import com.br.ibetelvote.infrastructure.repositories.CargoJpaRepository;
import com.br.ibetelvote.infrastructure.repositories.EleicaoJpaRepository;
import com.br.ibetelvote.infrastructure.repositories.MembroJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CandidatoServiceImpl implements CandidatoService {

    private final CandidatoJpaRepository candidatoRepository;
    private final MembroJpaRepository membroRepository;
    private final EleicaoJpaRepository eleicaoRepository;
    private final CargoJpaRepository cargoRepository;
    private final CandidatoMapper candidatoMapper;
    private final FileStorageService fileStorageService;

    // === OPERAÇÕES BÁSICAS ===

    @Override
    @CacheEvict(value = {"candidatos", "eleicoes", "cargos"}, allEntries = true)
    public CandidatoResponse createCandidato(CreateCandidatoRequest request) {
        log.info("Criando candidatura - Membro: {}, Cargo: {}", request.getMembroId(), request.getCargoId());

        // Validar se membro existe e está ativo
        Membro membro = membroRepository.findById(request.getMembroId())
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + request.getMembroId()));

        if (!membro.isActive()) {
            throw new IllegalArgumentException("Membro deve estar ativo para se candidatar");
        }

        // Validar se eleição existe
        Eleicao eleicao = eleicaoRepository.findById(request.getEleicaoId())
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + request.getEleicaoId()));

        // Validar se cargo existe
        Cargo cargo = cargoRepository.findById(request.getCargoId())
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + request.getCargoId()));

        // Validar se cargo pertence à eleição
        if (!cargo.getEleicaoId().equals(request.getEleicaoId())) {
            throw new IllegalArgumentException("Cargo não pertence à eleição especificada");
        }

        // Validar se votação não está em andamento
        if (eleicao.isVotacaoAberta()) {
            throw new IllegalStateException("Não é possível cadastrar candidatos durante a votação");
        }

        // Validar se membro já é candidato para este cargo
        if (candidatoRepository.existsByMembroIdAndCargoId(request.getMembroId(), request.getCargoId())) {
            throw new IllegalArgumentException("Membro já é candidato para este cargo");
        }

        // Validar número do candidato se foi fornecido
        if (request.getNumeroCandidato() != null &&
                candidatoRepository.existsByNumeroCandidatoAndEleicaoId(request.getNumeroCandidato(), request.getEleicaoId())) {
            throw new IllegalArgumentException("Número de candidato já está em uso nesta eleição");
        }

        Candidato candidato = candidatoMapper.toEntity(request);
        Candidato savedCandidato = candidatoRepository.save(candidato);

        log.info("Candidatura criada com sucesso - ID: {}, Nome: {}", savedCandidato.getId(), savedCandidato.getNomeCandidato());
        return candidatoMapper.toResponse(savedCandidato);
    }

    @Override
    @Cacheable(value = "candidatos", key = "#id")
    @Transactional(readOnly = true)
    public CandidatoResponse getCandidatoById(UUID id) {
        log.debug("Buscando candidato por ID: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        return candidatoMapper.toResponse(candidato);
    }

    @Override
    @Cacheable(value = "candidatos-eleicao", key = "#eleicaoId")
    @Transactional(readOnly = true)
    public List<CandidatoResponse> getCandidatosByEleicaoId(UUID eleicaoId) {
        log.debug("Buscando candidatos da eleição: {}", eleicaoId);

        List<Candidato> candidatos = candidatoRepository.findByEleicaoId(eleicaoId);
        return candidatoMapper.toResponseList(candidatos);
    }

    @Override
    @Cacheable(value = "candidatos-cargo", key = "#cargoId")
    @Transactional(readOnly = true)
    public List<CandidatoResponse> getCandidatosByCargoId(UUID cargoId) {
        log.debug("Buscando candidatos do cargo: {}", cargoId);

        List<Candidato> candidatos = candidatoRepository.findByCargoId(cargoId);
        return candidatoMapper.toResponseList(candidatos);
    }

    @Override
    @CacheEvict(value = {"candidatos", "eleicoes", "cargos"}, allEntries = true)
    public CandidatoResponse updateCandidato(UUID id, UpdateCandidatoRequest request) {
        log.info("Atualizando candidato ID: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        // Validar se pode ser atualizado
        if (candidato.getEleicao() != null && candidato.getEleicao().isVotacaoAberta()) {
            throw new IllegalStateException("Não é possível atualizar candidato durante a votação");
        }

        // Validar número do candidato se foi alterado
        if (request.getNumeroCandidato() != null &&
                !request.getNumeroCandidato().equals(candidato.getNumeroCandidato()) &&
                candidatoRepository.existsByNumeroCandidatoAndEleicaoId(request.getNumeroCandidato(), candidato.getEleicaoId())) {
            throw new IllegalArgumentException("Número de candidato já está em uso nesta eleição");
        }

        candidatoMapper.updateEntityFromRequest(request, candidato);
        Candidato updatedCandidato = candidatoRepository.save(candidato);

        log.info("Candidato atualizado com sucesso - ID: {}", updatedCandidato.getId());
        return candidatoMapper.toResponse(updatedCandidato);
    }

    @Override
    @CacheEvict(value = {"candidatos", "eleicoes", "cargos"}, allEntries = true)
    public void deleteCandidato(UUID id) {
        log.info("Removendo candidato ID: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        // Validar se pode ser removido
        if (!canDeleteCandidato(id)) {
            throw new IllegalStateException("Não é possível remover candidato que já recebeu votos");
        }

        // Remover foto de campanha se existir
        if (candidato.temFotoCampanha()) {
            try {
                fileStorageService.deleteFile(candidato.getFotoCampanha().replace("/api/v1/files/", ""));
            } catch (Exception e) {
                log.warn("Erro ao remover foto do candidato {}: {}", id, e.getMessage());
            }
        }

        candidatoRepository.delete(candidato);
        log.info("Candidato removido com sucesso - ID: {}", id);
    }

    // === OPERAÇÕES DE APROVAÇÃO ===

    @Override
    @CacheEvict(value = {"candidatos", "eleicoes", "cargos"}, allEntries = true)
    public void aprovarCandidato(UUID id) {
        log.info("Aprovando candidato ID: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        if (!candidato.isAtivo()) {
            throw new IllegalStateException("Só é possível aprovar candidatos ativos");
        }

        candidato.aprovar();
        candidatoRepository.save(candidato);

        log.info("Candidato aprovado com sucesso - ID: {}, Nome: {}", id, candidato.getNomeCandidato());
    }

    @Override
    @CacheEvict(value = {"candidatos", "eleicoes", "cargos"}, allEntries = true)
    public void reprovarCandidato(UUID id, String motivo) {
        log.info("Reprovando candidato ID: {} - Motivo: {}", id, motivo);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da reprovação é obrigatório");
        }

        candidato.reprovar(motivo);
        candidatoRepository.save(candidato);

        log.info("Candidato reprovado - ID: {}, Nome: {}", id, candidato.getNomeCandidato());
    }

    // === OPERAÇÕES DE CONTROLE ===

    @Override
    @CacheEvict(value = "candidatos", key = "#id")
    public void ativarCandidato(UUID id) {
        log.info("Ativando candidato ID: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        candidato.activate();
        candidatoRepository.save(candidato);

        log.info("Candidato ativado com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = "candidatos", key = "#id")
    public void desativarCandidato(UUID id) {
        log.info("Desativando candidato ID: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        candidato.deactivate();
        candidatoRepository.save(candidato);

        log.info("Candidato desativado com sucesso - ID: {}", id);
    }

    @Override
    @CacheEvict(value = "candidatos", key = "#id")
    public void definirNumeroCandidato(UUID id, String numero) {
        log.info("Definindo número {} para candidato ID: {}", numero, id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        // Validar se número já está em uso
        if (candidatoRepository.existsByNumeroCandidatoAndEleicaoId(numero, candidato.getEleicaoId())) {
            throw new IllegalArgumentException("Número já está em uso nesta eleição");
        }

        candidato.definirNumero(numero);
        candidatoRepository.save(candidato);

        log.info("Número definido com sucesso - Candidato: {}, Número: {}", id, numero);
    }

    // === OPERAÇÕES DE FOTO ===

    @Override
    @CacheEvict(value = "candidatos", key = "#id")
    public UploadPhotoResponse uploadFotoCampanha(UUID id, MultipartFile file) {
        log.info("Fazendo upload de foto de campanha para candidato ID: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        try {
            // Remover foto anterior se existir
            if (candidato.temFotoCampanha()) {
                fileStorageService.deleteFile(candidato.getFotoCampanha().replace("/api/v1/files/", ""));
            }

            String fileName = fileStorageService.storeFile(file, "candidatos/fotos");
            String fileUrl = "/api/v1/files/" + fileName;

            candidato.updateFotoCampanha(fileUrl);
            candidatoRepository.save(candidato);

            log.info("Upload de foto de campanha concluído para candidato ID: {} - Arquivo: {}", id, fileName);

            return UploadPhotoResponse.builder()
                    .fileName(fileName)
                    .fileUrl(fileUrl)
                    .message("Upload realizado com sucesso")
                    .build();

        } catch (Exception e) {
            log.error("Erro ao fazer upload de foto para candidato ID: {}", id, e);
            throw new RuntimeException("Erro ao fazer upload da foto: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "candidatos", key = "#id")
    public void removeFotoCampanha(UUID id) {
        log.info("Removendo foto de campanha do candidato ID: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        if (candidato.temFotoCampanha()) {
            fileStorageService.deleteFile(candidato.getFotoCampanha().replace("/api/v1/files/", ""));
            candidato.removeFotoCampanha();
            candidatoRepository.save(candidato);

            log.info("Foto de campanha removida com sucesso - ID: {}", id);
        }
    }

    // === CONSULTAS ESPECÍFICAS ===

    @Override
    @Cacheable(value = "candidatos-aprovados", key = "#cargoId")
    @Transactional(readOnly = true)
    public List<CandidatoResponse> getCandidatosAprovados(UUID cargoId) {
        log.debug("Buscando candidatos aprovados do cargo: {}", cargoId);

        List<Candidato> candidatos = candidatoRepository.findCandidatosAprovadosByCargoId(cargoId);
        return candidatoMapper.toResponseList(candidatos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatoResponse> getCandidatosPendentesAprovacao() {
        log.debug("Buscando candidatos pendentes de aprovação");

        List<Candidato> candidatos = candidatoRepository.findByAprovadoFalse();
        return candidatoMapper.toResponseList(candidatos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatoResponse> getCandidatosByMembroId(UUID membroId) {
        log.debug("Buscando candidaturas do membro: {}", membroId);

        List<Candidato> candidatos = candidatoRepository.findByMembroId(membroId);
        return candidatoMapper.toResponseList(candidatos);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidatoResponse getCandidatoByNumero(String numero, UUID eleicaoId) {
        log.debug("Buscando candidato por número: {} na eleição: {}", numero, eleicaoId);

        Candidato candidato = candidatoRepository.findByNumeroCandidatoAndEleicaoId(numero, eleicaoId)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com número: " + numero));

        return candidatoMapper.toResponse(candidato);
    }

    // === VALIDAÇÕES ===

    @Override
    @Transactional(readOnly = true)
    public boolean existsCandidatoByMembroAndCargo(UUID membroId, UUID cargoId) {
        return candidatoRepository.existsByMembroIdAndCargoId(membroId, cargoId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsCandidatoByNumero(String numero, UUID eleicaoId) {
        return candidatoRepository.existsByNumeroCandidatoAndEleicaoId(numero, eleicaoId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteCandidato(UUID id) {
        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        // Não pode deletar se já recebeu votos
        return candidato.getVotos().isEmpty();
    }

    // === ESTATÍSTICAS ===

    @Override
    @Cacheable(value = "candidato-stats", key = "#eleicaoId")
    @Transactional(readOnly = true)
    public long getTotalCandidatosByEleicao(UUID eleicaoId) {
        return candidatoRepository.countByEleicaoId(eleicaoId);
    }

    @Override
    @Cacheable(value = "candidato-stats", key = "#cargoId")
    @Transactional(readOnly = true)
    public long getTotalCandidatosByCargo(UUID cargoId) {
        return candidatoRepository.countByCargoId(cargoId);
    }

    @Override
    @Cacheable(value = "candidato-stats", key = "'total-aprovados'")
    @Transactional(readOnly = true)
    public long getTotalCandidatosAprovados() {
        return candidatoRepository.countByAprovadoTrue();
    }
}