package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.mapper.VotoMapper;
import com.br.ibetelvote.application.voto.dto.ValidarVotacaoResponse;
import com.br.ibetelvote.application.voto.dto.VotarRequest;
import com.br.ibetelvote.application.voto.dto.VotoFilterRequest;
import com.br.ibetelvote.application.voto.dto.VotoResponse;
import com.br.ibetelvote.domain.entities.*;
import com.br.ibetelvote.domain.entities.enums.TipoVoto;
import com.br.ibetelvote.domain.services.VotoService;
import com.br.ibetelvote.infrastructure.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final EleicaoConfigService eleicaoConfigService;

    // === OPERAÇÃO PRINCIPAL REFATORADA ===

    @Override
    @CacheEvict(value = {"votos-cache", "estatisticas-cache", "resultados-cache"}, allEntries = true)
    public List<VotoResponse> votar(UUID membroId, VotarRequest request, String ipOrigem, String userAgent) {
        log.info("Processando votação - Membro: {}, Eleição: {}", membroId, request.getEleicaoId());

        // Validação completa
        ValidarVotacaoResponse validacao = validarVotacaoCompleta(membroId, request);
        if (!validacao.isVotacaoValida()) {
            throw new IllegalArgumentException("Erros de validação: " + String.join(", ", validacao.getErros()));
        }

        // Buscar entidades uma única vez (evitar N+1)
        Membro membro = membroRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado"));
        Eleicao eleicao = eleicaoRepository.findById(request.getEleicaoId())
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada"));

        List<Voto> votosRegistrados = new ArrayList<>();

        // Processar cada voto
        for (VotarRequest.VotoIndividual votoIndividual : request.getVotos()) {
            Voto voto = processarVotoIndividualRefatorado(membro, eleicao, votoIndividual, ipOrigem, userAgent);
            votosRegistrados.add(voto);
        }

        // Atualizar contador da eleição
        eleicao.incrementarVotantes();
        eleicaoRepository.save(eleicao);

        log.info("Votação processada com sucesso - Membro: {}, Total de votos: {}", membroId, votosRegistrados.size());

        return votosRegistrados.stream()
                .map(votoMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Voto processarVotoIndividualRefatorado(Membro membro, Eleicao eleicao,
                                                   VotarRequest.VotoIndividual votoIndividual,
                                                   String ipOrigem, String userAgent) {

        // Buscar cargo uma única vez
        Cargo cargoPretendido = cargoRepository.findById(votoIndividual.getCargoPretendidoId())
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado"));

        Voto voto;

        // Factory methods refatorados usando entidades
        if (Boolean.TRUE.equals(votoIndividual.getVotoBranco())) {
            voto = Voto.criarVotoBranco(membro, eleicao, cargoPretendido);
        }
        else if (Boolean.TRUE.equals(votoIndividual.getVotoNulo())) {
            voto = Voto.criarVotoNulo(membro, eleicao, cargoPretendido);
        }
        else if (votoIndividual.getCandidatoId() != null) {
            Candidato candidato = candidatoRepository.findById(votoIndividual.getCandidatoId())
                    .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado"));

            validarCandidatoParaVoto(candidato, cargoPretendido, eleicao);
            voto = Voto.criarVotoValido(membro, eleicao, candidato);
        }
        else {
            throw new IllegalArgumentException("Tipo de voto inválido");
        }

        // Configurar dados de segurança
        voto.definirDadosOrigem(ipOrigem, userAgent);
        String hash = Voto.gerarHashVoto(membro, voto.getCandidato(), LocalDateTime.now());
        voto.definirHashSeguranca(hash);

        // Validação final
        voto.validarVotoCompleto();

        return votoRepository.save(voto);
    }

    private void validarCandidatoParaVoto(Candidato candidato, Cargo cargoPretendido, Eleicao eleicao) {
        if (!candidato.podeReceberVotos()) {
            throw new IllegalArgumentException("Candidato não está disponível para receber votos");
        }

        if (!candidato.getCargoPretendido().equals(cargoPretendido)) {
            throw new IllegalArgumentException("Candidato não pertence ao cargo especificado");
        }

        if (!candidato.getEleicao().equals(eleicao)) {
            throw new IllegalArgumentException("Candidato não pertence à eleição especificada");
        }
    }

    // === CONSULTAS OTIMIZADAS ===

    @Override
    @Transactional(readOnly = true)
    public List<VotoResponse> getVotosByMembroId(UUID membroId) {
        log.debug("Buscando votos do membro: {}", membroId);
        List<Voto> votos = votoRepository.findByMembroIdWithEntities(membroId);
        return votoMapper.toResponseList(votos);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean membroJaVotou(UUID membroId, UUID eleicaoId) {
        return votoRepository.existsByMembroIdAndEleicaoId(membroId, eleicaoId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean membroJaVotouNoCargo(UUID membroId, UUID cargoPretendidoId, UUID eleicaoId) {
        return votoRepository.existsByMembroIdAndCargoPretendidoIdAndEleicaoId(membroId, cargoPretendidoId, eleicaoId);
    }

    @Override
    @Cacheable(value = "votos-cache", key = "'eleicao:' + #eleicaoId")
    @Transactional(readOnly = true)
    public List<VotoResponse> getVotosByEleicaoId(UUID eleicaoId) {
        log.debug("Buscando votos da eleição: {}", eleicaoId);
        List<Voto> votos = votoRepository.findByEleicaoIdWithEntities(eleicaoId);
        return votoMapper.toResponseList(votos);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VotoResponse> getVotosByEleicaoPaginados(UUID eleicaoId, Pageable pageable) {
        Page<Voto> votos = votoRepository.findByEleicaoIdWithEntities(eleicaoId, pageable);
        return votos.map(votoMapper::toResponse);
    }

    @Override
    @Cacheable(value = "estatisticas-cache", key = "'total-eleicao:' + #eleicaoId")
    @Transactional(readOnly = true)
    public long getTotalVotosByEleicao(UUID eleicaoId) {
        return votoRepository.countByEleicaoId(eleicaoId);
    }

    @Override
    @Cacheable(value = "votos-cache", key = "'cargo:' + #cargoPretendidoId")
    @Transactional(readOnly = true)
    public List<VotoResponse> getVotosByCargoPretendidoId(UUID cargoPretendidoId) {
        log.debug("Buscando votos do cargo pretendido: {}", cargoPretendidoId);
        List<Voto> votos = votoRepository.findByCargoPretendidoIdWithEntities(cargoPretendidoId);
        return votoMapper.toResponseList(votos);
    }

    @Override
    @Cacheable(value = "estatisticas-cache", key = "'total-cargo:' + #cargoPretendidoId")
    @Transactional(readOnly = true)
    public long getTotalVotosByCargoPretendido(UUID cargoPretendidoId) {
        return votoRepository.countByCargoPretendidoId(cargoPretendidoId);
    }

    @Override
    @Cacheable(value = "votos-cache", key = "'candidato:' + #candidatoId")
    @Transactional(readOnly = true)
    public List<VotoResponse> getVotosByCandidatoId(UUID candidatoId) {
        log.debug("Buscando votos do candidato: {}", candidatoId);
        List<Voto> votos = votoRepository.findByCandidatoIdWithEntities(candidatoId);
        return votoMapper.toResponseList(votos);
    }

    @Override
    @Cacheable(value = "estatisticas-cache", key = "'total-candidato:' + #candidatoId")
    @Transactional(readOnly = true)
    public long getTotalVotosByCandidato(UUID candidatoId) {
        return votoRepository.countByCandidatoId(candidatoId);
    }

    // === ESTATÍSTICAS REFATORADAS ===

    @Override
    @Cacheable(value = "estatisticas-cache", key = "'votacao:' + #eleicaoId")
    @Transactional(readOnly = true)
    public Map<String, Long> getEstatisticasVotacao(UUID eleicaoId) {
        log.debug("Gerando estatísticas de votação para eleição: {}", eleicaoId);

        Map<String, Long> stats = new HashMap<>();

        stats.put("totalVotos", votoRepository.countByEleicaoId(eleicaoId));
        stats.put("votosValidos", votoRepository.countByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.CANDIDATO));
        stats.put("votosBranco", votoRepository.countByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.BRANCO));
        stats.put("votosNulo", votoRepository.countByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.NULO));
        stats.put("votantesUnicos", votoRepository.countDistinctMembroByEleicaoId(eleicaoId));

        return stats;
    }

    @Override
    @Cacheable(value = "estatisticas-cache", key = "'cargo:' + #cargoPretendidoId")
    @Transactional(readOnly = true)
    public Map<String, Long> getEstatisticasPorCargo(UUID cargoPretendidoId) {
        log.debug("Gerando estatísticas do cargo pretendido: {}", cargoPretendidoId);

        Map<String, Long> stats = new HashMap<>();

        stats.put("totalVotos", votoRepository.countByCargoPretendidoId(cargoPretendidoId));
        stats.put("votosValidos", votoRepository.countByCargoPretendidoIdAndTipoVoto(cargoPretendidoId, TipoVoto.CANDIDATO));
        stats.put("votosBranco", votoRepository.countByCargoPretendidoIdAndTipoVoto(cargoPretendidoId, TipoVoto.BRANCO));
        stats.put("votosNulo", votoRepository.countByCargoPretendidoIdAndTipoVoto(cargoPretendidoId, TipoVoto.NULO));

        return stats;
    }

    @Override
    @Cacheable(value = "resultados-cache", key = "'candidatos:' + #eleicaoId")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getResultadosPorCandidato(UUID eleicaoId) {
        log.debug("Gerando resultados por candidato para eleição: {}", eleicaoId);

        List<Object[]> resultados = votoRepository.countVotosByCandidatoAndCargo(eleicaoId);

        return resultados.stream()
                .map(resultado -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("candidatoId", resultado[0]);
                    item.put("nomeCandidato", resultado[1]);
                    item.put("cargoId", resultado[2]);
                    item.put("nomeCargo", resultado[3]);
                    item.put("totalVotos", resultado[4]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "resultados-cache", key = "'ranking:' + #eleicaoId + ':' + #cargoPretendidoId")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRankingCandidatosPorCargo(UUID eleicaoId, UUID cargoPretendidoId) {
        log.debug("Gerando ranking de candidatos - Eleição: {}, Cargo: {}", eleicaoId, cargoPretendidoId);

        List<Object[]> ranking = votoRepository.findRankingCandidatosPorVotos(eleicaoId, cargoPretendidoId);

        return ranking.stream()
                .map(item -> {
                    Map<String, Object> candidato = new HashMap<>();
                    candidato.put("candidatoId", item[0]);
                    candidato.put("nomeCandidato", item[1]);
                    candidato.put("numeroCandidato", item[2]);
                    candidato.put("totalVotos", item[3]);
                    candidato.put("percentualVotos", item[4]);
                    return candidato;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "estatisticas-cache", key = "'progresso:' + #eleicaoId")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProgressoVotacaoPorHora(UUID eleicaoId) {
        log.debug("Gerando progresso de votação por hora para eleição: {}", eleicaoId);

        List<Object[]> progresso = votoRepository.countVotosByHora(eleicaoId);

        return progresso.stream()
                .map(item -> {
                    Map<String, Object> hora = new HashMap<>();
                    hora.put("hora", item[0]);
                    hora.put("totalVotos", item[1]);
                    hora.put("votosAcumulados", item[2]);
                    return hora;
                })
                .collect(Collectors.toList());
    }

    // === VALIDAÇÕES MELHORADAS ===

    @Override
    @Transactional(readOnly = true)
    public boolean isEleicaoDisponivelParaVotacao(UUID eleicaoId) {
        return eleicaoRepository.findById(eleicaoId)
                .map(Eleicao::isVotacaoAberta)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMembroElegivelParaVotar(UUID membroId) {
        return membroRepository.findById(membroId)
                .map(membro -> membro.isActive() && membro.hasUser())
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public ValidarVotacaoResponse validarVotacaoCompleta(UUID membroId, VotarRequest request) {
        List<String> erros = validarVotacao(membroId, request);

        return ValidarVotacaoResponse.builder()
                .votacaoValida(erros.isEmpty())
                .erros(erros)
                .avisos(List.of()) // Implementar avisos se necessário
                .totalVotos(request.getVotos() != null ? request.getVotos().size() : 0)
                .membroElegivel(isMembroElegivelParaVotar(membroId))
                .eleicaoDisponivel(isEleicaoDisponivelParaVotacao(request.getEleicaoId()))
                .jaVotou(membroJaVotou(membroId, request.getEleicaoId()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> validarVotacao(UUID membroId, VotarRequest request) {
        List<String> erros = new ArrayList<>();

        // Validar membro
        Optional<Membro> membroOpt = membroRepository.findById(membroId);
        if (membroOpt.isEmpty()) {
            erros.add("Membro não encontrado");
            return erros;
        }

        Membro membro = membroOpt.get();
        if (!membro.isActive()) {
            erros.add("Membro deve estar ativo para votar");
        }

        if (!membro.hasUser()) {
            erros.add("Membro deve ter usuário associado para votar");
        }

        // Validar eleição
        Optional<Eleicao> eleicaoOpt = eleicaoRepository.findById(request.getEleicaoId());
        if (eleicaoOpt.isEmpty()) {
            erros.add("Eleição não encontrada");
            return erros;
        }

        Eleicao eleicao = eleicaoOpt.get();
        if (!eleicao.isVotacaoAberta()) {
            erros.add("Eleição não está aberta para votação");
        }

        // Validar votos individuais
        if (request.getVotos() == null || request.getVotos().isEmpty()) {
            erros.add("Deve informar pelo menos um voto");
        } else {
            erros.addAll(validarVotosIndividuais(request.getVotos(), membro, eleicao));
        }

        return erros;
    }

    private List<String> validarVotosIndividuais(List<VotarRequest.VotoIndividual> votos, Membro membro, Eleicao eleicao) {
        List<String> erros = new ArrayList<>();
        Map<UUID, Integer> votosPorCargo = new HashMap<>();

        for (VotarRequest.VotoIndividual voto : votos) {
            // Contar votos por cargo
            votosPorCargo.merge(voto.getCargoPretendidoId(), 1, Integer::sum);

            // Validar cargo
            Optional<Cargo> cargoOpt = cargoRepository.findById(voto.getCargoPretendidoId());
            if (cargoOpt.isEmpty()) {
                erros.add("Cargo não encontrado: " + voto.getCargoPretendidoId());
                continue;
            }

            Cargo cargo = cargoOpt.get();
            if (!cargo.isAtivo()) {
                erros.add("Cargo não está ativo para votação");
                continue;
            }

            int votosJaDadosNoCargo = eleicao.contarVotosDoMembroNoCargo(membro.getId(), cargo.getId());
            int votosNaRequisicao = votosPorCargo.get(cargo.getId());
            int totalVotosSeConfirmado = votosJaDadosNoCargo + votosNaRequisicao;
            int limiteVagas = eleicao.getLimiteVotosPorCargo(cargo.getId());

            if (totalVotosSeConfirmado > limiteVagas) {
                erros.add(String.format("Excede limite de %d votos para cargo %s (já tem %d, tentando adicionar %d)",
                        limiteVagas, cargo.getNome(), votosJaDadosNoCargo, votosNaRequisicao));
                continue;
            }

            // Validar tipo de voto
            if (!validarTipoVoto(voto)) {
                erros.add("Deve escolher exatamente um tipo de voto por cargo");
                continue;
            }

            if (voto.getCandidatoId() != null) {
                if (eleicao.membroJaVotouNoCandidato(membro.getId(), voto.getCandidatoId())) {
                    erros.add("Membro já votou neste candidato");
                    continue;
                }

                erros.addAll(validarCandidato(voto, cargo, eleicao));
                erros.addAll(validarHierarquiaVotacao(membro, cargo));
            }
        }

        return erros;
    }

    private boolean validarTipoVoto(VotarRequest.VotoIndividual voto) {
        int tiposVoto = 0;
        if (Boolean.TRUE.equals(voto.getVotoBranco())) tiposVoto++;
        if (Boolean.TRUE.equals(voto.getVotoNulo())) tiposVoto++;
        if (voto.getCandidatoId() != null) tiposVoto++;
        return tiposVoto == 1;
    }

    private List<String> validarCandidato(VotarRequest.VotoIndividual voto, Cargo cargo, Eleicao eleicao) {
        List<String> erros = new ArrayList<>();

        Optional<Candidato> candidatoOpt = candidatoRepository.findById(voto.getCandidatoId());
        if (candidatoOpt.isEmpty()) {
            erros.add("Candidato não encontrado: " + voto.getCandidatoId());
            return erros;
        }

        Candidato candidato = candidatoOpt.get();

        if (!candidato.getCargoPretendido().equals(cargo)) {
            erros.add("Candidato não pertence ao cargo especificado");
        }

        if (!candidato.podeReceberVotos()) {
            erros.add("Candidato não está disponível para receber votos");
        }

        if (!candidato.getEleicao().equals(eleicao)) {
            erros.add("Candidato não pertence à eleição especificada");
        }

        return erros;
    }

    private List<String> validarHierarquiaVotacao(Membro membro, Cargo cargoVotacao) {
        List<String> erros = new ArrayList<>();
        // TODO: Implementar validações hierárquicas específicas
        // Exemplo: Diáconos podem votar apenas em cargos diaconais e inferiores
        return erros;
    }

    // === AUDITORIA ===

    @Override
    @Cacheable(value = "estatisticas-cache", key = "'total-validos'")
    @Transactional(readOnly = true)
    public long getTotalVotosValidos() {
        return votoRepository.countByTipoVoto(TipoVoto.CANDIDATO);
    }

    @Override
    @Cacheable(value = "estatisticas-cache", key = "'total-branco'")
    @Transactional(readOnly = true)
    public long getTotalVotosBranco() {
        return votoRepository.countByTipoVoto(TipoVoto.BRANCO);
    }

    @Override
    @Cacheable(value = "estatisticas-cache", key = "'total-nulo'")
    @Transactional(readOnly = true)
    public long getTotalVotosNulo() {
        return votoRepository.countByTipoVoto(TipoVoto.NULO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VotoResponse> getVotosParaAuditoria(UUID eleicaoId) {
        log.debug("Gerando dados de auditoria para eleição: {}", eleicaoId);

        List<Voto> votos = votoRepository.findVotosParaAuditoria(eleicaoId);
        return votos.stream()
                .map(votoMapper::toResponse)
                .collect(Collectors.toList());
    }

    // === RESUMO DETALHADO ===

    @Override
    @Cacheable(value = "resultados-cache", key = "'resumo:' + #eleicaoId")
    @Transactional(readOnly = true)
    public Map<String, Object> getResumoVotacaoDetalhado(UUID eleicaoId) {
        log.debug("Gerando resumo detalhado para eleição: {}", eleicaoId);

        Map<String, Object> resumo = new HashMap<>();

        resumo.put("estatisticasGerais", getEstatisticasVotacao(eleicaoId));
        resumo.put("resultadosPorCandidato", getResultadosPorCandidato(eleicaoId));
        resumo.put("progressoTemporal", getProgressoVotacaoPorHora(eleicaoId));
        resumo.put("distribuicaoPorTipo", getDistribuicaoVotosPorTipo(eleicaoId));
        resumo.put("participacaoPorCargo", getParticipacaoPorCargoMembro(eleicaoId));

        return resumo;
    }

    private Map<String, Long> getDistribuicaoVotosPorTipo(UUID eleicaoId) {
        Map<String, Long> distribuicao = new HashMap<>();

        distribuicao.put("CANDIDATO", votoRepository.countByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.CANDIDATO));
        distribuicao.put("BRANCO", votoRepository.countByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.BRANCO));
        distribuicao.put("NULO", votoRepository.countByEleicaoIdAndTipoVoto(eleicaoId, TipoVoto.NULO));

        return distribuicao;
    }

    private List<Map<String, Object>> getParticipacaoPorCargoMembro(UUID eleicaoId) {
        List<Object[]> participacao = votoRepository.getParticipacaoPorCargoMembro(eleicaoId);

        return participacao.stream()
                .map(item -> {
                    Map<String, Object> part = new HashMap<>();
                    part.put("cargoMembro", item[0]);
                    part.put("quantidadeVotantes", item[1]);
                    part.put("percentualParticipacao", item[2]);
                    return part;
                })
                .collect(Collectors.toList());
    }

    // === ANÁLISE DE SEGURANÇA ===

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAnaliseSeguranca(UUID eleicaoId) {
        log.debug("Analisando segurança da votação: {}", eleicaoId);

        Map<String, Object> analise = new HashMap<>();

        analise.put("integridade", validarIntegridadeVotacao(eleicaoId));
        analise.put("votosSuspeitos", getVotosSuspeitos(eleicaoId));
        analise.put("hashsDuplicados", getHashsDuplicados(eleicaoId));
        analise.put("distribuicaoIP", getDistribuicaoIP(eleicaoId));
        analise.put("padroesTempo", getPadroesTemporais(eleicaoId));

        return analise;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validarIntegridadeVotacao(UUID eleicaoId) {
        List<Voto> votos = votoRepository.findByEleicaoId(eleicaoId);

        for (Voto voto : votos) {
            String hashCalculado = Voto.gerarHashVoto(
                    voto.getMembro(),
                    voto.getCandidato(),
                    voto.getDataVoto()
            );

            if (!Objects.equals(hashCalculado, voto.getHashVoto())) {
                log.warn("Hash inválido encontrado no voto: {}", voto.getId());
                return false;
            }
        }

        return true;
    }

    private List<Map<String, Object>> getVotosSuspeitos(UUID eleicaoId) {
        List<Object[]> suspeitos = votoRepository.findVotosSuspeitos(eleicaoId);

        return suspeitos.stream()
                .map(item -> {
                    Map<String, Object> suspeito = new HashMap<>();
                    suspeito.put("ipOrigem", item[0]);
                    suspeito.put("totalVotos", item[1]);
                    suspeito.put("tempoMedio", item[2]);
                    suspeito.put("flagSuspeito", ((Long) item[1]) > 10); // Mais de 10 votos do mesmo IP
                    return suspeito;
                })
                .collect(Collectors.toList());
    }

    private int getHashsDuplicados(UUID eleicaoId) {
        return votoRepository.countHashDuplicados(eleicaoId);
    }

    private List<Map<String, Object>> getDistribuicaoIP(UUID eleicaoId) {
        List<Object[]> distribuicao = votoRepository.countVotosPorIpOrigem(eleicaoId);

        return distribuicao.stream()
                .limit(20) // Top 20 IPs
                .map(item -> {
                    Map<String, Object> ip = new HashMap<>();
                    ip.put("ipMascarado", mascarIP((String) item[0]));
                    ip.put("totalVotos", item[1]);
                    return ip;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getPadroesTemporais(UUID eleicaoId) {
        List<Object[]> padroes = votoRepository.findPadroesTemporaisSuspeitos(eleicaoId);

        return padroes.stream()
                .map(item -> {
                    Map<String, Object> padrao = new HashMap<>();
                    padrao.put("hora", item[0]);
                    padrao.put("votosRapidos", item[1]);
                    padrao.put("flagSuspeito", ((Long) item[1]) > 50);
                    return padrao;
                })
                .collect(Collectors.toList());
    }

    // === MÉTODOS NOVOS ===

    @Override
    @Transactional(readOnly = true)
    public Page<VotoResponse> buscarVotosComFiltros(VotoFilterRequest filtros, Pageable pageable) {
        Page<Voto> votos = votoRepository.findWithFilters(filtros, pageable);
        return votos.map(votoMapper::toResponse);
    }

    @Override
    @Cacheable(value = "estatisticas-cache", key = "'tempo-real:' + #eleicaoId")
    @Transactional(readOnly = true)
    public Map<String, Object> getMetricasTempoReal(UUID eleicaoId) {
        Map<String, Object> metricas = new HashMap<>();

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime umMinutoAtras = agora.minusMinutes(1);
        LocalDateTime umaHoraAtras = agora.minusHours(1);

        metricas.put("totalVotosMinuto", votoRepository.countVotosUltimoMinuto(eleicaoId, umMinutoAtras));
        metricas.put("totalVotosHora", votoRepository.countVotosUltimaHora(eleicaoId, umaHoraAtras));
        metricas.put("velocidadeVotacao", votoRepository.getVelocidadeVotacao(eleicaoId));
        metricas.put("tempoMedioVoto", votoRepository.getTempoMedioEntreVotos(eleicaoId));
        metricas.put("participacaoAtual", calcularParticipacaoAtual(eleicaoId));

        return metricas;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean membroJaVotouNoCandidato(UUID membroId, UUID candidatoId) {
        return votoRepository.existsByMembroIdAndCandidatoId(membroId, candidatoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> consultarLimiteVotacao(UUID membroId, UUID eleicaoId, UUID cargoId) {
        // Buscar eleição
        Eleicao eleicao = eleicaoRepository.findById(eleicaoId)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada"));

        // Obter informações de limite
        Eleicao.LimiteVotacaoInfo info = eleicao.getLimiteVotacaoParaMembro(membroId, cargoId);

        Map<String, Object> limite = new HashMap<>();
        limite.put("cargoId", info.cargoId);
        limite.put("limiteVotos", info.limiteVotos);
        limite.put("votosJaDados", info.votosJaDados);
        limite.put("votosRestantes", info.getVotosRestantes());
        limite.put("podeVotarMais", info.podeVotarMais);
        limite.put("candidatosJaVotados", info.candidatosJaVotados);

        return limite;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean membroPodeVotarMaisNoCargo(UUID membroId, UUID eleicaoId, UUID cargoId) {
        Eleicao eleicao = eleicaoRepository.findById(eleicaoId)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada"));

        return eleicao.membroPodeVotarMaisNoCargo(membroId, cargoId);
    }

    private Double calcularParticipacaoAtual(UUID eleicaoId) {
        Long totalVotantes = votoRepository.countDistinctMembroByEleicaoId(eleicaoId);
        Long totalMembrosElegiveis = membroRepository.countMembrosAtivos();

        if (totalMembrosElegiveis == 0) {
            return 0.0;
        }

        return (totalVotantes.doubleValue() / totalMembrosElegiveis.doubleValue()) * 100.0;
    }

    // === MÉTODOS UTILITÁRIOS ===

    private String mascarIP(String ip) {
        if (ip == null) return "IP não registrado";

        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".*.*";
        }

        // IPv6 - mascarar os últimos blocos
        if (ip.contains(":")) {
            String[] v6Parts = ip.split(":");
            if (v6Parts.length >= 4) {
                return v6Parts[0] + ":" + v6Parts[1] + ":****:****";
            }
        }

        return "IP mascarado";
    }

    // === CACHE UTILITIES ===

    @CacheEvict(value = {"votos-cache", "estatisticas-cache", "resultados-cache"}, allEntries = true)
    public void limparCacheVotos() {
        log.info("Cache de votos limpo");
    }

    @CacheEvict(value = "estatisticas-cache", key = "'tempo-real:' + #eleicaoId")
    public void atualizarMetricasTempoReal(UUID eleicaoId) {
        log.debug("Métricas de tempo real atualizadas para eleição: {}", eleicaoId);
    }
}