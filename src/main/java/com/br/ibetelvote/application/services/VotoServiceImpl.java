package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.eleicao.dto.*;
import com.br.ibetelvote.application.mapper.VotoMapper;
import com.br.ibetelvote.domain.entities.*;
import com.br.ibetelvote.domain.services.VotoService;
import com.br.ibetelvote.infrastructure.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VotoServiceImpl implements VotoService {

    private final VotoJpaRepository votoRepository;
    private final MembroJpaRepository membroRepository;
    private final EleicaoJpaRepository eleicaoRepository;
    private final CargoJpaRepository cargoRepository;
    private final CandidatoJpaRepository candidatoRepository;
    private final VotoMapper votoMapper;

    // === OPERAÇÃO PRINCIPAL ===

    @Override
    @CacheEvict(value = {"votos", "eleicoes", "cargos", "candidatos", "voto-stats"}, allEntries = true)
    public List<VotoResponse> votar(UUID membroId, VotarRequest request, String ipOrigem, String userAgent) {
        log.info("Processando votação - Membro: {}, Eleição: {}", membroId, request.getEleicaoId());

        // Validações gerais
        List<String> erros = validarVotacao(membroId, request);
        if (!erros.isEmpty()) {
            throw new IllegalArgumentException("Erros de validação: " + String.join(", ", erros));
        }

        List<Voto> votosRegistrados = new ArrayList<>();

        // Processar cada voto individual
        for (VotarRequest.VotoIndividual votoIndividual : request.getVotos()) {
            Voto voto = processarVotoIndividual(membroId, request.getEleicaoId(), votoIndividual, ipOrigem, userAgent);
            votosRegistrados.add(voto);
        }

        // Atualizar contador de votantes na eleição
        Eleicao eleicao = eleicaoRepository.findById(request.getEleicaoId()).orElseThrow();
        eleicao.incrementarVotantes();
        eleicaoRepository.save(eleicao);

        log.info("Votação processada com sucesso - Membro: {}, Total de votos: {}", membroId, votosRegistrados.size());

        return votosRegistrados.stream()
                .map(votoMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Voto processarVotoIndividual(UUID membroId, UUID eleicaoId, VotarRequest.VotoIndividual votoIndividual,
                                         String ipOrigem, String userAgent) {
        Voto voto;

        // Determinar tipo de voto
        if (Boolean.TRUE.equals(votoIndividual.getVotoBranco())) {
            voto = Voto.criarVotoBranco(membroId, eleicaoId, votoIndividual.getCargoId());
        } else if (Boolean.TRUE.equals(votoIndividual.getVotoNulo())) {
            voto = Voto.criarVotoNulo(membroId, eleicaoId, votoIndividual.getCargoId());
        } else if (votoIndividual.getCandidatoId() != null) {
            // Validar se candidato existe e pode receber votos
            Candidato candidato = candidatoRepository.findById(votoIndividual.getCandidatoId())
                    .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado"));

            if (!candidato.podeReceberVotos()) {
                throw new IllegalArgumentException("Candidato não está disponível para receber votos");
            }

            voto = Voto.criarVotoValido(membroId, eleicaoId, votoIndividual.getCargoId(), votoIndividual.getCandidatoId());
        } else {
            throw new IllegalArgumentException("Tipo de voto inválido");
        }

        // Definir dados de origem e hash de segurança
        voto.definirDadosOrigem(ipOrigem, userAgent);
        String hash = Voto.gerarHashVoto(membroId, voto.getCandidatoId(), LocalDateTime.now());
        voto.definirHashSeguranca(hash);

        // Validar consistência
        voto.validarConsistencia();

        return votoRepository.save(voto);
    }

    // === CONSULTAS POR MEMBRO ===

    @Override
    @Transactional(readOnly = true)
    public List<VotoResponse> getVotosByMembroId(UUID membroId) {
        log.debug("Buscando votos do membro: {}", membroId);

        List<Voto> votos = votoRepository.findByMembroId(membroId);
        return votoMapper.toResponseList(votos);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean membroJaVotou(UUID membroId, UUID eleicaoId) {
        return !votoRepository.findByEleicaoIdAndMembroId(eleicaoId, membroId).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean membroJaVotouNoCargo(UUID membroId, UUID cargoId, UUID eleicaoId) {
        return votoRepository.existsByMembroIdAndCargoIdAndEleicaoId(membroId, cargoId, eleicaoId);
    }

    // === CONSULTAS POR ELEIÇÃO ===

    @Override
    @Cacheable(value = "votos-eleicao", key = "#eleicaoId")
    @Transactional(readOnly = true)
    public List<VotoResponse> getVotosByEleicaoId(UUID eleicaoId) {
        log.debug("Buscando votos da eleição: {}", eleicaoId);

        List<Voto> votos = votoRepository.findByEleicaoId(eleicaoId);
        return votoMapper.toResponseList(votos);
    }

    @Override
    @Cacheable(value = "voto-stats", key = "'total-eleicao-' + #eleicaoId")
    @Transactional(readOnly = true)
    public long getTotalVotosByEleicao(UUID eleicaoId) {
        return votoRepository.countByEleicaoId(eleicaoId);
    }

    // === CONSULTAS POR CARGO ===

    @Override
    @Cacheable(value = "votos-cargo", key = "#cargoId")
    @Transactional(readOnly = true)
    public List<VotoResponse> getVotosByCargoId(UUID cargoId) {
        log.debug("Buscando votos do cargo: {}", cargoId);

        List<Voto> votos = votoRepository.findByCargoId(cargoId);
        return votoMapper.toResponseList(votos);
    }

    @Override
    @Cacheable(value = "voto-stats", key = "'total-cargo-' + #cargoId")
    @Transactional(readOnly = true)
    public long getTotalVotosByCargo(UUID cargoId) {
        return votoRepository.countByCargoId(cargoId);
    }

    // === CONSULTAS POR CANDIDATO ===

    @Override
    @Cacheable(value = "votos-candidato", key = "#candidatoId")
    @Transactional(readOnly = true)
    public List<VotoResponse> getVotosByCandidatoId(UUID candidatoId) {
        log.debug("Buscando votos do candidato: {}", candidatoId);

        List<Voto> votos = votoRepository.findByCandidatoId(candidatoId);
        return votoMapper.toResponseList(votos);
    }

    @Override
    @Cacheable(value = "voto-stats", key = "'total-candidato-' + #candidatoId")
    @Transactional(readOnly = true)
    public long getTotalVotosByCandidato(UUID candidatoId) {
        return votoRepository.countByCandidatoId(candidatoId);
    }

    // === RELATÓRIOS E ESTATÍSTICAS ===

    @Override
    @Cacheable(value = "estatisticas-votacao", key = "#eleicaoId")
    @Transactional(readOnly = true)
    public Map<String, Long> getEstatisticasVotacao(UUID eleicaoId) {
        log.debug("Gerando estatísticas de votação para eleição: {}", eleicaoId);

        Map<String, Long> stats = new HashMap<>();

        List<Voto> votos = votoRepository.findByEleicaoId(eleicaoId);

        stats.put("totalVotos", (long) votos.size());
        stats.put("votosValidos", votos.stream().filter(Voto::isVotoValido).count());
        stats.put("votosBranco", votos.stream().filter(Voto::isVotoBranco).count());
        stats.put("votosNulo", votos.stream().filter(Voto::isVotoNulo).count());

        return stats;
    }

    @Override
    @Cacheable(value = "estatisticas-cargo", key = "#cargoId")
    @Transactional(readOnly = true)
    public Map<String, Long> getEstatisticasPorCargo(UUID cargoId) {
        log.debug("Gerando estatísticas do cargo: {}", cargoId);

        Map<String, Long> stats = new HashMap<>();

        List<Voto> votos = votoRepository.findByCargoId(cargoId);

        stats.put("totalVotos", (long) votos.size());
        stats.put("votosValidos", votos.stream().filter(Voto::isVotoValido).count());
        stats.put("votosBranco", votos.stream().filter(Voto::isVotoBranco).count());
        stats.put("votosNulo", votos.stream().filter(Voto::isVotoNulo).count());

        return stats;
    }

    @Override
    @Cacheable(value = "resultados-candidatos", key = "#eleicaoId")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getResultadosPorCandidato(UUID eleicaoId) {
        log.debug("Gerando resultados por candidato para eleição: {}", eleicaoId);

        List<Object[]> resultados = votoRepository.countVotosByCandidatoAndCargo(eleicaoId);

        return resultados.stream()
                .map(resultado -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("nomeCandidato", resultado[0]);
                    item.put("totalVotos", resultado[1]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "progresso-votacao", key = "#eleicaoId")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProgressoVotacaoPorHora(UUID eleicaoId) {
        log.debug("Gerando progresso de votação por hora para eleição: {}", eleicaoId);

        List<Object[]> progresso = votoRepository.countVotosByHora(eleicaoId);

        return progresso.stream()
                .map(item -> {
                    Map<String, Object> hora = new HashMap<>();
                    hora.put("hora", item[0]);
                    hora.put("totalVotos", item[1]);
                    return hora;
                })
                .collect(Collectors.toList());
    }

    // === VALIDAÇÕES ===

    @Override
    @Transactional(readOnly = true)
    public boolean isEleicaoDisponivelParaVotacao(UUID eleicaoId) {
        Eleicao eleicao = eleicaoRepository.findById(eleicaoId).orElse(null);
        return eleicao != null && eleicao.isVotacaoAberta();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMembroElegivelParaVotar(UUID membroId) {
        Membro membro = membroRepository.findById(membroId).orElse(null);
        return membro != null && membro.isActive() && membro.hasUser();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> validarVotacao(UUID membroId, VotarRequest request) {
        List<String> erros = new ArrayList<>();

        // Validar membro
        Membro membro = membroRepository.findById(membroId).orElse(null);
        if (membro == null) {
            erros.add("Membro não encontrado");
            return erros; // Retorna imediatamente se membro não existe
        }

        if (!membro.isActive()) {
            erros.add("Membro deve estar ativo para votar");
        }

        if (!membro.hasUser()) {
            erros.add("Membro deve ter usuário associado para votar");
        }

        // Validar eleição
        Eleicao eleicao = eleicaoRepository.findById(request.getEleicaoId()).orElse(null);
        if (eleicao == null) {
            erros.add("Eleição não encontrada");
            return erros; // Retorna imediatamente se eleição não existe
        }

        if (!eleicao.isVotacaoAberta()) {
            erros.add("Eleição não está aberta para votação");
        }

        // Validar se já votou na eleição
        if (membroJaVotou(membroId, request.getEleicaoId())) {
            erros.add("Membro já votou nesta eleição");
        }

        // Validar votos individuais
        if (request.getVotos() == null || request.getVotos().isEmpty()) {
            erros.add("Deve informar pelo menos um voto");
        } else {
            Set<UUID> cargosVotados = new HashSet<>();

            for (VotarRequest.VotoIndividual voto : request.getVotos()) {
                // Verificar voto duplicado no mesmo cargo
                if (cargosVotados.contains(voto.getCargoId())) {
                    erros.add("Não é possível votar duas vezes no mesmo cargo");
                    continue;
                }
                cargosVotados.add(voto.getCargoId());

                // Validar cargo
                Cargo cargo = cargoRepository.findById(voto.getCargoId()).orElse(null);
                if (cargo == null) {
                    erros.add("Cargo não encontrado: " + voto.getCargoId());
                    continue;
                }

                if (!cargo.getEleicaoId().equals(request.getEleicaoId())) {
                    erros.add("Cargo não pertence à eleição especificada");
                    continue;
                }

                // Validar tipo de voto
                int tiposVoto = 0;
                if (Boolean.TRUE.equals(voto.getVotoBranco())) tiposVoto++;
                if (Boolean.TRUE.equals(voto.getVotoNulo())) tiposVoto++;
                if (voto.getCandidatoId() != null) tiposVoto++;

                if (tiposVoto != 1) {
                    erros.add("Deve escolher exatamente um tipo de voto por cargo");
                    continue;
                }

                // Validar voto branco se permitido
                if (Boolean.TRUE.equals(voto.getVotoBranco()) && !cargo.permiteVotoBranco()) {
                    erros.add("Cargo não permite voto em branco");
                }

                // Validar candidato se foi especificado
                if (voto.getCandidatoId() != null) {
                    Candidato candidato = candidatoRepository.findById(voto.getCandidatoId()).orElse(null);
                    if (candidato == null) {
                        erros.add("Candidato não encontrado: " + voto.getCandidatoId());
                    } else if (!candidato.getCargoId().equals(voto.getCargoId())) {
                        erros.add("Candidato não pertence ao cargo especificado");
                    } else if (!candidato.podeReceberVotos()) {
                        erros.add("Candidato não está disponível para receber votos");
                    }
                }
            }
        }

        return erros;
    }

    // === AUDITORIA ===

    @Override
    @Cacheable(value = "voto-stats", key = "'total-validos'")
    @Transactional(readOnly = true)
    public long getTotalVotosValidos() {
        return votoRepository.countVotosValidos();
    }

    @Override
    @Cacheable(value = "voto-stats", key = "'total-branco'")
    @Transactional(readOnly = true)
    public long getTotalVotosBranco() {
        return votoRepository.countByVotoBrancoTrue();
    }

    @Override
    @Cacheable(value = "voto-stats", key = "'total-nulo'")
    @Transactional(readOnly = true)
    public long getTotalVotosNulo() {
        return votoRepository.countByVotoNuloTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VotoResponse> getVotosParaAuditoria(UUID eleicaoId) {
        log.debug("Gerando dados de auditoria para eleição: {}", eleicaoId);

        List<Voto> votos = votoRepository.findByEleicaoId(eleicaoId);
        return votos.stream()
                .map(votoMapper::toAuditResponse) // Remove dados sensíveis
                .collect(Collectors.toList());
    }
}