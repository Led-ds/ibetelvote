package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.candidato.dto.*;
import com.br.ibetelvote.application.mapper.CandidatoMapper;
import com.br.ibetelvote.application.shared.dto.UploadPhotoResponse;
import com.br.ibetelvote.domain.entities.Candidato;
import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Eleicao;
import com.br.ibetelvote.domain.entities.Membro;
import com.br.ibetelvote.domain.services.CandidatoService;
import com.br.ibetelvote.infrastructure.repositories.CandidatoJpaRepository;
import com.br.ibetelvote.infrastructure.repositories.CargoJpaRepository;
import com.br.ibetelvote.infrastructure.repositories.EleicaoJpaRepository;
import com.br.ibetelvote.infrastructure.repositories.MembroJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    private static final long MAX_FILE_SIZE = 500 * 1024; // 500KB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    // === OPERAÇÕES BÁSICAS ===

    @Override
    @CacheEvict(value = {"candidatos", "candidatos-eleicao", "candidatos-cargo"}, allEntries = true)
    public CandidatoResponse createCandidato(CreateCandidatoRequest request) {
        log.info("Criando candidatura - Membro: {}, Cargo: {}, Eleição: {}",
                request.getMembroId(), request.getCargoPretendidoId(), request.getEleicaoId());

        // 1. Validar e buscar entidades relacionadas
        Membro membro = validarEBuscarMembro(request.getMembroId());
        Eleicao eleicao = validarEBuscarEleicao(request.getEleicaoId());
        Cargo cargoPretendido = validarEBuscarCargo(request.getCargoPretendidoId());

        // 2. Validar regras de negócio
        validarRegrasNegocioCandidatura(membro, eleicao, cargoPretendido, request);

        // 3. Criar candidatura
        Candidato candidato = candidatoMapper.toEntity(request);
        Candidato savedCandidato = candidatoRepository.save(candidato);

        log.info("Candidatura criada com sucesso - ID: {}, Nome: {}",
                savedCandidato.getId(), savedCandidato.getNomeCandidato());

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
    @Cacheable(value = "candidatos", key = "#id + '-with-photo'")
    @Transactional(readOnly = true)
    public CandidatoResponse getCandidatoByIdWithPhoto(UUID id) {
        log.debug("Buscando candidato por ID com foto: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        return candidatoMapper.toResponseWithPhoto(candidato);
    }

    @Override
    @Cacheable(value = "candidatos-eleicao", key = "#eleicaoId")
    @Transactional(readOnly = true)
    public List<CandidatoResponse> getCandidatosByEleicaoId(UUID eleicaoId) {
        log.debug("Buscando candidatos da eleição: {}", eleicaoId);

        List<Candidato> candidatos = candidatoRepository.findByEleicaoIdOrderByNomeCandidatoAsc(eleicaoId);
        return candidatoMapper.toResponseList(candidatos);
    }

    @Override
    @Cacheable(value = "candidatos-cargo", key = "#cargoId")
    @Transactional(readOnly = true)
    public List<CandidatoResponse> getCandidatosByCargoPretendidoId(UUID cargoId) {
        log.debug("Buscando candidatos do cargo pretendido: {}", cargoId);

        List<Candidato> candidatos = candidatoRepository.findByCargoPretendidoIdOrderByNomeCandidatoAsc(cargoId);
        return candidatoMapper.toResponseList(candidatos);
    }

    @Override
    @CacheEvict(value = {"candidatos", "candidatos-eleicao", "candidatos-cargo"}, allEntries = true)
    public CandidatoResponse updateCandidato(UUID id, UpdateCandidatoRequest request) {
        log.info("Atualizando candidato ID: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        // Validar se pode ser atualizado
        validarPodeSerAtualizado(candidato);

        // Validar número do candidato se foi alterado
        if (request.getNumeroCandidato() != null &&
                !request.getNumeroCandidato().equals(candidato.getNumeroCandidato())) {
            validarNumeroCandidatoDisponivel(request.getNumeroCandidato(), candidato.getEleicaoId(), id);
        }

        // Validar cargo pretendido se foi alterado
        if (request.getCargoPretendidoId() != null &&
                !request.getCargoPretendidoId().equals(candidato.getCargoPretendidoId())) {
            validarMudancaCargoPretendido(candidato, request.getCargoPretendidoId());
        }

        candidatoMapper.updateEntityFromRequest(request, candidato);
        Candidato updatedCandidato = candidatoRepository.save(candidato);

        log.info("Candidato atualizado com sucesso - ID: {}", updatedCandidato.getId());
        return candidatoMapper.toResponse(updatedCandidato);
    }

    @Override
    @CacheEvict(value = {"candidatos", "candidatos-eleicao", "candidatos-cargo"}, allEntries = true)
    public void deleteCandidato(UUID id) {
        log.info("Removendo candidato ID: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        if (!canDeleteCandidato(id)) {
            throw new IllegalStateException("Não é possível remover candidato que já recebeu votos ou está em eleição ativa");
        }

        candidatoRepository.delete(candidato);
        log.info("Candidato removido com sucesso - ID: {}", id);
    }

    // === OPERAÇÕES DE APROVAÇÃO ===

    @Override
    @CacheEvict(value = {"candidatos", "candidatos-eleicao", "candidatos-cargo"}, allEntries = true)
    public void aprovarCandidato(UUID id) {
        log.info("Aprovando candidato ID: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        validarParaAprovacao(candidato);

        candidato.aprovar();
        candidatoRepository.save(candidato);

        log.info("Candidato aprovado com sucesso - ID: {}, Nome: {}", id, candidato.getNomeCandidato());
    }

    @Override
    @CacheEvict(value = {"candidatos", "candidatos-eleicao", "candidatos-cargo"}, allEntries = true)
    public void reprovarCandidato(UUID id, String motivo) {
        log.info("Reprovando candidato ID: {} - Motivo: {}", id, motivo);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo da reprovação é obrigatório");
        }

        candidato.reprovar(motivo.trim());
        candidatoRepository.save(candidato);

        log.info("Candidato reprovado - ID: {}, Nome: {}", id, candidato.getNomeCandidato());
    }

    @Override
    @CacheEvict(value = {"candidatos", "candidatos-eleicao", "candidatos-cargo"}, allEntries = true)
    public void aprovarCandidatos(List<UUID> candidatoIds) {
        log.info("Aprovando candidatos em lote - IDs: {}", candidatoIds);

        for (UUID candidatoId : candidatoIds) {
            try {
                aprovarCandidato(candidatoId);
            } catch (Exception e) {
                log.error("Erro ao aprovar candidato ID: {} - {}", candidatoId, e.getMessage());
                // Continua com os outros candidatos
            }
        }

        log.info("Aprovação em lote concluída para {} candidatos", candidatoIds.size());
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

        validarNumeroCandidatoDisponivel(numero, candidato.getEleicaoId(), id);

        candidato.definirNumero(numero.trim());
        candidatoRepository.save(candidato);

        log.info("Número definido com sucesso - Candidato: {}, Número: {}", id, numero);
    }

    @Override
    @CacheEvict(value = "candidatos", key = "#id")
    public void updateCargoPretendido(UUID id, UUID novoCargoPretendidoId) {
        log.info("Atualizando cargo pretendido do candidato ID: {} para cargo: {}", id, novoCargoPretendidoId);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        validarMudancaCargoPretendido(candidato, novoCargoPretendidoId);

        candidato.updateCargoPretendido(novoCargoPretendidoId);
        candidatoRepository.save(candidato);

        log.info("Cargo pretendido atualizado com sucesso - Candidato: {}", id);
    }

    // === OPERAÇÕES DE FOTO ===

    @Override
    @CacheEvict(value = "candidatos", key = "#id")
    public UploadPhotoResponse uploadFotoCampanha(UUID id, MultipartFile file) {
        log.info("Fazendo upload de foto de campanha para candidato ID: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        try {
            validatePhotoFile(file);

            byte[] fotoData = file.getBytes();
            String contentType = file.getContentType();
            String fileName = file.getOriginalFilename();

            candidato.updateFotoCampanha(fotoData, contentType, fileName);
            candidatoRepository.save(candidato);

            log.info("Upload de foto de campanha concluído para candidato ID: {} - Arquivo: {}", id, fileName);

            return UploadPhotoResponse.builder()
                    .fileName(fileName)
                    .fotoBase64(candidato.getFotoCampanhaDataUri())
                    .message("Upload realizado com sucesso")
                    .build();

        } catch (IOException e) {
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
            candidato.removeFotoCampanha();
            candidatoRepository.save(candidato);
            log.info("Foto de campanha removida com sucesso - ID: {}", id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getFotoCampanhaBase64(UUID id) {
        log.debug("Buscando foto de campanha em Base64 para candidato ID: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        return candidato.getFotoCampanhaDataUri();
    }

    // === CONSULTAS ESPECÍFICAS ===

    @Override
    @Cacheable(value = "candidatos-aprovados", key = "#cargoId")
    @Transactional(readOnly = true)
    public List<CandidatoResponse> getCandidatosAprovados(UUID cargoId) {
        log.debug("Buscando candidatos aprovados do cargo: {}", cargoId);

        // ✅ CORRIGIDO: Usando método que existe no repository
        List<Candidato> candidatos = candidatoRepository.findByCargoPretendidoIdAndAtivoTrueAndAprovadoTrue(cargoId);
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

    @Override
    @Transactional(readOnly = true)
    public List<CandidatoListResponse> getCandidatosParaListagem(UUID eleicaoId) {
        log.debug("Buscando candidatos para listagem da eleição: {}", eleicaoId);

        List<Candidato> candidatos = candidatoRepository.findByEleicaoIdAndAtivoTrue(eleicaoId);
        return candidatoMapper.toListResponseList(candidatos);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidatoListResponse> getCandidatosParaListagem(UUID eleicaoId, Pageable pageable) {
        log.debug("Buscando candidatos paginados para listagem da eleição: {}", eleicaoId);

        Page<Candidato> candidatos = candidatoRepository.findByEleicaoId(eleicaoId, pageable);
        return candidatos.map(candidatoMapper::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatoResponse> getCandidatosElegiveis(UUID eleicaoId) {
        log.debug("Buscando candidatos elegíveis da eleição: {}", eleicaoId);

        List<Candidato> candidatos = candidatoRepository.findCandidatosElegiveis(eleicaoId);
        return candidatoMapper.toResponseList(candidatos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatoRankingResponse> getRankingCandidatosPorCargo(UUID cargoId, UUID eleicaoId) {
        log.debug("Buscando ranking de candidatos do cargo: {} na eleição: {}", cargoId, eleicaoId);

        List<Candidato> candidatos = candidatoRepository.findRankingCandidatosPorCargo(cargoId, eleicaoId);
        return candidatoMapper.toRankingResponseList(candidatos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatoResponse> buscarCandidatosPorNome(String nome) {
        log.debug("Buscando candidatos por nome: {}", nome);

        List<Candidato> candidatos = candidatoRepository.findByNomeCandidatoContainingIgnoreCase(nome);
        return candidatoMapper.toResponseList(candidatos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidatoResponse> getCandidatosSemNumero(UUID eleicaoId) {
        log.debug("Buscando candidatos sem número da eleição: {}", eleicaoId);

        List<Candidato> candidatos = candidatoRepository.findCandidatosSemNumero(eleicaoId);
        return candidatoMapper.toResponseList(candidatos);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidatoResponse> buscarCandidatosComFiltros(CandidatoFilterRequest filtros, Pageable pageable) {
        log.debug("Buscando candidatos com filtros: {}", filtros);

        // TODO: Implementar busca com filtros customizados
        // Por enquanto, retorna busca básica
        Page<Candidato> candidatos = candidatoRepository.findByEleicaoId(filtros.getEleicaoId(), pageable);
        return candidatos.map(candidatoMapper::toResponse);
    }

    // === VALIDAÇÕES ===

    @Override
    @Transactional(readOnly = true)
    public boolean existsCandidatoByMembroAndCargo(UUID membroId, UUID cargoId, UUID eleicaoId) {
        return candidatoRepository.existsByMembroIdAndCargoPretendidoIdAndEleicaoId(membroId, cargoId, eleicaoId);
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

        // Não pode deletar se já recebeu votos ou se eleição está ativa
        return candidato.getVotos().isEmpty() &&
                (candidato.getEleicao() == null || !candidato.getEleicao().isVotacaoAberta());
    }

    @Override
    @Transactional(readOnly = true)
    public CandidatoElegibilidadeResponse verificarElegibilidade(UUID id) {
        log.debug("Verificando elegibilidade do candidato ID: {}", id);

        Candidato candidato = candidatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado com ID: " + id));

        return candidatoMapper.toElegibilidadeResponse(candidato);
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
        return candidatoRepository.countByCargoPretendidoId(cargoId);
    }

    @Override
    @Cacheable(value = "candidato-stats", key = "'total-aprovados'")
    @Transactional(readOnly = true)
    public long getTotalCandidatosAprovados() {
        return candidatoRepository.countByAprovado(true);
    }

    @Override
    @Cacheable(value = "candidato-stats", key = "'total-ativos'")
    @Transactional(readOnly = true)
    public long getTotalCandidatosAtivos() {
        return candidatoRepository.countByAtivo(true);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidatoStatsResponse getEstatisticasCandidatos(UUID eleicaoId) {
        log.debug("Buscando estatísticas de candidatos da eleição: {}", eleicaoId);

        long total = candidatoRepository.countByEleicaoId(eleicaoId);
        // ✅ CORRIGIDO: Usando métodos que existem no repository
        long ativos = candidatoRepository.countByEleicaoIdAndAtivoTrue(eleicaoId);
        long aprovados = candidatoRepository.countByEleicaoIdAndAprovadoTrue(eleicaoId);
        long pendentes = candidatoRepository.countByEleicaoIdAndAprovadoFalse(eleicaoId);

        return CandidatoStatsResponse.builder()
                .totalCandidatos(total)
                .candidatosAtivos(ativos)
                .candidatosAprovados(aprovados)
                .candidatosPendentes(pendentes)
                .candidatosInativos(total - ativos)
                .percentualAprovacao(total > 0 ? (aprovados * 100.0) / total : 0.0)
                .build();
    }

    // === MÉTODOS PRIVADOS DE VALIDAÇÃO ===

    private Membro validarEBuscarMembro(UUID membroId) {
        Membro membro = membroRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com ID: " + membroId));

        if (!membro.isActive()) {
            throw new IllegalArgumentException("Membro deve estar ativo para se candidatar");
        }

        return membro;
    }

    private Eleicao validarEBuscarEleicao(UUID eleicaoId) {
        Eleicao eleicao = eleicaoRepository.findById(eleicaoId)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + eleicaoId));

        if (eleicao.isVotacaoAberta()) {
            throw new IllegalStateException("Não é possível cadastrar candidatos durante a votação");
        }

        return eleicao;
    }

    private Cargo validarEBuscarCargo(UUID cargoId) {
        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + cargoId));

        if (!cargo.isAtivo()) {
            throw new IllegalArgumentException("Cargo deve estar ativo para receber candidaturas");
        }

        return cargo;
    }

    private void validarRegrasNegocioCandidatura(Membro membro, Eleicao eleicao, Cargo cargoPretendido, CreateCandidatoRequest request) {
        // Validar se membro pode se candidatar ao cargo (hierarquia eclesiástica)
        if (!membro.podeSeCandidarPara(cargoPretendido)) {
            throw new IllegalArgumentException("Membro não atende aos requisitos hierárquicos para o cargo pretendido");
        }

        // Validar se membro já é candidato para este cargo nesta eleição
        if (candidatoRepository.existsByMembroIdAndCargoPretendidoIdAndEleicaoId(
                request.getMembroId(), request.getCargoPretendidoId(), request.getEleicaoId())) {
            throw new IllegalArgumentException("Membro já é candidato para este cargo nesta eleição");
        }

        // Validar número do candidato se foi fornecido
        if (request.getNumeroCandidato() != null) {
            validarNumeroCandidatoDisponivel(request.getNumeroCandidato(), request.getEleicaoId(), null);
        }
    }

    private void validarNumeroCandidatoDisponivel(String numero, UUID eleicaoId, UUID candidatoId) {
        if (numero == null || numero.trim().isEmpty()) {
            throw new IllegalArgumentException("Número do candidato não pode estar vazio");
        }

        boolean existe = candidatoId != null ?
                candidatoRepository.existsByNumeroCandidatoAndEleicaoIdAndIdNot(numero, eleicaoId, candidatoId) :
                candidatoRepository.existsByNumeroCandidatoAndEleicaoId(numero, eleicaoId);

        if (existe) {
            throw new IllegalArgumentException("Número de candidato já está em uso nesta eleição");
        }
    }

    private void validarPodeSerAtualizado(Candidato candidato) {
        if (candidato.getEleicao() != null && candidato.getEleicao().isVotacaoAberta()) {
            throw new IllegalStateException("Não é possível atualizar candidato durante a votação");
        }
    }

    private void validarMudancaCargoPretendido(Candidato candidato, UUID novoCargoPretendidoId) {
        Cargo novoCargo = validarEBuscarCargo(novoCargoPretendidoId);

        if (!candidato.getMembro().podeSeCandidarPara(novoCargo)) {
            throw new IllegalArgumentException("Membro não atende aos requisitos hierárquicos para o novo cargo pretendido");
        }

        // Verificar se já existe candidatura para o novo cargo
        if (candidatoRepository.existsByMembroIdAndCargoPretendidoIdAndEleicaoId(
                candidato.getMembroId(), novoCargoPretendidoId, candidato.getEleicaoId())) {
            throw new IllegalArgumentException("Membro já é candidato para este cargo nesta eleição");
        }
    }

    private void validarParaAprovacao(Candidato candidato) {
        if (!candidato.isAtivo()) {
            throw new IllegalStateException("Só é possível aprovar candidatos ativos");
        }

        if (candidato.isAprovado()) {
            throw new IllegalStateException("Candidato já está aprovado");
        }

        try {
            candidato.validarParaAprovacao();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Candidato não pode ser aprovado: " + e.getMessage());
        }
    }

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

        // Validar nome do arquivo
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do arquivo é obrigatório");
        }

        // Validar extensão
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "webp");
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Extensão de arquivo não permitida: " + extension);
        }
    }

    // === MÉTODOS UTILITÁRIOS ===

    private void logOperacao(String operacao, UUID candidatoId, String detalhes) {
        log.info("Operação: {} - Candidato ID: {} - Detalhes: {}", operacao, candidatoId, detalhes);
    }

    private void validarParametrosObrigatorios(Object... parametros) {
        for (Object parametro : parametros) {
            if (parametro == null) {
                throw new IllegalArgumentException("Parâmetros obrigatórios não podem ser nulos");
            }
        }
    }

    private String formatarMensagemValidacao(String campo, String valor, String motivo) {
        return String.format("Campo '%s' com valor '%s' é inválido: %s", campo, valor, motivo);
    }
}